package de.bennyboer.kicherkrabbe.patterns.messaging;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.eventsourcing.testing.EventListenerTest;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutbox;
import de.bennyboer.kicherkrabbe.patterns.PatternsModule;
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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Import(PatternsMessaging.class)
public class PatternsMessagingTest extends EventListenerTest {

    @MockBean
    private PatternsModule module;

    @Autowired
    public PatternsMessagingTest(
            MessagingOutbox outbox,
            ReactiveTransactionManager transactionManager
    ) {
        super(outbox, transactionManager);
    }

    @BeforeEach
    void setup() {
        when(module.allowUserToCreatePatterns(anyString())).thenReturn(Mono.empty());
        when(module.removePermissionsForUser(anyString())).thenReturn(Mono.empty());
        when(module.updatePatternInLookup(anyString())).thenReturn(Mono.empty());
        when(module.removePatternFromLookup(anyString())).thenReturn(Mono.empty());
        when(module.allowUserToManagePattern(anyString(), anyString())).thenReturn(Mono.empty());
        when(module.removePermissionsOnPattern(anyString())).thenReturn(Mono.empty());
        when(module.allowAnonymousAndSystemUsersToReadPublishedPattern(anyString())).thenReturn(Mono.empty());
        when(module.disallowAnonymousAndSystemUsersToReadPublishedPattern(anyString())).thenReturn(Mono.empty());
        when(module.removeCategoryFromPatterns(anyString(), any())).thenReturn(Flux.empty());
        when(module.markCategoryAsAvailable(anyString(), anyString())).thenReturn(Mono.empty());
        when(module.markCategoryAsUnavailable(anyString())).thenReturn(Mono.empty());
    }

    @Test
    void shouldAllowUserToCreatePatternsOnUserCreated() {
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

        // then: the user is allowed to create patterns
        verify(module, timeout(5000).times(1)).allowUserToCreatePatterns(eq("USER_ID"));
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

        // then: the permissions for the user are removed
        verify(module, timeout(5000).times(1)).removePermissionsForUser(eq("USER_ID"));
    }

    @Test
    void shouldUpdatePatternInLookupOnPatternCreatedOrUpdated() {
        // when: some pattern events are published
        send(
                AggregateType.of("PATTERN"),
                AggregateId.of("PATTERN_ID"),
                Version.of(1),
                EventName.of("CREATED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );
        send(
                AggregateType.of("PATTERN"),
                AggregateId.of("PATTERN_ID"),
                Version.of(2),
                EventName.of("PUBLISHED"),
                Version.of(1),
                Agent.system(),
                Instant.now(),
                Map.of()
        );
        send(
                AggregateType.of("PATTERN"),
                AggregateId.of("PATTERN_ID"),
                Version.of(2),
                EventName.of("UNPUBLISHED"),
                Version.of(1),
                Agent.system(),
                Instant.now(),
                Map.of()
        );
        send(
                AggregateType.of("PATTERN"),
                AggregateId.of("PATTERN_ID"),
                Version.of(2),
                EventName.of("RENAMED"),
                Version.of(1),
                Agent.system(),
                Instant.now(),
                Map.of()
        );
        send(
                AggregateType.of("PATTERN"),
                AggregateId.of("PATTERN_ID"),
                Version.of(2),
                EventName.of("ATTRIBUTION_UPDATED"),
                Version.of(1),
                Agent.system(),
                Instant.now(),
                Map.of()
        );
        send(
                AggregateType.of("PATTERN"),
                AggregateId.of("PATTERN_ID"),
                Version.of(2),
                EventName.of("CATEGORIES_UPDATED"),
                Version.of(1),
                Agent.system(),
                Instant.now(),
                Map.of()
        );
        send(
                AggregateType.of("PATTERN"),
                AggregateId.of("PATTERN_ID"),
                Version.of(2),
                EventName.of("IMAGES_UPDATED"),
                Version.of(1),
                Agent.system(),
                Instant.now(),
                Map.of()
        );
        send(
                AggregateType.of("PATTERN"),
                AggregateId.of("PATTERN_ID"),
                Version.of(2),
                EventName.of("VARIANTS_UPDATED"),
                Version.of(1),
                Agent.system(),
                Instant.now(),
                Map.of()
        );
        send(
                AggregateType.of("PATTERN"),
                AggregateId.of("PATTERN_ID"),
                Version.of(2),
                EventName.of("EXTRAS_UPDATED"),
                Version.of(1),
                Agent.system(),
                Instant.now(),
                Map.of()
        );
        send(
                AggregateType.of("PATTERN"),
                AggregateId.of("PATTERN_ID"),
                Version.of(2),
                EventName.of("DESCRIPTION_UPDATED"),
                Version.of(1),
                Agent.system(),
                Instant.now(),
                Map.of()
        );
        send(
                AggregateType.of("PATTERN"),
                AggregateId.of("PATTERN_ID"),
                Version.of(2),
                EventName.of("NUMBER_UPDATED"),
                Version.of(1),
                Agent.system(),
                Instant.now(),
                Map.of()
        );
        send(
                AggregateType.of("PATTERN"),
                AggregateId.of("PATTERN_ID"),
                Version.of(2),
                EventName.of("CATEGORY_REMOVED"),
                Version.of(1),
                Agent.system(),
                Instant.now(),
                Map.of()
        );
        send(
                AggregateType.of("PATTERN"),
                AggregateId.of("PATTERN_ID"),
                Version.of(3),
                EventName.of("DELETED"),
                Version.of(2),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the pattern is only updated on non-deleted events
        verify(module, timeout(20000).times(12)).updatePatternInLookup(eq("PATTERN_ID"));
    }

    @Test
    void shouldRemovePatternFromLookupOnPatternDeleted() {
        // when: a pattern deleted event is published
        send(
                AggregateType.of("PATTERN"),
                AggregateId.of("PATTERN_ID"),
                Version.of(1),
                EventName.of("DELETED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the pattern is removed from the lookup
        verify(module, timeout(5000).times(1)).removePatternFromLookup(eq("PATTERN_ID"));
    }

    @Test
    void shouldAllowUserToManagePatternsOnPatternCreated() {
        // when: a pattern created event is published
        send(
                AggregateType.of("PATTERN"),
                AggregateId.of("PATTERN_ID"),
                Version.of(1),
                EventName.of("CREATED"),
                Version.zero(),
                Agent.user(AgentId.of("USER_ID")),
                Instant.now(),
                Map.of()
        );

        // then: the user is allowed to manage patterns
        verify(module, timeout(5000).times(1)).allowUserToManagePattern(eq("PATTERN_ID"), eq("USER_ID"));
    }

    @Test
    void shouldRemovePermissionsOnPatternDeleted() {
        // when: a pattern deleted event is published
        send(
                AggregateType.of("PATTERN"),
                AggregateId.of("PATTERN_ID"),
                Version.of(1),
                EventName.of("DELETED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the permissions on the pattern are removed
        verify(module, timeout(5000).times(1)).removePermissionsOnPattern(eq("PATTERN_ID"));
    }

    @Test
    void shouldAllowAnonymousAndSystemUsersToReadPublishedPatternOnPatternPublished() {
        // when: a pattern published event is published
        send(
                AggregateType.of("PATTERN"),
                AggregateId.of("PATTERN_ID"),
                Version.of(1),
                EventName.of("PUBLISHED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the group is allowed to read the published pattern
        verify(module, timeout(5000).times(1)).allowAnonymousAndSystemUsersToReadPublishedPattern(eq("PATTERN_ID"));
    }

    @Test
    void shouldDisallowAnonymousAndSystemUsersToReadPublishedPatternOnPatternUnpublished() {
        // when: a pattern unpublished event is published
        send(
                AggregateType.of("PATTERN"),
                AggregateId.of("PATTERN_ID"),
                Version.of(1),
                EventName.of("UNPUBLISHED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the groups are disallowed to read the published pattern
        verify(module, timeout(5000).times(1)).disallowAnonymousAndSystemUsersToReadPublishedPattern(eq("PATTERN_ID"));
    }

    @Test
    void shouldRemoveCategoryFromPatternsOnCategoryDeleted() {
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

        // then: the category is removed from the patterns
        verify(module, timeout(5000).times(1)).removeCategoryFromPatterns(eq("CATEGORY_ID"), eq(Agent.system()));
    }

    @Test
    void shouldMarkCategoryAsAvailableOnClothingCategoryCreated() {
        // when: a category created event is published that is in the clothing group
        send(
                AggregateType.of("CATEGORY"),
                AggregateId.of("CATEGORY_ID"),
                Version.of(1),
                EventName.of("CREATED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of(
                        "name", "Dress",
                        "group", "CLOTHING"
                )
        );

        // then: the category is marked as available
        verify(module, timeout(5000).times(1)).markCategoryAsAvailable(eq("CATEGORY_ID"), eq("Dress"));
    }

    @Test
    void shouldNotMarkCategoryAvailableOnNonClothingCategoryCreated() {
        // when: a category created event is published that is not in the clothing group
        send(
                AggregateType.of("CATEGORY"),
                AggregateId.of("CATEGORY_ID"),
                Version.of(1),
                EventName.of("CREATED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of(
                        "name", "Test",
                        "group", "NONE"
                )
        );

        // then: the category is not marked as available
        verify(module, timeout(5000).times(0)).markCategoryAsAvailable(anyString(), anyString());
    }

    @Test
    void shouldMarkCategoryAsAvailableOnCategoryRenamed() {
        // when: a category renamed event is published
        send(
                AggregateType.of("CATEGORY"),
                AggregateId.of("CATEGORY_ID"),
                Version.of(1),
                EventName.of("RENAMED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of("name", "Trousers")
        );

        // then: the category is marked as available
        verify(module, timeout(5000).times(1)).markCategoryAsAvailable(eq("CATEGORY_ID"), eq("Trousers"));
    }

    @Test
    void shouldMarkCategoryAsUnavailableOnCategoryRegroupedToAGroupOtherThanClothing() {
        // when: a category regrouped event is published
        send(
                AggregateType.of("CATEGORY"),
                AggregateId.of("CATEGORY_ID"),
                Version.of(1),
                EventName.of("REGROUPED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of(
                        "name", "Dress",
                        "group", "NONE"
                )
        );

        // then: the category is marked as unavailable
        verify(module, timeout(5000).times(1)).markCategoryAsUnavailable(eq("CATEGORY_ID"));
    }

    @Test
    void shouldMarkCategoryAsAvailableOnCategoryRegroupedToClothingGroup() {
        // when: a category regrouped event is published
        send(
                AggregateType.of("CATEGORY"),
                AggregateId.of("CATEGORY_ID"),
                Version.of(1),
                EventName.of("REGROUPED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of(
                        "name", "Skirt",
                        "group", "CLOTHING"
                )
        );

        // then: the category is marked as available
        verify(module, timeout(5000).times(1)).markCategoryAsAvailable(eq("CATEGORY_ID"), eq("Skirt"));
    }

    @Test
    void shouldMarkCategoryAsUnavailableOnCategoryDeleted() {
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

        // then: the category is marked as unavailable
        verify(module, timeout(5000).times(1)).markCategoryAsUnavailable(eq("CATEGORY_ID"));
    }

}
