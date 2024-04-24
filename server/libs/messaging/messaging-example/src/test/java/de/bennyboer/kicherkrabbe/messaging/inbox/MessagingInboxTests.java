package de.bennyboer.kicherkrabbe.messaging.inbox;

import de.bennyboer.kicherkrabbe.messaging.inbox.persistence.IncomingMessageAlreadySeenException;
import de.bennyboer.kicherkrabbe.messaging.inbox.persistence.inmemory.InMemoryMessagingInboxRepo;
import de.bennyboer.kicherkrabbe.testing.time.TestClock;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MessagingInboxTests {

    private final InMemoryMessagingInboxRepo repo = new InMemoryMessagingInboxRepo();

    private final TestClock clock = new TestClock();

    private final MessagingInbox inbox = new MessagingInbox(repo, clock);

    @Test
    void shouldAddMessageToInbox() {
        // given: a message to add to the inbox
        clock.setNow(Instant.parse("2023-02-22T11:15:00Z"));
        var messageId = IncomingMessageId.of("TEST");

        // when: adding the message ID to the inbox
        inbox.addMessage(messageId).block();

        // then: no error is raised

        // when: adding the message to the inbox again; then: an error is raised
        assertThatThrownBy(() -> inbox.addMessage(messageId).block())
                .matches(e -> e.getCause() instanceof IncomingMessageAlreadySeenException);

        // when: adding another message to the inbox with a different ID
        var messageId2 = IncomingMessageId.of("TEST2");
        clock.setNow(Instant.parse("2023-02-23T13:30:00Z"));
        inbox.addMessage(messageId2).block();

        // then: no error is raised

        // and: the inbox contains the two messages
        assertThat(repo.findAll().collectList().block()).containsExactlyInAnyOrder(
                IncomingMessage.of(messageId, Instant.parse("2023-02-22T11:15:00Z")),
                IncomingMessage.of(messageId2, Instant.parse("2023-02-23T13:30:00Z"))
        );
    }

}
