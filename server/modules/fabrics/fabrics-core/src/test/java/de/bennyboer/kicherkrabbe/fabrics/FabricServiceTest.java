package de.bennyboer.kicherkrabbe.fabrics;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.LoggingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.inmemory.InMemoryEventSourcingRepo;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class FabricServiceTest {

    private final EventSourcingRepo repo = new InMemoryEventSourcingRepo();

    private final LoggingEventPublisher eventPublisher = new LoggingEventPublisher();

    private final FabricService fabricService = new FabricService(repo, eventPublisher);

    @Test
    void shouldCreateFabric() {
        // when: creating a fabric
        var id = create(
                FabricName.of("Fabric"),
                ImageId.of("image"),
                Set.of(ColorId.of("color")),
                Set.of(TopicId.of("theme")),
                Set.of(FabricTypeAvailability.of(FabricTypeId.of("fabric-type"), true))
        );

        // then: the fabric is created
        var fabric = get(id);
        assertThat(fabric.getId()).isEqualTo(id);
        assertThat(fabric.getVersion()).isEqualTo(Version.zero());
        assertThat(fabric.getName()).isEqualTo(FabricName.of("Fabric"));
        assertThat(fabric.getImage()).isEqualTo(ImageId.of("image"));
        assertThat(fabric.getColors()).containsExactly(ColorId.of("color"));
        assertThat(fabric.getTopics()).containsExactly(TopicId.of("theme"));
        assertThat(fabric.getAvailability()).containsExactly(FabricTypeAvailability.of(
                FabricTypeId.of("fabric-type"),
                true
        ));
        assertThat(fabric.isNotDeleted()).isTrue();
    }

    @Test
    void shouldRenameFabric() {
        // given: a fabric
        var id = create(
                FabricName.of("Fabric"),
                ImageId.of("image"),
                Set.of(ColorId.of("color")),
                Set.of(TopicId.of("theme")),
                Set.of(FabricTypeAvailability.of(FabricTypeId.of("fabric-type"), true))
        );

        // when: renaming the fabric
        var updatedVersion = rename(id, Version.zero(), FabricName.of("Fabric 2"));

        // then: the fabric is renamed
        var fabric = get(id);
        assertThat(fabric.getVersion()).isEqualTo(updatedVersion);
        assertThat(fabric.getName()).isEqualTo(FabricName.of("Fabric 2"));
    }

    @Test
    void shouldNotRenameFabricGivenAnOutdatedVersion() {
        // given: a fabric
        var id = create(
                FabricName.of("Fabric"),
                ImageId.of("image"),
                Set.of(ColorId.of("color")),
                Set.of(TopicId.of("theme")),
                Set.of(FabricTypeAvailability.of(FabricTypeId.of("fabric-type"), true))
        );

        // and: the fabric is renamed
        rename(id, Version.zero(), FabricName.of("Fabric 2"));

        // when: renaming the fabric with an outdated version; then: an error is raised
        assertThatThrownBy(() -> rename(id, Version.zero(), FabricName.of("Fabric 3")))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldDeleteFabric() {
        // given: a fabric
        var id = create(
                FabricName.of("Fabric"),
                ImageId.of("image"),
                Set.of(ColorId.of("color")),
                Set.of(TopicId.of("theme")),
                Set.of(FabricTypeAvailability.of(FabricTypeId.of("fabric-type"), true))
        );

        // when: deleting the fabric
        var updatedVersion = delete(id, Version.zero());

        // then: the fabric is deleted
        var fabric = get(id);
        assertThat(fabric).isNull();
    }

    @Test
    void shouldNotDeleteFabricGivenAnOutdatedVersion() {
        // given: a fabric
        var id = create(
                FabricName.of("Fabric"),
                ImageId.of("image"),
                Set.of(ColorId.of("color")),
                Set.of(TopicId.of("theme")),
                Set.of(FabricTypeAvailability.of(FabricTypeId.of("fabric-type"), true))
        );

        // and: the fabric is renamed
        rename(id, Version.zero(), FabricName.of("Fabric 2"));

        // when: deleting the fabric with an outdated version; then: an error is raised
        assertThatThrownBy(() -> delete(id, Version.zero()))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldPublishFabric() {
        // given: a fabric
        var id = create(
                FabricName.of("Fabric"),
                ImageId.of("image"),
                Set.of(ColorId.of("color")),
                Set.of(TopicId.of("theme")),
                Set.of(FabricTypeAvailability.of(FabricTypeId.of("fabric-type"), true))
        );

        // when: publishing the fabric
        var updatedVersion = publish(id, Version.zero());

        // then: the fabric is published
        var fabric = get(id);
        assertThat(fabric.getVersion()).isEqualTo(updatedVersion);
        assertThat(fabric.isPublished()).isTrue();
    }

    @Test
    void shouldNotPublishFabricGivenAnOutdatedVersion() {
        // given: a fabric
        var id = create(
                FabricName.of("Fabric"),
                ImageId.of("image"),
                Set.of(ColorId.of("color")),
                Set.of(TopicId.of("theme")),
                Set.of(FabricTypeAvailability.of(FabricTypeId.of("fabric-type"), true))
        );

        // and: the fabric is renamed
        rename(id, Version.zero(), FabricName.of("Fabric 2"));

        // when: publishing the fabric with an outdated version; then: an error is raised
        assertThatThrownBy(() -> publish(id, Version.zero()))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldUnpublishFabric() {
        // given: a published fabric
        var id = create(
                FabricName.of("Fabric"),
                ImageId.of("image"),
                Set.of(ColorId.of("color")),
                Set.of(TopicId.of("theme")),
                Set.of(FabricTypeAvailability.of(FabricTypeId.of("fabric-type"), true))
        );
        publish(id, Version.zero());

        // when: unpublishing the fabric
        var updatedVersion = unpublish(id, Version.of(1));

        // then: the fabric is unpublished
        var fabric = get(id);
        assertThat(fabric.getVersion()).isEqualTo(updatedVersion);
        assertThat(fabric.isPublished()).isFalse();
    }

    @Test
    void shouldNotUnpublishFabricGivenAnOutdatedVersion() {
        // given: a published fabric
        var id = create(
                FabricName.of("Fabric"),
                ImageId.of("image"),
                Set.of(ColorId.of("color")),
                Set.of(TopicId.of("theme")),
                Set.of(FabricTypeAvailability.of(FabricTypeId.of("fabric-type"), true))
        );
        var version = publish(id, Version.zero());

        // and: the fabric is renamed
        rename(id, version, FabricName.of("Fabric 2"));

        // when: unpublishing the fabric with an outdated version; then: an error is raised
        assertThatThrownBy(() -> unpublish(id, Version.zero()))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldUpdateColors() {
        // given: a fabric
        var id = create(
                FabricName.of("Fabric"),
                ImageId.of("image"),
                Set.of(ColorId.of("color")),
                Set.of(TopicId.of("theme")),
                Set.of(FabricTypeAvailability.of(FabricTypeId.of("fabric-type"), true))
        );

        // when: updating the colors
        var updatedVersion = updateColors(id, Version.zero(), Set.of(ColorId.of("color 2"), ColorId.of("color 3")));

        // then: the colors are updated
        var fabric = get(id);
        assertThat(fabric.getVersion()).isEqualTo(updatedVersion);
        assertThat(fabric.getColors()).containsExactlyInAnyOrder(ColorId.of("color 2"), ColorId.of("color 3"));
    }

    @Test
    void shouldNotUpdateColorsGivenAnOutdatedVersion() {
        // given: a fabric
        var id = create(
                FabricName.of("Fabric"),
                ImageId.of("image"),
                Set.of(ColorId.of("color")),
                Set.of(TopicId.of("theme")),
                Set.of(FabricTypeAvailability.of(FabricTypeId.of("fabric-type"), true))
        );

        // and: the fabric is renamed
        rename(id, Version.zero(), FabricName.of("Fabric 2"));

        // when: updating the colors with an outdated version; then: an error is raised
        assertThatThrownBy(() -> updateColors(id, Version.zero(), Set.of(ColorId.of("color 2"))))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldUpdateImage() {
        // given: a fabric
        var id = create(
                FabricName.of("Fabric"),
                ImageId.of("image"),
                Set.of(ColorId.of("color")),
                Set.of(TopicId.of("theme")),
                Set.of(FabricTypeAvailability.of(FabricTypeId.of("fabric-type"), true))
        );

        // when: updating the image
        var updatedVersion = updateImage(id, Version.zero(), ImageId.of("image 2"));

        // then: the image is updated
        var fabric = get(id);
        assertThat(fabric.getVersion()).isEqualTo(updatedVersion);
        assertThat(fabric.getImage()).isEqualTo(ImageId.of("image 2"));
    }

    @Test
    void shouldNotUpdateImageGivenAnOutdatedVersion() {
        // given: a fabric
        var id = create(
                FabricName.of("Fabric"),
                ImageId.of("image"),
                Set.of(ColorId.of("color")),
                Set.of(TopicId.of("theme")),
                Set.of(FabricTypeAvailability.of(FabricTypeId.of("fabric-type"), true))
        );

        // and: the fabric is renamed
        rename(id, Version.zero(), FabricName.of("Fabric 2"));

        // when: updating the image with an outdated version; then: an error is raised
        assertThatThrownBy(() -> updateImage(id, Version.zero(), ImageId.of("image 2")))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldUpdateThemes() {
        // given: a fabric
        var id = create(
                FabricName.of("Fabric"),
                ImageId.of("image"),
                Set.of(ColorId.of("color")),
                Set.of(TopicId.of("theme")),
                Set.of(FabricTypeAvailability.of(FabricTypeId.of("fabric-type"), true))
        );

        // when: updating the themes
        var updatedVersion = updateThemes(id, Version.zero(), Set.of(TopicId.of("theme 2"), TopicId.of("theme 3")));

        // then: the themes are updated
        var fabric = get(id);
        assertThat(fabric.getVersion()).isEqualTo(updatedVersion);
        assertThat(fabric.getTopics()).containsExactlyInAnyOrder(TopicId.of("theme 2"), TopicId.of("theme 3"));
    }

    @Test
    void shouldNotUpdateThemesGivenAnOutdatedVersion() {
        // given: a fabric
        var id = create(
                FabricName.of("Fabric"),
                ImageId.of("image"),
                Set.of(ColorId.of("color")),
                Set.of(TopicId.of("theme")),
                Set.of(FabricTypeAvailability.of(FabricTypeId.of("fabric-type"), true))
        );

        // and: the fabric is renamed
        rename(id, Version.zero(), FabricName.of("Fabric 2"));

        // when: updating the themes with an outdated version; then: an error is raised
        assertThatThrownBy(() -> updateThemes(id, Version.zero(), Set.of(TopicId.of("theme 2"))))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldUpdateAvailability() {
        // given: a fabric
        var id = create(
                FabricName.of("Fabric"),
                ImageId.of("image"),
                Set.of(ColorId.of("color")),
                Set.of(TopicId.of("theme")),
                Set.of(FabricTypeAvailability.of(FabricTypeId.of("fabric-type"), true))
        );

        // when: updating the availability
        var updatedVersion = updateAvailability(id, Version.zero(), Set.of(
                FabricTypeAvailability.of(FabricTypeId.of("fabric-type 2"), true),
                FabricTypeAvailability.of(FabricTypeId.of("fabric-type 3"), false)
        ));

        // then: the availability is updated
        var fabric = get(id);
        assertThat(fabric.getVersion()).isEqualTo(updatedVersion);
        assertThat(fabric.getAvailability()).containsExactlyInAnyOrder(
                FabricTypeAvailability.of(FabricTypeId.of("fabric-type 2"), true),
                FabricTypeAvailability.of(FabricTypeId.of("fabric-type 3"), false)
        );
    }

    @Test
    void shouldNotUpdateAvailabilityGivenAnOutdatedVersion() {
        // given: a fabric
        var id = create(
                FabricName.of("Fabric"),
                ImageId.of("image"),
                Set.of(ColorId.of("color")),
                Set.of(TopicId.of("theme")),
                Set.of(FabricTypeAvailability.of(FabricTypeId.of("fabric-type"), true))
        );

        // and: the fabric is renamed
        rename(id, Version.zero(), FabricName.of("Fabric 2"));

        // when: updating the availability with an outdated version; then: an error is raised
        assertThatThrownBy(() -> updateAvailability(id, Version.zero(), Set.of(
                FabricTypeAvailability.of(FabricTypeId.of("fabric-type 2"), true),
                FabricTypeAvailability.of(FabricTypeId.of("fabric-type 3"), false)
        ))).matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldSnapshotEvery100Events() {
        // given: a fabric
        var id = create(
                FabricName.of("Fabric"),
                ImageId.of("image"),
                Set.of(ColorId.of("color")),
                Set.of(TopicId.of("theme")),
                Set.of(FabricTypeAvailability.of(FabricTypeId.of("fabric"), true))
        );

        // when: updating the fabric 200 times
        var version = Version.zero();
        for (int i = 0; i < 200; i++) {
            version = rename(id, version, FabricName.of("Fabric " + i));
        }

        // then: the fabric is updated
        var fabric = get(id);
        assertThat(fabric.getVersion()).isEqualTo(Version.of(202));
        assertThat(fabric.getName()).isEqualTo(FabricName.of("Fabric 199"));

        // and: there are exactly 2 snapshot events in the repository
        var events = repo.findEventsByAggregateIdAndType(
                AggregateId.of(id.getValue()),
                Fabric.TYPE,
                Version.zero()
        ).collectList().block();
        var snapshotEvents = events.stream().filter(e -> e.getMetadata().isSnapshot()).toList();
        assertThat(snapshotEvents).hasSize(2);
        assertThat(snapshotEvents.getFirst().getMetadata().getAggregateVersion()).isEqualTo(Version.of(100));
        assertThat(snapshotEvents.getLast().getMetadata().getAggregateVersion()).isEqualTo(Version.of(200));
    }

    private Fabric get(FabricId id) {
        return fabricService.get(id).block();
    }

    private FabricId create(
            FabricName name,
            ImageId imageId,
            Set<ColorId> colors,
            Set<TopicId> themes,
            Set<FabricTypeAvailability> availability
    ) {
        return fabricService.create(
                name,
                imageId,
                colors,
                themes,
                availability,
                Agent.system()
        ).block().getId();
    }

    private Version delete(FabricId id, Version version) {
        return fabricService.delete(id, version, Agent.system()).block();
    }

    private Version rename(FabricId id, Version version, FabricName name) {
        return fabricService.rename(id, version, name, Agent.system()).block();
    }

    private Version publish(FabricId id, Version version) {
        return fabricService.publish(id, version, Agent.system()).block();
    }

    private Version unpublish(FabricId id, Version version) {
        return fabricService.unpublish(id, version, Agent.system()).block();
    }

    private Version updateColors(FabricId id, Version version, Set<ColorId> colors) {
        return fabricService.updateColors(id, version, colors, Agent.system()).block();
    }

    private Version updateImage(FabricId id, Version version, ImageId imageId) {
        return fabricService.updateImage(id, version, imageId, Agent.system()).block();
    }

    private Version updateThemes(FabricId id, Version version, Set<TopicId> topics) {
        return fabricService.updateTopics(id, version, topics, Agent.system()).block();
    }

    private Version updateAvailability(FabricId id, Version version, Set<FabricTypeAvailability> availability) {
        return fabricService.updateAvailability(id, version, availability, Agent.system()).block();
    }

}
