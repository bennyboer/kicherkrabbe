package de.bennyboer.kicherkrabbe.messaging.outbox;

import de.bennyboer.kicherkrabbe.messaging.RoutingKey;
import de.bennyboer.kicherkrabbe.messaging.outbox.persistence.inmemory.InMemoryMessagingOutboxRepo;
import de.bennyboer.kicherkrabbe.messaging.outbox.publisher.LoggingMessagingOutboxEntryPublisher;
import de.bennyboer.kicherkrabbe.messaging.target.ExchangeTarget;
import de.bennyboer.kicherkrabbe.messaging.target.MessageTarget;
import de.bennyboer.kicherkrabbe.persistence.MockReactiveTransactionManager;
import de.bennyboer.kicherkrabbe.testing.time.TestClock;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MessagingOutboxTests {

    private final InMemoryMessagingOutboxRepo repo = new InMemoryMessagingOutboxRepo();

    private final LoggingMessagingOutboxEntryPublisher publisher = new LoggingMessagingOutboxEntryPublisher();

    private final TestClock clock = new TestClock();

    private final MessagingOutbox outbox = new MessagingOutbox(repo, publisher, 2, clock);

    private final ReactiveTransactionManager transactionManager = new MockReactiveTransactionManager();

    private final ExchangeTarget exchange = ExchangeTarget.of("test-exchange");

    private final MessageTarget target = MessageTarget.exchange(exchange);

    private final RoutingKey routingKey = RoutingKey.parse("test.key");

    private final Map<String, Object> payload = Map.of("test", "value");

    @Test
    void shouldInsertEntry() {
        clock.setNow(Instant.parse("2022-05-17T12:30:00Z"));

        // given: an entry to insert
        var entry = MessagingOutboxEntry.create(target, routingKey, payload, clock);

        // when: inserting the entry
        insert(entry);

        // then: the entry is in the outbox repo
        var entries = repo.findAll().collectList().block();
        assertThat(entries.size()).isEqualTo(1);
        assertThat(entries.get(0).getId()).isEqualTo(entry.getId());

        // and: the entry is in the outbox publisher
        var publishedEntries = publisher.getEntries();
        assertThat(publishedEntries.size()).isEqualTo(1);
        assertThat(publishedEntries.get(0).getId()).isEqualTo(entry.getId());
    }

    @Test
    void shouldInsertEntries() {
        // given: entries to insert
        clock.setNow(Instant.parse("2022-05-17T12:30:00Z"));
        var entry1 = MessagingOutboxEntry.create(target, routingKey, payload, clock);
        clock.setNow(Instant.parse("2022-05-17T10:30:01Z"));
        var entry2 = MessagingOutboxEntry.create(target, routingKey, payload, clock);

        // when: inserting the entries
        insert(entry1, entry2);

        // then: the entries are in the outbox repo
        var entries = repo.findAll().collectList().block();
        assertThat(entries.size()).isEqualTo(2);
        var entryIds = entries.stream().map(MessagingOutboxEntry::getId).toList();
        assertThat(entryIds).containsExactlyInAnyOrder(entry1.getId(), entry2.getId());

        // and: the entries are in the outbox publisher
        var publishedEntries = publisher.getEntries();
        assertThat(publishedEntries.size()).isEqualTo(2);
        var publishedEntryIds = publishedEntries.stream().map(MessagingOutboxEntry::getId).toList();
        assertThat(publishedEntryIds).containsExactly(entry2.getId(), entry1.getId());
    }

    @Test
    void shouldFailWhenTryingToInsertWithoutActiveTransaction() {
        assertThatThrownBy(() -> outbox.insert(MessagingOutboxEntry.create(target, routingKey, payload, clock)).block())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No transaction active. A transaction is required to insert entries into the outbox.");
    }

    @Test
    void shouldIncreaseRetryCountWhenPublishingFailsAndFindStaleFailedEntries() {
        // given: an entry to insert
        clock.setNow(Instant.parse("2022-05-17T12:30:00Z"));
        var entry = MessagingOutboxEntry.create(target, routingKey, payload, clock);

        var outboxWithBrokenPublisher = new MessagingOutbox(
                repo,
                entries -> Mono.error(new Exception("Bad luck, broker unavailable!")),
                1,
                clock
        );

        // when: the entry is inserted to the outbox
        insertWithTransaction(outboxWithBrokenPublisher, entry);

        // then: the entry is in the outbox repo with retry count 1 and is ready to be published again
        var entries = repo.findAll().collectList().block();
        assertThat(entries.size()).isEqualTo(1);
        var updatedEntry = entries.get(0);
        assertThat(updatedEntry.getRetryCount()).isEqualTo(1);
        assertThat(updatedEntry.isLocked()).isFalse();
        assertThat(updatedEntry.getLockedAt()).isEmpty();
        assertThat(updatedEntry.getAcknowledgedAt()).isEmpty();
        assertThat(updatedEntry.getFailedAt()).isEmpty();

        // when: trying to publish the entry again
        outboxWithBrokenPublisher.publishNextUnpublishedEntries().block();

        // then: the entry is in the outbox repo with retry count 2
        entries = repo.findAll().collectList().block();
        assertThat(entries.size()).isEqualTo(1);
        updatedEntry = entries.get(0);
        assertThat(updatedEntry.getRetryCount()).isEqualTo(2);
        assertThat(updatedEntry.isLocked()).isFalse();
        assertThat(updatedEntry.getLockedAt()).isEmpty();
        assertThat(updatedEntry.getAcknowledgedAt()).isEmpty();
        assertThat(updatedEntry.getFailedAt()).isEmpty();

        // when: trying to publish the entry three times again
        for (int i = 0; i < 3; i++) {
            outboxWithBrokenPublisher.publishNextUnpublishedEntries().block();
        }

        // then: the entry is in the outbox repo with retry count 5
        entries = repo.findAll().collectList().block();
        assertThat(entries.size()).isEqualTo(1);
        updatedEntry = entries.get(0);
        assertThat(updatedEntry.getRetryCount()).isEqualTo(5);
        assertThat(updatedEntry.isLocked()).isFalse();
        assertThat(updatedEntry.getLockedAt()).isEmpty();
        assertThat(updatedEntry.getAcknowledgedAt()).isEmpty();
        assertThat(updatedEntry.getFailedAt()).isEmpty();

        // when: trying to publish the entry again
        outboxWithBrokenPublisher.publishNextUnpublishedEntries().block();

        // then: the entry is in the outbox repo still with retry count 5 and not ready to be published again
        entries = repo.findAll().collectList().block();
        assertThat(entries.size()).isEqualTo(1);
        updatedEntry = entries.get(0);
        assertThat(updatedEntry.getRetryCount()).isEqualTo(5);
        assertThat(updatedEntry.isLocked()).isTrue();
        assertThat(updatedEntry.getLockedAt()).isNotEmpty();
        assertThat(updatedEntry.getAcknowledgedAt()).isEmpty();
        assertThat(updatedEntry.getFailedAt()).isNotEmpty();

        // when: trying to find old failed entries right away
        var oldFailedEntries = outboxWithBrokenPublisher.findStaleFailedEntries().collectList().block();

        // then: the list is empty
        assertThat(oldFailedEntries).isEmpty();

        // when: trying to find old failed entries after 30 minutes
        clock.setNow(Instant.parse("2022-05-17T13:00:01Z"));
        oldFailedEntries = outboxWithBrokenPublisher.findStaleFailedEntries().collectList().block();

        // then: the list contains the failed entry
        assertThat(oldFailedEntries.size()).isEqualTo(1);
        assertThat(oldFailedEntries.get(0).getId()).isEqualTo(updatedEntry.getId());
    }

    @Test
    void shouldUnlockStaleEntries() {
        // given: an entry in the outbox repo that has been tried to publish but the service crashed along the way
        clock.setNow(Instant.parse("2022-05-17T12:30:00Z"));
        var entry = MessagingOutboxEntry.create(target, routingKey, payload, clock)
                .lock(MessagingOutboxEntryLock.create(), clock);
        repo.insert(List.of(entry)).block();

        // when: trying to publish the next publishable entries
        publishNextUnpublishedEntries();

        // then: the entry is still in the outbox repo and is locked
        var entries = repo.findAll().collectList().block();
        assertThat(entries.size()).isEqualTo(1);
        var updatedEntry = entries.get(0);
        assertThat(updatedEntry.isLocked()).isTrue();
        assertThat(updatedEntry.getLockedAt()).isNotEmpty();

        // when: unlocking stale entries right away
        unlockStaleEntries();

        // then: the entry is still in the outbox repo and is still locked
        entries = repo.findAll().collectList().block();
        assertThat(entries.size()).isEqualTo(1);
        updatedEntry = entries.get(0);
        assertThat(updatedEntry.isLocked()).isTrue();
        assertThat(updatedEntry.getLockedAt()).isNotEmpty();

        // when: unlocking stale entries after 5 minutes
        clock.setNow(Instant.parse("2022-05-17T12:35:01Z"));
        unlockStaleEntries();

        // then: the entry is in the outbox repo and is not locked anymore
        entries = repo.findAll().collectList().block();
        assertThat(entries.size()).isEqualTo(1);
        updatedEntry = entries.get(0);
        assertThat(updatedEntry.isLocked()).isFalse();
        assertThat(updatedEntry.getLockedAt()).isEmpty();
    }

    @Test
    void shouldCleanupOldAcknowledgedEntriesAfter30Days() {
        // when: an entry is inserted in the outbox
        clock.setNow(Instant.parse("2022-05-17T12:30:00Z"));
        var entry = MessagingOutboxEntry.create(target, routingKey, payload, clock);
        insert(entry);

        // then: the entry is acknowledged right away, as it is published
        var entries = repo.findAll().collectList().block();
        assertThat(entries.size()).isEqualTo(1);
        var updatedEntry = entries.get(0);
        assertThat(updatedEntry.getAcknowledgedAt()).isNotEmpty();

        // when: trying to cleanup old acknowledged entries right away
        cleanupOldAcknowledgedEntries();

        // then: the entry is still in the outbox repo
        entries = repo.findAll().collectList().block();
        assertThat(entries.size()).isEqualTo(1);

        // when: trying to cleanup old acknowledged entries after 30 days
        clock.setNow(Instant.parse("2022-06-16T12:30:01Z"));
        cleanupOldAcknowledgedEntries();

        // then: the entry is not in the outbox repo anymore
        entries = repo.findAll().collectList().block();
        assertThat(entries.isEmpty()).isTrue();
    }

    private void insert(MessagingOutboxEntry... entries) {
        insertWithTransaction(outbox, entries);
    }

    private void publishNextUnpublishedEntries() {
        outbox.publishNextUnpublishedEntries().block();
    }

    private void unlockStaleEntries() {
        outbox.unlockStaleEntries().block();
    }

    private void cleanupOldAcknowledgedEntries() {
        outbox.cleanupOldAcknowledgedEntries().block();
    }

    private void insertWithTransaction(MessagingOutbox outbox, MessagingOutboxEntry... entries) {
        TransactionalOperator transactionalOperator = TransactionalOperator.create(transactionManager);

        outbox.insert(entries)
                .as(transactionalOperator::transactional)
                .block();
    }

}
