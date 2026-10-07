package de.bennyboer.kicherkrabbe.embroideries.messaging;

import de.bennyboer.kicherkrabbe.embroideries.EmbroideriesModule;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.eventsourcing.testing.EventListenerTest;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutbox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.ReactiveTransactionManager;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Import(EmbroideriesMessaging.class)
public class EmbroideriesMessagingTest extends EventListenerTest {

    @MockitoBean
    private EmbroideriesModule module;

    @Autowired
    public EmbroideriesMessagingTest(
            MessagingOutbox outbox,
            ReactiveTransactionManager transactionManager
    ) {
        super(outbox, transactionManager);
    }

    @BeforeEach
    void setup() {
        when(module.allowUserToCreateEmbroideries(anyString())).thenReturn(Mono.empty());
        when(module.removePermissionsForUser(anyString())).thenReturn(Mono.empty());
        when(module.updateEmbroideryInLookup(anyString())).thenReturn(Mono.empty());
        when(module.removeEmbroideryFromLookup(anyString())).thenReturn(Mono.empty());
        when(module.allowUserToManageEmbroidery(anyString(), anyString())).thenReturn(Mono.empty());
        when(module.removePermissionsOnEmbroidery(anyString())).thenReturn(Mono.empty());
        when(module.allowAnonymousAndSystemUsersToReadPublishedEmbroidery(anyString())).thenReturn(Mono.empty());
        when(module.disallowAnonymousAndSystemUsersToReadPublishedEmbroidery(anyString())).thenReturn(Mono.empty());
        when(module.removeCategoryFromEmbroideries(anyString(), any())).thenReturn(Flux.empty());
        when(module.markCategoryAsAvailable(anyString(), anyString())).thenReturn(Mono.empty());
        when(module.markCategoryAsUnavailable(anyString())).thenReturn(Mono.empty());
        when(module.renameCategoryIfAvailable(anyString(), anyString())).thenReturn(Mono.empty());
    }

    @Test
    void shouldAllowUserToCreateEmbroideriesOnUserCreated() {
        // when: a user created event is published
        sendEvent("USER", "USER_ID", "CREATED", Agent.system(), Map.of());

        // then: the user is allowed to create embroideries
        verify(module, timeout(10000).times(1)).allowUserToCreateEmbroideries(eq("USER_ID"));
    }

    @Test
    void shouldRemovePermissionsForUserOnUserDeleted() {
        // when: a user deleted event is published
        sendEvent("USER", "USER_ID", "DELETED", Agent.system(), Map.of());

        // then: the permissions for the user are removed
        verify(module, timeout(10000).times(1)).removePermissionsForUser(eq("USER_ID"));
    }

    @Test
    void shouldUpdateEmbroideryInLookupOnEmbroideryEvents() {
        // when: an embroidery created event is published
        sendEvent("EMBROIDERY", "EMBROIDERY_ID_1", "CREATED", Agent.system(), Map.of());

        // then: the embroidery is updated in the lookup
        verify(module, timeout(10000).times(1)).updateEmbroideryInLookup(eq("EMBROIDERY_ID_1"));

        // when: an embroidery renamed event is published
        sendEvent("EMBROIDERY", "EMBROIDERY_ID_2", "RENAMED", Agent.system(), Map.of());

        // then: the embroidery is updated in the lookup
        verify(module, timeout(10000).times(1)).updateEmbroideryInLookup(eq("EMBROIDERY_ID_2"));

        // when: an embroidery image updated event is published
        sendEvent("EMBROIDERY", "EMBROIDERY_ID_3", "IMAGE_UPDATED", Agent.system(), Map.of());

        // then: the embroidery is updated in the lookup
        verify(module, timeout(10000).times(1)).updateEmbroideryInLookup(eq("EMBROIDERY_ID_3"));

        // when: an embroidery categories updated event is published
        sendEvent("EMBROIDERY", "EMBROIDERY_ID_4", "CATEGORIES_UPDATED", Agent.system(), Map.of());

        // then: the embroidery is updated in the lookup
        verify(module, timeout(10000).times(1)).updateEmbroideryInLookup(eq("EMBROIDERY_ID_4"));

        // when: an embroidery category removed event is published
        sendEvent("EMBROIDERY", "EMBROIDERY_ID_5", "CATEGORY_REMOVED", Agent.system(), Map.of());

        // then: the embroidery is updated in the lookup
        verify(module, timeout(10000).times(1)).updateEmbroideryInLookup(eq("EMBROIDERY_ID_5"));

        // when: an embroidery published event is published
        sendEvent("EMBROIDERY", "EMBROIDERY_ID_6", "PUBLISHED", Agent.system(), Map.of());

        // then: the embroidery is updated in the lookup
        verify(module, timeout(10000).times(1)).updateEmbroideryInLookup(eq("EMBROIDERY_ID_6"));

        // when: an embroidery unpublished event is published
        sendEvent("EMBROIDERY", "EMBROIDERY_ID_7", "UNPUBLISHED", Agent.system(), Map.of());

        // then: the embroidery is updated in the lookup
        verify(module, timeout(10000).times(1)).updateEmbroideryInLookup(eq("EMBROIDERY_ID_7"));

        // when: an embroidery featured event is published
        sendEvent("EMBROIDERY", "EMBROIDERY_ID_8", "FEATURED", Agent.system(), Map.of());

        // then: the embroidery is updated in the lookup
        verify(module, timeout(10000).times(1)).updateEmbroideryInLookup(eq("EMBROIDERY_ID_8"));

        // when: an embroidery unfeatured event is published
        sendEvent("EMBROIDERY", "EMBROIDERY_ID_9", "UNFEATURED", Agent.system(), Map.of());

        // then: the embroidery is updated in the lookup
        verify(module, timeout(10000).times(1)).updateEmbroideryInLookup(eq("EMBROIDERY_ID_9"));
    }

    @Test
    void shouldRemoveEmbroideryFromLookupAndRemovePermissionsOnEmbroideryDeleted() {
        // when: an embroidery deleted event is published
        sendEvent("EMBROIDERY", "EMBROIDERY_ID", "DELETED", Agent.system(), Map.of());

        // then: the embroidery is removed from the lookup
        verify(module, timeout(10000).times(1)).removeEmbroideryFromLookup(eq("EMBROIDERY_ID"));

        // and: the permissions on the embroidery are removed
        verify(module, timeout(10000).times(1)).removePermissionsOnEmbroidery(eq("EMBROIDERY_ID"));

        // and: the deleted embroidery is not updated in the lookup
        verify(module, after(2000).never()).updateEmbroideryInLookup(anyString());
    }

    @Test
    void shouldAllowUserToManageEmbroideryOnEmbroideryCreated() {
        // when: an embroidery created event is published by a user
        sendEvent("EMBROIDERY", "EMBROIDERY_ID", "CREATED", Agent.user(AgentId.of("USER_ID")), Map.of());

        // then: the user is allowed to manage the embroidery
        verify(module, timeout(10000).times(1)).allowUserToManageEmbroidery(eq("EMBROIDERY_ID"), eq("USER_ID"));
    }

    @Test
    void shouldAllowAnonymousAndSystemUsersToReadPublishedEmbroideryOnEmbroideryPublished() {
        // when: an embroidery published event is published
        sendEvent("EMBROIDERY", "EMBROIDERY_ID", "PUBLISHED", Agent.system(), Map.of());

        // then: anonymous and system users are allowed to read the published embroidery
        verify(module, timeout(10000).times(1))
                .allowAnonymousAndSystemUsersToReadPublishedEmbroidery(eq("EMBROIDERY_ID"));
    }

    @Test
    void shouldDisallowAnonymousAndSystemUsersToReadPublishedEmbroideryOnEmbroideryUnpublished() {
        // when: an embroidery unpublished event is published
        sendEvent("EMBROIDERY", "EMBROIDERY_ID", "UNPUBLISHED", Agent.system(), Map.of());

        // then: anonymous and system users are no longer allowed to read the published embroidery
        verify(module, timeout(10000).times(1))
                .disallowAnonymousAndSystemUsersToReadPublishedEmbroidery(eq("EMBROIDERY_ID"));
    }

    @Test
    void shouldRemoveCategoryAndMarkItUnavailableOnCategoryDeleted() {
        // when: a category deleted event is published
        sendEvent("CATEGORY", "CATEGORY_ID", "DELETED", Agent.system(), Map.of());

        // then: the category is removed from the embroideries
        verify(module, timeout(10000).times(1)).removeCategoryFromEmbroideries(eq("CATEGORY_ID"), eq(Agent.system()));

        // and: the category is marked as unavailable
        verify(module, timeout(10000).times(1)).markCategoryAsUnavailable(eq("CATEGORY_ID"));
    }

    @Test
    void shouldMarkCategoryAsAvailableOnEmbroideryCategoryCreated() {
        // when: a category created event is published that is in the embroidery group
        sendEvent("CATEGORY", "CATEGORY_ID", "CREATED", Agent.system(), Map.of(
                "name", "Animals",
                "group", "EMBROIDERY"
        ));

        // then: the category is marked as available
        verify(module, timeout(10000).times(1)).markCategoryAsAvailable(eq("CATEGORY_ID"), eq("Animals"));
    }

    @Test
    void shouldNotMarkCategoryAsAvailableOnNonEmbroideryCategoryCreated() {
        // when: a category created event is published that is in the clothing group
        sendEvent("CATEGORY", "CATEGORY_ID", "CREATED", Agent.system(), Map.of(
                "name", "Dress",
                "group", "CLOTHING"
        ));

        // then: the category is not marked as available
        verify(module, after(2000).never()).markCategoryAsAvailable(anyString(), anyString());
    }

    @Test
    void shouldRenameCategoryIfAvailableOnCategoryRenamed() {
        // when: a category renamed event is published
        sendEvent("CATEGORY", "CATEGORY_ID", "RENAMED", Agent.system(), Map.of("name", "Wild animals"));

        // then: the category is renamed if it is available
        verify(module, timeout(10000).times(1)).renameCategoryIfAvailable(eq("CATEGORY_ID"), eq("Wild animals"));

        // and: the category is not blindly marked as available
        verify(module, after(2000).never()).markCategoryAsAvailable(anyString(), anyString());
    }

    @Test
    void shouldMarkCategoryAsAvailableOnCategoryRegroupedToEmbroideryGroup() {
        // when: a category is regrouped to the embroidery group
        sendEvent("CATEGORY", "CATEGORY_ID", "REGROUPED", Agent.system(), Map.of(
                "name", "Animals",
                "group", "EMBROIDERY"
        ));

        // then: the category is marked as available
        verify(module, timeout(10000).times(1)).markCategoryAsAvailable(eq("CATEGORY_ID"), eq("Animals"));

        // and: the category is not marked as unavailable
        verify(module, after(2000).never()).markCategoryAsUnavailable(anyString());
    }

    @Test
    void shouldMarkCategoryAsUnavailableOnCategoryRegroupedToAnotherGroup() {
        // when: a category is regrouped to another group than embroidery
        sendEvent("CATEGORY", "CATEGORY_ID", "REGROUPED", Agent.system(), Map.of(
                "name", "Animals",
                "group", "NONE"
        ));

        // then: the category is marked as unavailable
        verify(module, timeout(10000).times(1)).markCategoryAsUnavailable(eq("CATEGORY_ID"));

        // and: the category is not marked as available
        verify(module, after(2000).never()).markCategoryAsAvailable(anyString(), anyString());
    }

    private void sendEvent(
            String aggregateType,
            String aggregateId,
            String eventName,
            Agent agent,
            Map<String, Object> payload
    ) {
        send(
                AggregateType.of(aggregateType),
                AggregateId.of(aggregateId),
                Version.of(1),
                EventName.of(eventName),
                Version.zero(),
                agent,
                Instant.now(),
                payload
        );
    }

}
