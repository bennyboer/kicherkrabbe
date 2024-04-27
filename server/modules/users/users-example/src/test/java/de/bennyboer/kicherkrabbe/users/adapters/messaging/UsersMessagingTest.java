package de.bennyboer.kicherkrabbe.users.adapters.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.testing.EventListenerTest;
import de.bennyboer.kicherkrabbe.messaging.listener.MessageListenerFactory;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutbox;
import de.bennyboer.kicherkrabbe.users.UsersModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.ReactiveTransactionManager;

import java.time.Instant;
import java.util.Map;

import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@Import(UsersMessaging.class)
public class UsersMessagingTest extends EventListenerTest {

    @MockBean
    private UsersModule module;

    @Autowired
    public UsersMessagingTest(
            MessageListenerFactory factory,
            MessagingOutbox outbox,
            ReactiveTransactionManager transactionManager,
            ObjectMapper objectMapper
    ) {
        super(factory, outbox, transactionManager, objectMapper);
    }

    @Test
    void shouldUpdateUsersLookupOnUserCreated() {
        // when: a user created event is published
        send(
                AggregateType.of("USER"),
                AggregateId.of("USER_ID"),
                Version.of(1),
                EventName.of("CREATED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the users lookup is updated
        verify(module, timeout(5000).times(1)).updateUserInLookup("USER_ID");
    }

    @Test
    void shouldUpdateUsersLookupOnUserDeleted() {
        // when: a user deleted event is published
        send(
                AggregateType.of("USER"),
                AggregateId.of("USER_ID"),
                Version.of(1),
                EventName.of("DELETED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the users lookup is updated
        verify(module, timeout(5000).times(1)).removeUserFromLookup("USER_ID");
    }

}
