package de.bennyboer.kicherkrabbe.categories.messaging;

import de.bennyboer.kicherkrabbe.categories.CategoriesModule;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.ReactiveTransactionManager;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Import(CategoriesMessaging.class)
public class CategoriesMessagingTest extends EventListenerTest {

    @MockitoBean
    private CategoriesModule module;

    @Autowired
    public CategoriesMessagingTest(
            MessagingOutbox outbox,
            ReactiveTransactionManager transactionManager
    ) {
        super(outbox, transactionManager);
    }

    @BeforeEach
    void setup() {
        when(module.allowUserToCreateCategories(anyString())).thenReturn(Mono.empty());
        when(module.removePermissionsForUser(anyString())).thenReturn(Mono.empty());
        when(module.updateCategoryInLookup(anyString())).thenReturn(Mono.empty());
        when(module.removeCategoryFromLookup(anyString())).thenReturn(Mono.empty());
        when(module.allowUserToManageCategory(anyString(), anyString())).thenReturn(Mono.empty());
        when(module.removePermissionsForCategory(anyString())).thenReturn(Mono.empty());
    }

    @Test
    void shouldAllowUserToCreateCategoriesOnUserCreated() {
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

        // then: the user is allowed to create categories
        verify(module, timeout(5000).times(1)).allowUserToCreateCategories(eq("USER_ID"));
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
    void shouldUpdateCategoryInLookupOnCategoryCreated() {
        // when: a category created event is published
        send(
                AggregateType.of("CATEGORY"),
                AggregateId.of("CATEGORY_ID"),
                Version.of(1),
                EventName.of("CREATED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the category is updated in the lookup
        verify(module, timeout(5000).times(1)).updateCategoryInLookup(eq("CATEGORY_ID"));
    }

    @Test
    void shouldUpdateCategoryInLookupOnCategoryRenamed() {
        // when: a category updated event is published
        send(
                AggregateType.of("CATEGORY"),
                AggregateId.of("CATEGORY_ID"),
                Version.of(1),
                EventName.of("RENAMED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the category is updated in the lookup
        verify(module, timeout(5000).times(1)).updateCategoryInLookup(eq("CATEGORY_ID"));
    }

    @Test
    void shouldUpdateCategoryInLookupOnCategoryRegrouped() {
        // when: a category updated event is published
        send(
                AggregateType.of("CATEGORY"),
                AggregateId.of("CATEGORY_ID"),
                Version.of(1),
                EventName.of("REGROUPED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the category is updated in the lookup
        verify(module, timeout(5000).times(1)).updateCategoryInLookup(eq("CATEGORY_ID"));
    }

    @Test
    void shouldRemoveCategoryFromLookupOnCategoryDeleted() {
        // when: a category deleted event is published
        send(
                AggregateType.of("CATEGORY"),
                AggregateId.of("CATEGORY_ID"),
                Version.of(1),
                EventName.of("DELETED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the category is removed from the lookup
        verify(module, timeout(5000).times(1)).removeCategoryFromLookup(eq("CATEGORY_ID"));
    }

    @Test
    void shouldAllowUserToManageCategoryOnCategoryCreated() {
        // when: a category created event is published
        send(
                AggregateType.of("CATEGORY"),
                AggregateId.of("CATEGORY_ID"),
                Version.of(1),
                EventName.of("CREATED"),
                Version.zero(),
                Agent.user(AgentId.of("USER_ID")),
                Instant.now(),
                Map.of()
        );

        // then: the user is allowed to manage the category
        verify(module, timeout(5000).times(1)).allowUserToManageCategory(eq("CATEGORY_ID"), eq("USER_ID"));
    }

    @Test
    void shouldRemovePermissionsForCategoryOnCategoryDeleted() {
        // when: a category deleted event is published
        send(
                AggregateType.of("CATEGORY"),
                AggregateId.of("CATEGORY_ID"),
                Version.of(1),
                EventName.of("DELETED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the categories permissions are removed
        verify(module, timeout(5000).times(1)).removePermissionsForCategory(eq("CATEGORY_ID"));
    }

}
