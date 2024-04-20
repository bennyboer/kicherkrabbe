package de.bennyboer.kicherkrabbe.eventsourcing.example;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventWithMetadata;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.EventMetadata;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.LoggingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.example.events.CreatedEvent;
import de.bennyboer.kicherkrabbe.eventsourcing.example.events.CreatedEvent2;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.EventSourcingRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public abstract class SampleAggregateTests {

    private EventSourcingRepo repo;

    private final LoggingEventPublisher eventPublisher = new LoggingEventPublisher();

    private SampleAggregateService eventSourcingService = new SampleAggregateService(
            repo,
            eventPublisher
    );

    Agent testAgent = Agent.user(AgentId.of("USER_ID"));

    protected abstract EventSourcingRepo createRepo();

    @BeforeEach
    void setUp() {
        repo = createRepo();
        eventSourcingService = new SampleAggregateService(
                repo,
                eventPublisher
        );
    }

    @Test
    void shouldCreate() {
        var id = "SAMPLE_ID";

        // when: the aggregate is created
        var version = eventSourcingService.create(id, "Test title", "Test description", testAgent).block();

        // then: the retrieved aggregate has the correct title and description
        var aggregate = eventSourcingService.get(id).block();
        assertEquals(id, aggregate.getId());
        assertEquals("Test title", aggregate.getTitle());
        assertEquals("Test description", aggregate.getDescription());
        assertFalse(aggregate.isDeleted());

        // and: the version is 0
        assertEquals(Version.zero(), version);

        // and: an event has been published
        var events = eventPublisher.findEventsByName(CreatedEvent.NAME);
        assertEquals(1, events.size());
        EventWithMetadata eventWithMetadata = events.getFirst();

        var metadata = eventWithMetadata.getMetadata();
        assertThat(metadata.getAggregateId()).isEqualTo(AggregateId.of(id));
        assertThat(metadata.getAggregateType()).isEqualTo(SampleAggregate.TYPE);
        assertThat(metadata.getAggregateVersion()).isEqualTo(Version.zero());
        assertThat(metadata.getAgent().getType()).isEqualTo(AgentType.USER);
        assertThat(metadata.getAgent().getId()).isEqualTo(AgentId.of("USER_ID"));

        var event = eventWithMetadata.getEvent();
        assertThat(event).isInstanceOf(CreatedEvent2.class);
        var createdEvent = (CreatedEvent2) event;
        assertThat(createdEvent.getTitle()).isEqualTo("Test title");
        assertThat(createdEvent.getDescription()).isEqualTo("Test description");
    }

    @Test
    void shouldUpdateTitle() {
        var id = "SAMPLE_ID";

        // given: an aggregate with a title
        var initialVersion = eventSourcingService.create(id, "Test title", "Test description", testAgent).block();

        // when: the title is updated
        var version = eventSourcingService.updateTitle(id, initialVersion, "New title", testAgent).block();

        // then: the retrieved aggregate has the correct title
        var aggregate = eventSourcingService.get(id).block();

        assertThat(aggregate.getTitle()).isEqualTo("New title");
        assertThat(aggregate.getDescription()).isEqualTo("Test description");
        assertThat(aggregate.isDeleted()).isFalse();

        // and: the version is 1
        assertThat(version).isEqualTo(Version.of(1));
    }

    @Test
    void shouldUpdateDescription() {
        var id = "SAMPLE_ID";

        // given: an aggregate with a description
        var initialVersion = eventSourcingService.create(id, "Test title", "Test description", testAgent).block();

        // when: the description is updated
        var version = eventSourcingService.updateDescription(id, initialVersion, "New description", testAgent).block();

        // then: the retrieved aggregate has the correct description
        var aggregate = eventSourcingService.get(id).block();
        assertThat(aggregate.getTitle()).isEqualTo("Test title");
        assertThat(aggregate.getDescription()).isEqualTo("New description");
        assertThat(aggregate.isDeleted()).isFalse();

        // and: the version is 1
        assertThat(version).isEqualTo(Version.of(1));
    }

    @Test
    void shouldDelete() {
        var id = "SAMPLE_ID";

        // given: an aggregate
        var initialVersion = eventSourcingService.create(id, "Test title", "Test description", testAgent).block();

        // when: the aggregate is deleted
        var version = eventSourcingService.delete(id, initialVersion, testAgent).block();

        // then: the retrieved aggregate is deleted
        var aggregate = eventSourcingService.get(id).block();
        assertThat(aggregate).isNull();

        // and: the version is 1
        assertThat(version).isEqualTo(Version.of(1));
    }

    @Test
    void shouldNotBeAbleToDispatchCommandsAfterDelete() {
        var id = "SAMPLE_ID";

        // given: a deleted aggregate
        var initialVersion = eventSourcingService.create(id, "Test title", "Test description", testAgent).block();
        var version = eventSourcingService.delete(id, initialVersion, testAgent).block();

        // when: another command is dispatched
        Executable executable = () -> eventSourcingService.updateTitle(id, version, "New title", testAgent).block();

        // then: an exception is thrown
        var exception = assertThrows(IllegalStateException.class, executable);
        assertThat(exception.getMessage()).isEqualTo("Cannot apply command to deleted aggregate");
    }

    @Test
    void shouldBeAbleToRetrieveOldVersions() {
        var id = "SAMPLE_ID";

        // given: an aggregate that underwent multiple changes
        var version = eventSourcingService.create(id, "Test title", "Test description", testAgent).block();
        version = eventSourcingService.updateTitle(id, version, "New title", testAgent).block();
        version = eventSourcingService.updateDescription(id, version, "New description", testAgent).block();
        version = eventSourcingService.updateTitle(id, version, "Newer title", testAgent).block();
        version = eventSourcingService.delete(id, version, testAgent).block();

        // when: the aggregate is retrieved in an old version
        var aggregate = eventSourcingService.get(id, Version.of(2)).block();

        // then: the aggregate has the correct title and description
        assertThat(aggregate.getTitle()).isEqualTo("New title");
        assertThat(aggregate.getDescription()).isEqualTo("New description");
        assertThat(aggregate.isDeleted()).isFalse();
    }

    @Test
    void shouldBeAbleToManageMultipleAggregates() {
        var id1 = "SAMPLE_ID_1";
        var id2 = "SAMPLE_ID_2";

        // given: two aggregates
        var version1 = eventSourcingService.create(id1, "Test title 1", "Test description 1", testAgent).block();
        var version2 = eventSourcingService.create(id2, "Test title 2", "Test description 2", testAgent).block();
        version2 = eventSourcingService.updateTitle(id2, version2, "New title 2", testAgent).block();

        // when: the aggregates are retrieved
        var aggregate1 = eventSourcingService.get(id1).block();
        var aggregate2 = eventSourcingService.get(id2).block();

        // then: the aggregates have the correct titles and descriptions
        assertThat(aggregate1.getTitle()).isEqualTo("Test title 1");
        assertThat(aggregate1.getDescription()).isEqualTo("Test description 1");
        assertThat(aggregate1.isDeleted()).isFalse();

        assertThat(aggregate2.getTitle()).isEqualTo("New title 2");
        assertThat(aggregate2.getDescription()).isEqualTo("Test description 2");
        assertThat(aggregate2.isDeleted()).isFalse();

        // and: the versions are correct
        assertThat(version1).isEqualTo(Version.zero());
        assertThat(version2).isEqualTo(Version.of(1));
    }

    @Test
    void shouldSnapshotEvery100Events() {
        var id = "SAMPLE_ID";

        // given: an aggregate
        var version = eventSourcingService.create(id, "Test title", "Test description", testAgent).block();

        // when: the aggregate undergoes 300 changes
        for (int i = 0; i < 300; i++) {
            version = eventSourcingService.updateTitle(id, version, "New title " + i, testAgent).block();
        }

        // then: each 100th event is a snapshot
        var events = repo.findEventsByAggregateIdAndType(AggregateId.of(id), SampleAggregate.TYPE, Version.zero())
                .collectList()
                .block();

        assertThat(events.size()).isEqualTo(304);
        for (int i = 0; i < events.size(); i++) {
            var event = events.get(i);

            if (Set.of(100, 200, 300).contains(i)) {
                assertThat(event.getMetadata().isSnapshot()).isTrue();
            } else {
                assertThat(event.getMetadata().isSnapshot()).isFalse();
            }
        }
    }

    @Test
    void shouldPatchOldEventVersionsToTheLatestVersion() {
        var id = "SAMPLE_ID";

        // given: an aggregate that underwent an old create change
        var testDeletedAt = Instant.parse("2021-01-01T00:00:00.000Z");
        var oldEvent = CreatedEvent.of("Test title", "Test description");
        var oldEventWithMetadata = EventWithMetadata.of(oldEvent, EventMetadata.of(
                AggregateId.of(id),
                SampleAggregate.TYPE,
                Version.zero(),
                testAgent,
                Instant.now(),
                false
        ));
        repo.insert(oldEventWithMetadata).block();

        // when: the aggregate is retrieved
        var aggregate = eventSourcingService.get(id).block();

        // then: the aggregate does not have the deletedAt field set since only the new created event has it
        assertThat(aggregate.isDeleted()).isFalse();
    }

    @Test
    void shouldCollapseEvents() {
        var id = "SAMPLE_ID";

        // given: an aggregate that underwent multiple changes
        var version = eventSourcingService.create(id, "Test title", "Test description", testAgent).block();
        version = eventSourcingService.updateTitle(id, version, "New title", testAgent).block();
        version = eventSourcingService.updateDescription(id, version, "New description", testAgent).block();
        version = eventSourcingService.updateTitle(id, version, "Newer title", testAgent).block();

        // when: collapsing the events
        eventSourcingService.collapseEvents(id, version, testAgent).block();

        // then: there is only a single snapshot event in the event store
        var events = repo.findEventsByAggregateIdAndType(AggregateId.of(id), SampleAggregate.TYPE, Version.zero())
                .collectList()
                .block();
        assertThat(events.size()).isEqualTo(1);
        var event = events.get(0);
        assertThat(event.getMetadata().isSnapshot()).isTrue();
    }

}
