package de.bennyboer.kicherkrabbe.messaging.outbox.persistence;

import de.bennyboer.kicherkrabbe.messaging.RoutingKey;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutboxEntry;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutboxEntryId;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutboxEntryLock;
import de.bennyboer.kicherkrabbe.messaging.target.ExchangeTarget;
import de.bennyboer.kicherkrabbe.messaging.target.MessageTarget;
import de.bennyboer.kicherkrabbe.testing.time.TestClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public abstract class MessagingOutboxRepoTests {

    protected MessagingOutboxRepo repo;

    private final ExchangeTarget exchange = ExchangeTarget.of("test-exchange");

    private final MessageTarget target = MessageTarget.exchange(exchange);

    private final RoutingKey routingKey = RoutingKey.parse("test.key");

    private final Map<String, Object> payload = Map.of("test", "value");

    private final TestClock clock = new TestClock();

    protected abstract MessagingOutboxRepo createRepo();

    protected abstract List<MessagingOutboxEntry> findEntries();

    @BeforeEach
    void setUp() {
        repo = createRepo();
    }

    @Test
    void shouldSaveEntries() {
        clock.setNow(Instant.parse("2023-08-13T13:45:00Z"));

        // given: some entries to save
        var entry1 = MessagingOutboxEntry.create(target, routingKey, payload, clock);
        var entry2 = MessagingOutboxEntry.create(target, routingKey, payload, clock);

        // when: saving the entries
        save(entry1, entry2);

        // then: the entries have been saved
        var entries = findEntries();
        assertThat(entries)
                .containsExactlyInAnyOrder(entry1, entry2);
    }

    @Test
    void shouldInsertEntries() {
        clock.setNow(Instant.parse("2023-08-13T13:45:00Z"));

        // given: some entries to insert
        var entry1 = MessagingOutboxEntry.create(target, routingKey, payload, clock);
        var entry2 = MessagingOutboxEntry.create(target, routingKey, payload, clock);

        // when: inserting the entries
        insert(entry1, entry2);

        // then: the entries have been inserted
        var entries = findEntries();
        assertThat(entries)
                .containsExactlyInAnyOrder(entry1, entry2);
    }

    @Test
    void shouldRaiseErrorWhenTryingToInsertEntryThatIsAlreadyThere() {
        // given: an entry
        var entry = MessagingOutboxEntry.create(target, routingKey, payload, clock);
        insert(entry);

        // when: trying to insert the same entry again, then: an error is raised
        assertThatThrownBy(() -> insert(entry))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void shouldLockNextPublishableEntries() {
        // given: some entries in the outbox
        clock.setNow(Instant.parse("2023-08-13T00:00:00Z"));
        var entry1 = MessagingOutboxEntry.create(target, routingKey, payload, clock);
        clock.setNow(Instant.parse("2023-08-13T02:00:00Z"));
        var entry2 = MessagingOutboxEntry.create(target, routingKey, payload, clock);
        clock.setNow(Instant.parse("2023-08-13T01:00:00Z"));
        var entry3 = MessagingOutboxEntry.create(target, routingKey, payload, clock);
        clock.setNow(Instant.parse("2023-08-13T00:30:00Z"));
        var failedEntry = MessagingOutboxEntry.create(target, routingKey, payload, clock)
                .failed(clock)
                .failed(clock)
                .failed(clock)
                .failed(clock)
                .failed(clock)
                .failed(clock);
        var acknowledgedEntry = MessagingOutboxEntry.create(target, routingKey, payload, clock)
                .acknowledge(clock);
        save(entry1, entry2, entry3, failedEntry, acknowledgedEntry);

        // when: locking the next 2 publishable entries
        var lock = MessagingOutboxEntryLock.create();
        lockNextPublishableEntries(lock, 2, clock);

        // then: the next 2 publishable entries have been locked (sorted by creation date)
        var updatedEntry1 = findEntry(entry1.getId());
        assertThat(updatedEntry1.isLocked()).isTrue();
        assertThat(updatedEntry1.getLock()).contains(lock);

        var updatedEntry2 = findEntry(entry2.getId());
        assertThat(updatedEntry2.isLocked()).isFalse();
        assertThat(updatedEntry2.getLock()).isEmpty();

        var updatedEntry3 = findEntry(entry3.getId());
        assertThat(updatedEntry3.isLocked()).isTrue();
        assertThat(updatedEntry3.getLock()).contains(lock);
    }

    @Test
    void shouldFindLockedEntries() {
        // given: some entries in the outbox
        clock.setNow(Instant.parse("2023-08-13T00:00:00Z"));
        var entry1 = MessagingOutboxEntry.create(target, routingKey, payload, clock);
        clock.setNow(Instant.parse("2023-08-13T02:00:00Z"));
        var entry2 = MessagingOutboxEntry.create(target, routingKey, payload, clock);
        clock.setNow(Instant.parse("2023-08-13T01:00:00Z"));
        var entry3 = MessagingOutboxEntry.create(target, routingKey, payload, clock);
        save(entry1, entry2, entry3);

        // and: locking the next publishable entries in two steps
        var lock1 = MessagingOutboxEntryLock.create();
        lockNextPublishableEntries(lock1, 1, clock);

        var lock2 = MessagingOutboxEntryLock.create();
        lockNextPublishableEntries(lock2, 2, clock);

        // when: finding the entries by the first lock
        var lockedEntries1 = findLockedEntries(lock1);

        // then: the first publishable entry has been locked
        assertThat(lockedEntries1)
                .containsExactly(findEntry(entry1.getId()));

        // when: finding the entries by the second lock
        var lockedEntries2 = findLockedEntries(lock2);

        // then: the other 2 publishable entries have been locked (sorted by creation date)
        assertThat(lockedEntries2)
                .containsExactly(findEntry(entry3.getId()), findEntry(entry2.getId()));
    }

    @Test
    void shouldUnlockEntriesAndMarkAsUnpublishedOlderThanASpecificDate() {
        // given: some entries in the outbox
        clock.setNow(Instant.parse("2023-08-13T00:00:00Z"));
        var entry1 = MessagingOutboxEntry.create(target, routingKey, payload, clock);
        clock.setNow(Instant.parse("2023-08-13T02:00:00Z"));
        var entry2 = MessagingOutboxEntry.create(target, routingKey, payload, clock);
        clock.setNow(Instant.parse("2023-08-13T01:00:00Z"));
        var entry3 = MessagingOutboxEntry.create(target, routingKey, payload, clock);
        save(entry1, entry2, entry3);

        // and: locking the next publishable entries
        var lock = MessagingOutboxEntryLock.create();
        lockNextPublishableEntries(lock, 3, clock);

        // when: unlocking the entries older than a specific date that is before all entries
        unlockEntriesAndMarkAsUnpublishedOlderThan(Instant.parse("2023-08-13T00:00:00Z"));

        // then: no entries have been unlocked
        assertThat(findEntry(entry1.getId()).isLocked()).isTrue();
        assertThat(findEntry(entry2.getId()).isLocked()).isTrue();
        assertThat(findEntry(entry3.getId()).isLocked()).isTrue();

        // when: unlocking the entries older than a specific date that is after all entries
        unlockEntriesAndMarkAsUnpublishedOlderThan(Instant.parse("2023-08-13T03:00:01Z"));

        // then: all entries have been unlocked
        assertThat(findEntry(entry1.getId()).isLocked()).isFalse();
        assertThat(findEntry(entry2.getId()).isLocked()).isFalse();
        assertThat(findEntry(entry3.getId()).isLocked()).isFalse();
    }

    @Test
    void shouldRemoveAcknowledgedEntriesOlderThanASpecificDate() {
        // given: some entries in the outbox
        clock.setNow(Instant.parse("2023-08-13T00:00:00Z"));
        var entry1 = MessagingOutboxEntry.create(target, routingKey, payload, clock);
        clock.setNow(Instant.parse("2023-08-13T02:00:00Z"));
        var entry2 = MessagingOutboxEntry.create(target, routingKey, payload, clock);
        clock.setNow(Instant.parse("2023-08-13T01:00:00Z"));
        var entry3 = MessagingOutboxEntry.create(target, routingKey, payload, clock);
        save(entry1, entry2, entry3);

        // and: publishing and acknowledging the first and the last entry
        entry1 = entry1.acknowledge(clock);
        entry3 = entry3.acknowledge(clock);
        save(entry1, entry3);

        // when: removing the acknowledged entries older than a specific date that is before all entries
        removeAcknowledgedEntriesOlderThan(Instant.parse("2023-08-13T00:00:00Z"));

        // then: no entries have been removed
        assertThat(findEntries())
                .containsExactlyInAnyOrder(entry1, entry2, entry3);

        // when: removing the acknowledged entries older than a specific date that is after all entries
        removeAcknowledgedEntriesOlderThan(Instant.parse("2023-08-13T03:00:01Z"));

        // then: all entries have been removed
        assertThat(findEntries())
                .containsExactlyInAnyOrder(entry2);
    }

    @Test
    void shouldFindStaleFailedEntries() {
        // given: some failed entries in the outbox
        clock.setNow(Instant.parse("2023-08-13T00:00:00Z"));
        var entry1 = MessagingOutboxEntry.create(target, routingKey, payload, clock)
                .failed(clock)
                .failed(clock)
                .failed(clock)
                .failed(clock)
                .failed(clock)
                .failed(clock);
        clock.setNow(Instant.parse("2023-08-13T02:00:00Z"));
        var entry2 = MessagingOutboxEntry.create(target, routingKey, payload, clock)
                .failed(clock)
                .failed(clock)
                .failed(clock)
                .failed(clock)
                .failed(clock)
                .failed(clock);
        save(entry1, entry2);

        // when: finding the old failed entries
        var oldFailedEntries = findFailedEntriesOlderThan(Instant.parse("2023-08-13T01:00:00Z"));

        // then: the first failed entry has been found
        assertThat(oldFailedEntries)
                .containsExactly(findEntry(entry1.getId()));

        // when: finding the old failed entries
        oldFailedEntries = findFailedEntriesOlderThan(Instant.parse("2023-08-13T03:00:00Z"));

        // then: all the failed entries have been found
        assertThat(oldFailedEntries)
                .containsExactlyInAnyOrder(findEntry(entry1.getId()), findEntry(entry2.getId()));
    }

    private void save(MessagingOutboxEntry... entries) {
        repo.save(List.of(entries)).block();
    }

    private void insert(MessagingOutboxEntry... entries) {
        repo.insert(List.of(entries)).block();
    }

    private void lockNextPublishableEntries(MessagingOutboxEntryLock lock, int maxEntries, Clock clock) {
        repo.lockNextPublishableEntries(lock, maxEntries, clock).block();
    }

    private List<MessagingOutboxEntry> findLockedEntries(MessagingOutboxEntryLock lock) {
        return repo.findLockedEntries(lock).collectList().block();
    }

    private void unlockEntriesAndMarkAsUnpublishedOlderThan(Instant date) {
        repo.unlockEntriesOlderThan(date).block();
    }

    private void removeAcknowledgedEntriesOlderThan(Instant date) {
        repo.removeAcknowledgedEntriesOlderThan(date).block();
    }

    private List<MessagingOutboxEntry> findFailedEntriesOlderThan(Instant date) {
        return repo.findFailedEntriesOlderThan(date).collectList().block();
    }

    private MessagingOutboxEntry findEntry(MessagingOutboxEntryId id) {
        return findEntries().stream()
                .filter(entry -> entry.getId().equals(id))
                .findFirst()
                .orElseThrow();
    }

}
