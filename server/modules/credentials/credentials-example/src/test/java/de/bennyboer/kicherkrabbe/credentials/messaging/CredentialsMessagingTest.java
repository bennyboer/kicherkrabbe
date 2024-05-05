package de.bennyboer.kicherkrabbe.credentials.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.bennyboer.kicherkrabbe.credentials.CredentialsModule;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.testing.EventListenerTest;
import de.bennyboer.kicherkrabbe.messaging.listener.MessageListenerFactory;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutbox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.ReactiveTransactionManager;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Import(CredentialsMessaging.class)
public class CredentialsMessagingTest extends EventListenerTest {

    @MockBean
    private CredentialsModule module;

    @Autowired
    public CredentialsMessagingTest(
            MessageListenerFactory factory,
            MessagingOutbox outbox,
            ReactiveTransactionManager transactionManager,
            ObjectMapper objectMapper
    ) {
        super(factory, outbox, transactionManager, objectMapper);
    }

    @BeforeEach
    void setup() {
        when(module.updateCredentialsInLookup(any())).thenReturn(Mono.empty());
        when(module.addPermissions(any(), any())).thenReturn(Mono.empty());
        when(module.removeCredentialsFromLookup(any())).thenReturn(Mono.empty());
        when(module.removePermissionsOnCredentials(any())).thenReturn(Mono.empty());
        when(module.createCredentials(any(), any(), any(), any())).thenReturn(Mono.empty());
        when(module.deleteCredentialsByUserId(any(), any())).thenReturn(Flux.empty());
    }

    @Test
    void shouldUpdateCredentialsLookupOnCredentialsCreated() {
        // when: a credentials created event is published
        send(
                AggregateType.of("CREDENTIALS"),
                AggregateId.of("CREDENTIALS_ID"),
                Version.of(1),
                EventName.of("CREATED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the credentials lookup is updated
        verify(module, timeout(5000).times(1)).updateCredentialsInLookup(eq("CREDENTIALS_ID"));
    }

    @Test
    void shouldAddPermissionsOnCredentialsCreated() {
        // when: a credentials created event is published
        send(
                AggregateType.of("CREDENTIALS"),
                AggregateId.of("CREDENTIALS_ID"),
                Version.of(1),
                EventName.of("CREATED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of(
                        "userId", "USER_ID"
                )
        );

        // then: the permissions are added
        verify(module, timeout(5000).times(1)).addPermissions(eq("CREDENTIALS_ID"), eq("USER_ID"));
    }

    @Test
    void shouldUpdateCredentialsLookupOnCredentialsDeleted() {
        // when: a credentials deleted event is published
        send(
                AggregateType.of("CREDENTIALS"),
                AggregateId.of("CREDENTIALS_ID"),
                Version.of(1),
                EventName.of("DELETED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the credentials lookup is updated
        verify(module, timeout(5000).times(1)).removeCredentialsFromLookup(eq("CREDENTIALS_ID"));
    }

    @Test
    void shouldRemovePermissionsLookupOnCredentialsDeleted() {
        // when: a credentials deleted event is published
        send(
                AggregateType.of("CREDENTIALS"),
                AggregateId.of("CREDENTIALS_ID"),
                Version.of(1),
                EventName.of("DELETED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the credentials lookup is updated
        verify(module, timeout(5000).times(1)).removePermissionsOnCredentials(eq("CREDENTIALS_ID"));
    }

    @Test
    void shouldCreateCredentialsOnUserCreated() {
        // when: a user created event is published
        send(
                AggregateType.of("USER"),
                AggregateId.of("USER_ID"),
                Version.of(1),
                EventName.of("CREATED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of(
                        "mail", "test@kicherkrabbe.com"
                )
        );

        // then: the credentials are created
        verify(module, timeout(5000).times(1)).createCredentials(
                eq("test@kicherkrabbe.com"),
                any(),
                eq("USER_ID"),
                any()
        );
    }

    @Test
    void shouldDeleteCredentialsOnUserDeleted() {
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

        // then: the credentials are deleted by the user ID
        verify(module, timeout(5000).times(1)).deleteCredentialsByUserId(eq("USER_ID"), any());
    }

}
