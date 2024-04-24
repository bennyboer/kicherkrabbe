package de.bennyboer.kicherkrabbe.messaging.inbox.persistence;

import de.bennyboer.kicherkrabbe.messaging.inbox.IncomingMessage;
import de.bennyboer.kicherkrabbe.messaging.inbox.IncomingMessageId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public abstract class MessagingInboxRepoTests {

    protected MessagingInboxRepo repo;

    protected abstract MessagingInboxRepo createRepo();

    protected abstract List<IncomingMessage> findAll();

    @BeforeEach
    void setUp() {
        repo = createRepo();
    }

    @Test
    void shouldInsertMessage() {
        // given: an incoming message
        var msg = IncomingMessage.of(IncomingMessageId.of("TEST"), Instant.parse("2023-02-22T11:15:00Z"));

        // when: inserting the message
        insert(msg);

        // then: the message is stored
        var entries = findAll();
        assertThat(entries).containsExactly(msg);
    }

    @Test
    void shouldRaiseErrorWhenTryingToInsertMessageThatIsAlreadyThere() {
        // given: an incoming message
        var msg = IncomingMessage.of(IncomingMessageId.of("TEST"), Instant.now());
        insert(msg);

        // when: trying to insert the same entry again, then: an error is raised
        assertThatThrownBy(() -> insert(msg))
                .matches(e -> e.getCause() instanceof IncomingMessageAlreadySeenException);

        // when: trying to insert a different message with another ID, then: no error is raised
        var msg2 = IncomingMessage.of(IncomingMessageId.of("TEST2"), Instant.now());
        insert(msg2);
    }

    private void insert(IncomingMessage message) {
        repo.insert(message).block();
    }

}
