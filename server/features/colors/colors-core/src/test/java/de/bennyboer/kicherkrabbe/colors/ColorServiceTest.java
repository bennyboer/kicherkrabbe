package de.bennyboer.kicherkrabbe.colors;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.LoggingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.inmemory.InMemoryEventSourcingRepo;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ColorServiceTest {

    private final EventSourcingRepo repo = new InMemoryEventSourcingRepo();

    private final LoggingEventPublisher eventPublisher = new LoggingEventPublisher();

    private final ColorService colorService = new ColorService(repo, eventPublisher, Clock.systemUTC());

    @Test
    void shouldCreateColor() {
        // given: a name to create a color for
        var name = ColorName.of("Red");

        // when: creating a color
        var id = create(name, 255, 0, 0);

        // then: the color is created
        var color = get(id);
        Assertions.assertThat(color.getId()).isEqualTo(id);
        Assertions.assertThat(color.getVersion()).isEqualTo(Version.zero());
        Assertions.assertThat(color.getName()).isEqualTo(name);
        Assertions.assertThat(color.getRed()).isEqualTo(255);
        Assertions.assertThat(color.getGreen()).isEqualTo(0);
        Assertions.assertThat(color.getBlue()).isEqualTo(0);
        Assertions.assertThat(color.isNotDeleted()).isTrue();
    }

    @Test
    void shouldUpdateColor() {
        // given: a color
        var id = create(ColorName.of("Red"), 255, 0, 0);

        // when: updating the color
        var updatedVersion = update(id, Version.zero(), ColorName.of("Green"), 0, 255, 0);

        // then: the color is updated
        var color = get(id);
        Assertions.assertThat(color.getVersion()).isEqualTo(updatedVersion);
        Assertions.assertThat(color.getName()).isEqualTo(ColorName.of("Green"));
        Assertions.assertThat(color.getRed()).isEqualTo(0);
        Assertions.assertThat(color.getGreen()).isEqualTo(255);
        Assertions.assertThat(color.getBlue()).isEqualTo(0);
    }

    @Test
    void shouldNotUpdateColorGivenAnOutdatedVersion() {
        // given: a color
        var id = create(ColorName.of("Red"), 255, 0, 0);

        // and: the color is updated
        update(id, Version.zero(), ColorName.of("Green"), 0, 255, 0);

        // when: updating the color with an outdated version; then: an error is raised
        assertThatThrownBy(() -> update(id, Version.zero(), ColorName.of("Blue"), 0, 0, 255))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldDeleteColor() {
        // given: a color
        var id = create(ColorName.of("Red"), 255, 0, 0);

        // when: deleting the color
        delete(id, Version.zero());

        // then: the color is deleted
        Assertions.assertThat(get(id)).isNull();
    }

    @Test
    void shouldNotDeleteColorGivenAnOutdatedVersion() {
        // given: a color
        var id = create(ColorName.of("Red"), 255, 0, 0);

        // and: the color is updated
        update(id, Version.zero(), ColorName.of("Green"), 0, 255, 0);

        // when: deleting the color with an outdated version; then: an error is raised
        assertThatThrownBy(() -> delete(id, Version.zero()))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldSnapshotEvery100Events() {
        // given: a color
        var id = create(ColorName.of("Red"), 255, 0, 0);

        // when: updating the color 200 times
        var version = Version.zero();
        for (int i = 0; i < 200; i++) {
            version = update(id, version, ColorName.of("Green " + i), 0, 255, 0);
        }

        // then: the color is updated
        var color = get(id);
        Assertions.assertThat(color.getVersion()).isEqualTo(Version.of(202));
        Assertions.assertThat(color.getName()).isEqualTo(ColorName.of("Green 199"));

        // and: there are exactly 2 snapshot events in the repository
        var events = repo.findEventsByAggregateIdAndType(
                AggregateId.of(id.getValue()),
                Color.TYPE,
                Version.zero()
        ).collectList().block();
        var snapshotEvents = events.stream().filter(e -> e.getMetadata().isSnapshot()).toList();
        assertThat(snapshotEvents).hasSize(2);
        assertThat(snapshotEvents.getFirst().getMetadata().getAggregateVersion()).isEqualTo(Version.of(100));
        assertThat(snapshotEvents.getLast().getMetadata().getAggregateVersion()).isEqualTo(Version.of(200));
    }

    private Color get(ColorId id) {
        return colorService.get(id).block();
    }

    private ColorId create(ColorName name, int red, int green, int blue) {
        return colorService.create(name, red, green, blue, Agent.system()).block().getId();
    }

    private Version update(ColorId id, Version version, ColorName name, int red, int green, int blue) {
        return colorService.update(id, version, name, red, green, blue, Agent.system()).block();
    }

    private void delete(ColorId id, Version version) {
        colorService.delete(id, version, Agent.system()).block();
    }

}
