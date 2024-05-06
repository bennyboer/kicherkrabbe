package de.bennyboer.kicherkrabbe.colors.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.bennyboer.kicherkrabbe.colors.ColorsModule;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.eventsourcing.testing.EventListenerTest;
import de.bennyboer.kicherkrabbe.messaging.listener.MessageListenerFactory;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutbox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.ReactiveTransactionManager;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Import(ColorsMessaging.class)
public class ColorsMessagingTest extends EventListenerTest {

    @MockBean
    private ColorsModule module;

    @Autowired
    public ColorsMessagingTest(
            MessageListenerFactory factory,
            MessagingOutbox outbox,
            ReactiveTransactionManager transactionManager,
            ObjectMapper objectMapper
    ) {
        super(factory, outbox, transactionManager, objectMapper);
    }

    @BeforeEach
    void setup() {
        when(module.allowUserToCreateColors(any())).thenReturn(Mono.empty());
        when(module.removePermissionsForUser(any())).thenReturn(Mono.empty());
        when(module.updateColorInLookup(any())).thenReturn(Mono.empty());
        when(module.removeColorFromLookup(any())).thenReturn(Mono.empty());
        when(module.allowCreatorToManageColor(any(), any())).thenReturn(Mono.empty());
        when(module.removePermissionsForColor(any())).thenReturn(Mono.empty());
    }

    @Test
    void shouldAllowUserToCreateColors() {
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

        // then: the user is allowed to create colors
        verify(module, timeout(5000).times(1)).allowUserToCreateColors(eq("USER_ID"));
    }

    @Test
    void shouldRemovePermissionsForUser() {
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

        // then: the permissions for the user are removed
        verify(module, timeout(5000).times(1)).removePermissionsForUser(eq("USER_ID"));
    }

    @Test
    void shouldUpdateColorInLookupOnColorCreated() {
        // when: a color created event is published
        send(
                AggregateType.of("COLOR"),
                AggregateId.of("COLOR_ID"),
                Version.of(1),
                EventName.of("CREATED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the color is updated in the lookup
        verify(module, timeout(5000).times(1)).updateColorInLookup(eq("COLOR_ID"));
    }

    @Test
    void shouldUpdateColorInLookupOnColorUpdated() {
        // when: a color updated event is published
        send(
                AggregateType.of("COLOR"),
                AggregateId.of("COLOR_ID"),
                Version.of(1),
                EventName.of("UPDATED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the color is updated in the lookup
        verify(module, timeout(5000).times(1)).updateColorInLookup(eq("COLOR_ID"));
    }

    @Test
    void shouldRemoveColorFromLookup() {
        // when: a color deleted event is published
        send(
                AggregateType.of("COLOR"),
                AggregateId.of("COLOR_ID"),
                Version.of(1),
                EventName.of("DELETED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the color is removed from the lookup
        verify(module, timeout(5000).times(1)).removeColorFromLookup(eq("COLOR_ID"));
    }

    @Test
    void shouldAllowCreatorOfColorToManageColor() {
        // when: a color created event is published
        send(
                AggregateType.of("COLOR"),
                AggregateId.of("COLOR_ID"),
                Version.of(1),
                EventName.of("CREATED"),
                Version.zero(),
                Agent.user(AgentId.of("USER_ID")),
                Instant.now(),
                Map.of()
        );

        // then: the creator of the color is allowed to manage the color
        verify(module, timeout(5000).times(1)).allowCreatorToManageColor(eq("COLOR_ID"), eq("USER_ID"));
    }

    @Test
    void shouldRemovePermissionsOnDeletedColor() {
        // when: a color deleted event is published
        send(
                AggregateType.of("COLOR"),
                AggregateId.of("COLOR_ID"),
                Version.of(1),
                EventName.of("DELETED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the permissions for the color are removed
        verify(module, timeout(5000).times(1)).removePermissionsForColor(eq("COLOR_ID"));
    }

}
