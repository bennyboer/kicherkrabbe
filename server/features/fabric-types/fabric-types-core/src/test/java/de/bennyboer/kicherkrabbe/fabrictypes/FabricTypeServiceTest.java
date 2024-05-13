package de.bennyboer.kicherkrabbe.fabrictypes;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.LoggingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.inmemory.InMemoryEventSourcingRepo;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class FabricTypeServiceTest {

    private final EventSourcingRepo repo = new InMemoryEventSourcingRepo();

    private final LoggingEventPublisher eventPublisher = new LoggingEventPublisher();

    private final FabricTypeService fabricTypeService = new FabricTypeService(repo, eventPublisher);

    @Test
    void shouldCreateFabricType() {
        // given: a name to create a fabric type for
        var name = FabricTypeName.of("Cotton");

        // when: creating a fabric type
        var id = create(name);

        // then: the fabric type is created
        var fabricType = get(id);
        assertThat(fabricType.getId()).isEqualTo(id);
        assertThat(fabricType.getVersion()).isEqualTo(Version.zero());
        assertThat(fabricType.getName()).isEqualTo(name);
        assertThat(fabricType.isNotDeleted()).isTrue();
    }

    @Test
    void shouldUpdateFabricType() {
        // given: a fabric type
        var id = create(FabricTypeName.of("Silk"));

        // when: updating the fabric type
        var updatedVersion = update(id, Version.zero(), FabricTypeName.of("Wool"));

        // then: the fabric type is updated
        var fabricType = get(id);
        assertThat(fabricType.getVersion()).isEqualTo(updatedVersion);
        assertThat(fabricType.getName()).isEqualTo(FabricTypeName.of("Wool"));
    }

    @Test
    void shouldNotUpdateFabricTypeGivenAnOutdatedVersion() {
        // given: a fabric type
        var id = create(FabricTypeName.of("Cotton"));

        // and: the fabric type is updated
        update(id, Version.zero(), FabricTypeName.of("Silk"));

        // when: updating the fabric type with an outdated version; then: an error is raised
        assertThatThrownBy(() -> update(id, Version.zero(), FabricTypeName.of("Wool")))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldDeleteFabricType() {
        // given: a fabric type
        var id = create(FabricTypeName.of("Cotton"));

        // when: deleting the fabric type
        delete(id, Version.zero());

        // then: the fabric type is deleted
        assertThat(get(id)).isNull();
    }

    @Test
    void shouldNotDeleteFabricTypeGivenAnOutdatedVersion() {
        // given: a fabric type
        var id = create(FabricTypeName.of("Cotton"));

        // and: the fabric type is updated
        update(id, Version.zero(), FabricTypeName.of("Silk"));

        // when: deleting the fabric type with an outdated version; then: an error is raised
        assertThatThrownBy(() -> delete(id, Version.zero()))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldSnapshotEvery100Events() {
        // given: a fabric type
        var id = create(FabricTypeName.of("Cotton"));

        // when: updating the fabric type 200 times
        var version = Version.zero();
        for (int i = 0; i < 200; i++) {
            version = update(id, version, FabricTypeName.of("Silk " + i));
        }

        // then: the fabric type is updated
        var fabricType = get(id);
        assertThat(fabricType.getVersion()).isEqualTo(Version.of(202));
        assertThat(fabricType.getName()).isEqualTo(FabricTypeName.of("Silk 199"));

        // and: there are exactly 2 snapshot events in the repository
        var events = repo.findEventsByAggregateIdAndType(
                AggregateId.of(id.getValue()),
                FabricType.TYPE,
                Version.zero()
        ).collectList().block();
        var snapshotEvents = events.stream().filter(e -> e.getMetadata().isSnapshot()).toList();
        assertThat(snapshotEvents).hasSize(2);
        assertThat(snapshotEvents.getFirst().getMetadata().getAggregateVersion()).isEqualTo(Version.of(100));
        assertThat(snapshotEvents.getLast().getMetadata().getAggregateVersion()).isEqualTo(Version.of(200));
    }

    private FabricType get(FabricTypeId id) {
        return fabricTypeService.get(id).block();
    }

    private FabricTypeId create(FabricTypeName name) {
        return fabricTypeService.create(name, Agent.system()).block().getId();
    }

    private Version update(FabricTypeId id, Version version, FabricTypeName name) {
        return fabricTypeService.update(id, version, name, Agent.system()).block();
    }

    private void delete(FabricTypeId id, Version version) {
        fabricTypeService.delete(id, version, Agent.system()).block();
    }

}
