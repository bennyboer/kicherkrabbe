package de.bennyboer.kicherkrabbe.topics.messaging;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.eventsourcing.testing.EventListenerTest;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutbox;
import de.bennyboer.kicherkrabbe.topics.TopicsModule;
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

@Import(TopicsMessaging.class)
public class TopicsMessagingTest extends EventListenerTest {

    @MockBean
    private TopicsModule module;

    @Autowired
    public TopicsMessagingTest(
            MessagingOutbox outbox,
            ReactiveTransactionManager transactionManager
    ) {
        super(outbox, transactionManager);
    }

    @BeforeEach
    void setup() {
        when(module.allowUserToCreateTopics(anyString())).thenReturn(Mono.empty());
        when(module.removePermissionsForUser(anyString())).thenReturn(Mono.empty());
        when(module.updateTopicInLookup(anyString())).thenReturn(Mono.empty());
        when(module.removeTopicFromLookup(anyString())).thenReturn(Mono.empty());
        when(module.allowUserToManageTopic(anyString(), anyString())).thenReturn(Mono.empty());
        when(module.removePermissionsForTopic(anyString())).thenReturn(Mono.empty());
    }

    @Test
    void shouldAllowUserToCreateTopicsOnUserCreated() {
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

        // then: the user is allowed to create topics
        verify(module, timeout(5000).times(1)).allowUserToCreateTopics(eq("USER_ID"));
    }

    @Test
    void shouldRemovePermissionsForUserOnUserDeleted() {
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

        // then: the users permissions are removed
        verify(module, timeout(5000).times(1)).removePermissionsForUser(eq("USER_ID"));
    }

    @Test
    void shouldUpdateTopicInLookupOnTopicCreated() {
        // when: a topic created event is published
        send(
                AggregateType.of("TOPIC"),
                AggregateId.of("TOPIC_ID"),
                Version.of(1),
                EventName.of("CREATED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the topic is updated in the lookup
        verify(module, timeout(5000).times(1)).updateTopicInLookup(eq("TOPIC_ID"));
    }

    @Test
    void shouldUpdateTopicInLookupOnTopicUpdated() {
        // when: a topic updated event is published
        send(
                AggregateType.of("TOPIC"),
                AggregateId.of("TOPIC_ID"),
                Version.of(1),
                EventName.of("UPDATED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the topic is updated in the lookup
        verify(module, timeout(5000).times(1)).updateTopicInLookup(eq("TOPIC_ID"));
    }

    @Test
    void shouldRemoveTopicFromLookupOnTopicDeleted() {
        // when: a topic deleted event is published
        send(
                AggregateType.of("TOPIC"),
                AggregateId.of("TOPIC_ID"),
                Version.of(1),
                EventName.of("DELETED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the topic is removed from the lookup
        verify(module, timeout(5000).times(1)).removeTopicFromLookup(eq("TOPIC_ID"));
    }

    @Test
    void shouldAllowUserToManageTopicOnTopicCreated() {
        // when: a topic created event is published
        send(
                AggregateType.of("TOPIC"),
                AggregateId.of("TOPIC_ID"),
                Version.of(1),
                EventName.of("CREATED"),
                Version.zero(),
                Agent.user(AgentId.of("USER_ID")),
                Instant.now(),
                Map.of()
        );

        // then: the user is allowed to manage the topic
        verify(module, timeout(5000).times(1)).allowUserToManageTopic(eq("TOPIC_ID"), eq("USER_ID"));
    }

    @Test
    void shouldRemovePermissionsForTopicOnTopicDeleted() {
        // when: a topic deleted event is published
        send(
                AggregateType.of("TOPIC"),
                AggregateId.of("TOPIC_ID"),
                Version.of(1),
                EventName.of("DELETED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the topics permissions are removed
        verify(module, timeout(5000).times(1)).removePermissionsForTopic(eq("TOPIC_ID"));
    }

}
