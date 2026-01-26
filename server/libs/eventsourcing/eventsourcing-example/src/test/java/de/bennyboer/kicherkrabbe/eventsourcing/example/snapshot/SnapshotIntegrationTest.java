package de.bennyboer.kicherkrabbe.eventsourcing.example.snapshot;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventWithMetadata;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.EventMetadata;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.LoggingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.event.snapshot.SnapshotEvent;
import de.bennyboer.kicherkrabbe.eventsourcing.example.SampleAggregate;
import de.bennyboer.kicherkrabbe.eventsourcing.example.SampleAggregateService;
import de.bennyboer.kicherkrabbe.eventsourcing.example.events.CreatedEvent2;
import de.bennyboer.kicherkrabbe.eventsourcing.example.events.TitleUpdatedEvent;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.inmemory.InMemoryEventSourcingRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class SnapshotIntegrationTest {

    private EventSourcingRepo repo;

    private final LoggingEventPublisher eventPublisher = new LoggingEventPublisher();

    private SampleAggregateService service;

    private final Agent testAgent = Agent.user(AgentId.of("USER_ID"));

    @BeforeEach
    void setUp() {
        repo = new InMemoryEventSourcingRepo();
        service = new SampleAggregateService(repo, eventPublisher, Clock.systemUTC());
    }

    @Test
    void shouldCreateSnapshotEveryHundredEvents() {
        var id = "SAMPLE_ID";

        var version = service.create(id, "Test title", "Test description", testAgent).block();

        for (int i = 0; i < 300; i++) {
            version = service.updateTitle(id, version, "New title " + i, testAgent).block();
        }

        var events = repo.findEventsByAggregateIdAndType(
                AggregateId.of(id),
                SampleAggregate.TYPE,
                Version.zero()
        ).collectList().block();

        assertThat(events).hasSize(304);

        var snapshotVersions = events.stream()
                .filter(e -> e.getMetadata().isSnapshot())
                .map(e -> e.getMetadata().getAggregateVersion().getValue())
                .toList();

        assertThat(snapshotVersions).containsExactly(100L, 200L, 300L);
    }

    @Test
    void shouldCreateSnapshotAtExactlyVersion100() {
        var id = "SAMPLE_ID";

        var version = service.create(id, "Test title", "Test description", testAgent).block();

        for (int i = 0; i < 99; i++) {
            version = service.updateTitle(id, version, "New title " + i, testAgent).block();
        }

        var events = repo.findEventsByAggregateIdAndType(
                AggregateId.of(id),
                SampleAggregate.TYPE,
                Version.zero()
        ).collectList().block();

        assertThat(events).hasSize(101);

        var snapshotCount = events.stream()
                .filter(e -> e.getMetadata().isSnapshot())
                .count();
        assertThat(snapshotCount).isEqualTo(1);

        var snapshotEvent = events.stream()
                .filter(e -> e.getMetadata().isSnapshot())
                .findFirst()
                .orElseThrow();
        assertThat(snapshotEvent.getMetadata().getAggregateVersion()).isEqualTo(Version.of(100));
    }

    @Test
    void shouldRecoverAggregateWhenNoSnapshotsExist() {
        var id = "SAMPLE_ID";

        insertEventWithoutSnapshot(id, CreatedEvent2.of("Initial", "Description", null), Version.zero());

        for (int i = 1; i <= 150; i++) {
            insertEventWithoutSnapshot(id, TitleUpdatedEvent.of("Title " + i), Version.of(i));
        }

        var aggregate = service.get(id).block();

        assertThat(aggregate).isNotNull();
        assertThat(aggregate.getId()).isEqualTo(id);
        assertThat(aggregate.getTitle()).isEqualTo("Title 150");
        assertThat(aggregate.getDescription()).isEqualTo("Description");
    }

    @Test
    void shouldRecoverAggregateFromManyEventsWithoutSnapshots() {
        var id = "SAMPLE_ID";

        insertEventWithoutSnapshot(id, CreatedEvent2.of("Initial", "Description", null), Version.zero());

        for (int i = 1; i <= 500; i++) {
            insertEventWithoutSnapshot(id, TitleUpdatedEvent.of("Title " + i), Version.of(i));
        }

        var aggregate = service.get(id).block();

        assertThat(aggregate).isNotNull();
        assertThat(aggregate.getTitle()).isEqualTo("Title 500");

        var nextVersion = service.updateTitle(id, Version.of(500), "After recovery", testAgent).block();
        assertThat(nextVersion).isEqualTo(Version.of(502));
    }

    @Test
    void shouldHandleSnapshotWithMissingField() {
        var id = "SAMPLE_ID";

        Map<String, Object> snapshotState = new HashMap<>();
        snapshotState.put("title", "Snapshot Title");
        snapshotState.put("createdAt", Instant.now().toString());

        var snapshotEvent = SnapshotEvent.of(snapshotState);
        var metadata = EventMetadata.of(
                AggregateId.of(id),
                SampleAggregate.TYPE,
                Version.zero(),
                testAgent,
                Instant.now(),
                true
        );

        repo.insert(EventWithMetadata.of(snapshotEvent, metadata)).block();

        var aggregate = service.get(id).block();

        assertThat(aggregate).isNotNull();
        assertThat(aggregate.getId()).isEqualTo(id);
        assertThat(aggregate.getTitle()).isEqualTo("Snapshot Title");
        assertThat(aggregate.getDescription()).isNull();
    }

    @Test
    void shouldHandleSnapshotWithExtraField() {
        var id = "SAMPLE_ID";

        Map<String, Object> snapshotState = new HashMap<>();
        snapshotState.put("title", "Snapshot Title");
        snapshotState.put("description", "Snapshot Description");
        snapshotState.put("createdAt", Instant.now().toString());
        snapshotState.put("legacyField", "This field no longer exists in the aggregate");
        snapshotState.put("anotherRemovedField", 12345);
        snapshotState.put("removedNestedObject", Map.of("key", "value"));

        var snapshotEvent = SnapshotEvent.of(snapshotState);
        var metadata = EventMetadata.of(
                AggregateId.of(id),
                SampleAggregate.TYPE,
                Version.zero(),
                testAgent,
                Instant.now(),
                true
        );

        repo.insert(EventWithMetadata.of(snapshotEvent, metadata)).block();

        var aggregate = service.get(id).block();

        assertThat(aggregate).isNotNull();
        assertThat(aggregate.getId()).isEqualTo(id);
        assertThat(aggregate.getTitle()).isEqualTo("Snapshot Title");
        assertThat(aggregate.getDescription()).isEqualTo("Snapshot Description");
    }

    @Test
    void shouldHandleSnapshotWithBothMissingAndExtraFields() {
        var id = "SAMPLE_ID";

        Map<String, Object> snapshotState = new HashMap<>();
        snapshotState.put("title", "Snapshot Title");
        snapshotState.put("createdAt", Instant.now().toString());
        snapshotState.put("legacyField", "This field no longer exists");

        var snapshotEvent = SnapshotEvent.of(snapshotState);
        var metadata = EventMetadata.of(
                AggregateId.of(id),
                SampleAggregate.TYPE,
                Version.zero(),
                testAgent,
                Instant.now(),
                true
        );

        repo.insert(EventWithMetadata.of(snapshotEvent, metadata)).block();

        var aggregate = service.get(id).block();

        assertThat(aggregate).isNotNull();
        assertThat(aggregate.getId()).isEqualTo(id);
        assertThat(aggregate.getTitle()).isEqualTo("Snapshot Title");
        assertThat(aggregate.getDescription()).isNull();
    }

    @Test
    void shouldApplyEventsAfterRestoringFromSnapshot() {
        var id = "SAMPLE_ID";

        Map<String, Object> snapshotState = new HashMap<>();
        snapshotState.put("title", "Snapshot Title");
        snapshotState.put("description", "Snapshot Description");
        snapshotState.put("createdAt", Instant.now().toString());

        var snapshotEvent = SnapshotEvent.of(snapshotState);
        var snapshotMetadata = EventMetadata.of(
                AggregateId.of(id),
                SampleAggregate.TYPE,
                Version.of(50),
                testAgent,
                Instant.now(),
                true
        );
        repo.insert(EventWithMetadata.of(snapshotEvent, snapshotMetadata)).block();

        var version = service.updateTitle(id, Version.of(50), "Updated After Snapshot", testAgent).block();
        assertThat(version).isEqualTo(Version.of(51));

        var aggregate = service.get(id).block();

        assertThat(aggregate).isNotNull();
        assertThat(aggregate.getTitle()).isEqualTo("Updated After Snapshot");
        assertThat(aggregate.getDescription()).isEqualTo("Snapshot Description");
    }

    @Test
    void shouldHandleSnapshotWithWrongFieldTypes() {
        var id = "SAMPLE_ID";

        Map<String, Object> snapshotState = new HashMap<>();
        snapshotState.put("title", 12345);
        snapshotState.put("description", true);
        snapshotState.put("createdAt", Instant.now().toString());

        var snapshotEvent = SnapshotEvent.of(snapshotState);
        var metadata = EventMetadata.of(
                AggregateId.of(id),
                SampleAggregate.TYPE,
                Version.zero(),
                testAgent,
                Instant.now(),
                true
        );

        repo.insert(EventWithMetadata.of(snapshotEvent, metadata)).block();

        var aggregate = service.get(id).block();

        assertThat(aggregate).isNotNull();
        assertThat(aggregate.getTitle()).isEqualTo("12345");
        assertThat(aggregate.getDescription()).isEqualTo("true");
    }

    @Test
    void shouldHandleEmptySnapshot() {
        var id = "SAMPLE_ID";

        Map<String, Object> snapshotState = new HashMap<>();
        snapshotState.put("createdAt", Instant.now().toString());

        var snapshotEvent = SnapshotEvent.of(snapshotState);
        var metadata = EventMetadata.of(
                AggregateId.of(id),
                SampleAggregate.TYPE,
                Version.zero(),
                testAgent,
                Instant.now(),
                true
        );

        repo.insert(EventWithMetadata.of(snapshotEvent, metadata)).block();

        var aggregate = service.get(id).block();

        assertThat(aggregate).isNotNull();
        assertThat(aggregate.getId()).isEqualTo(id);
        assertThat(aggregate.getTitle()).isNull();
        assertThat(aggregate.getDescription()).isNull();
    }

    @Test
    void shouldUseLatestSnapshotWhenMultipleExist() {
        var id = "SAMPLE_ID";

        var version = service.create(id, "Initial", "Initial description", testAgent).block();

        for (int i = 0; i < 250; i++) {
            version = service.updateTitle(id, version, "Title " + i, testAgent).block();
        }

        var events = repo.findEventsByAggregateIdAndType(
                AggregateId.of(id),
                SampleAggregate.TYPE,
                Version.zero()
        ).collectList().block();

        var snapshots = events.stream()
                .filter(e -> e.getMetadata().isSnapshot())
                .toList();
        assertThat(snapshots).hasSize(2);

        var aggregate = service.get(id).block();

        assertThat(aggregate).isNotNull();
        assertThat(aggregate.getTitle()).isEqualTo("Title 249");
    }

    @Test
    void shouldRetrieveOldVersionEvenWithSnapshots() {
        var id = "SAMPLE_ID";

        var version = service.create(id, "Initial", "Initial description", testAgent).block();

        for (int i = 0; i < 150; i++) {
            version = service.updateTitle(id, version, "Title " + i, testAgent).block();
        }

        var aggregateAtVersion50 = service.get(id, Version.of(50)).block();
        assertThat(aggregateAtVersion50).isNotNull();
        assertThat(aggregateAtVersion50.getTitle()).isEqualTo("Title 49");

        var aggregateAtVersion120 = service.get(id, Version.of(120)).block();
        assertThat(aggregateAtVersion120).isNotNull();
        assertThat(aggregateAtVersion120.getTitle()).isEqualTo("Title 118");
    }

    @Test
    void shouldApplyEventsOnTopOfSnapshotCorrectly() {
        var id = "SAMPLE_ID";

        Map<String, Object> snapshotState = new HashMap<>();
        snapshotState.put("title", "Snapshot Title");
        snapshotState.put("description", "Snapshot Description");
        snapshotState.put("createdAt", Instant.now().toString());

        var snapshotEvent = SnapshotEvent.of(snapshotState);
        var snapshotMetadata = EventMetadata.of(
                AggregateId.of(id),
                SampleAggregate.TYPE,
                Version.of(100),
                testAgent,
                Instant.now(),
                true
        );
        repo.insert(EventWithMetadata.of(snapshotEvent, snapshotMetadata)).block();

        for (int i = 101; i <= 110; i++) {
            insertEventWithoutSnapshot(id, TitleUpdatedEvent.of("Title " + i), Version.of(i));
        }

        var aggregate = service.get(id).block();

        assertThat(aggregate).isNotNull();
        assertThat(aggregate.getTitle()).isEqualTo("Title 110");
        assertThat(aggregate.getDescription()).isEqualTo("Snapshot Description");
    }

    @Test
    void shouldContinueCreatingSnapshotsAfterManualEventsWithoutSnapshots() {
        var id = "SAMPLE_ID";

        insertEventWithoutSnapshot(id, CreatedEvent2.of("Initial", "Description", null), Version.zero());

        for (int i = 1; i <= 50; i++) {
            insertEventWithoutSnapshot(id, TitleUpdatedEvent.of("Title " + i), Version.of(i));
        }

        var version = Version.of(50);
        for (int i = 51; i <= 150; i++) {
            version = service.updateTitle(id, version, "Title " + i, testAgent).block();
        }

        var events = repo.findEventsByAggregateIdAndType(
                AggregateId.of(id),
                SampleAggregate.TYPE,
                Version.zero()
        ).collectList().block();

        var snapshotVersions = events.stream()
                .filter(e -> e.getMetadata().isSnapshot())
                .map(e -> e.getMetadata().getAggregateVersion().getValue())
                .toList();

        assertThat(snapshotVersions).contains(100L);
    }

    @Test
    void shouldRestoreFromSnapshotAtSpecificVersion() {
        var id = "SAMPLE_ID";

        Map<String, Object> snapshotState = new HashMap<>();
        snapshotState.put("title", "At Version 50");
        snapshotState.put("description", "Description");
        snapshotState.put("createdAt", Instant.now().toString());

        var snapshotEvent = SnapshotEvent.of(snapshotState);
        var snapshotMetadata = EventMetadata.of(
                AggregateId.of(id),
                SampleAggregate.TYPE,
                Version.of(50),
                testAgent,
                Instant.now(),
                true
        );
        repo.insert(EventWithMetadata.of(snapshotEvent, snapshotMetadata)).block();

        for (int i = 51; i <= 60; i++) {
            insertEventWithoutSnapshot(id, TitleUpdatedEvent.of("Title " + i), Version.of(i));
        }

        var aggregateAtSnapshot = service.get(id, Version.of(50)).block();
        assertThat(aggregateAtSnapshot).isNotNull();
        assertThat(aggregateAtSnapshot.getTitle()).isEqualTo("At Version 50");

        var aggregateAtVersion55 = service.get(id, Version.of(55)).block();
        assertThat(aggregateAtVersion55).isNotNull();
        assertThat(aggregateAtVersion55.getTitle()).isEqualTo("Title 55");

        var latestAggregate = service.get(id).block();
        assertThat(latestAggregate).isNotNull();
        assertThat(latestAggregate.getTitle()).isEqualTo("Title 60");
    }

    private void insertEventWithoutSnapshot(String id, de.bennyboer.kicherkrabbe.eventsourcing.event.Event event, Version version) {
        var metadata = EventMetadata.of(
                AggregateId.of(id),
                SampleAggregate.TYPE,
                version,
                testAgent,
                Instant.now(),
                false
        );
        repo.insert(EventWithMetadata.of(event, metadata)).block();
    }

    @Test
    void shouldNotCreateSnapshotsWhenAutoSnapshotIsDisabled() {
        var noSnapshotRepo = new InMemoryEventSourcingRepo();
        var noSnapshotService = new NoAutoSnapshotAggregateService(noSnapshotRepo, eventPublisher, Clock.systemUTC());

        var id = "NO_SNAPSHOT_ID";

        var version = noSnapshotService.create(id, "Test title", "Test description", testAgent).block();

        for (int i = 0; i < 300; i++) {
            version = noSnapshotService.updateTitle(id, version, "New title " + i, testAgent).block();
        }

        var events = noSnapshotRepo.findEventsByAggregateIdAndType(
                AggregateId.of(id),
                NoAutoSnapshotAggregate.TYPE,
                Version.zero()
        ).collectList().block();

        assertThat(events).hasSize(301);

        var snapshotCount = events.stream()
                .filter(e -> e.getMetadata().isSnapshot())
                .count();

        assertThat(snapshotCount).isZero();
    }

}
