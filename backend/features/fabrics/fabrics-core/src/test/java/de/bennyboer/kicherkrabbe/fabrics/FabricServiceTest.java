package de.bennyboer.kicherkrabbe.fabrics;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.LoggingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.inmemory.InMemoryEventSourcingRepo;
import de.bennyboer.kicherkrabbe.fabrics.feature.AlreadyFeaturedError;
import de.bennyboer.kicherkrabbe.fabrics.publish.AlreadyPublishedError;
import de.bennyboer.kicherkrabbe.fabrics.samples.SampleFabric;
import de.bennyboer.kicherkrabbe.fabrics.unfeature.AlreadyUnfeaturedError;
import de.bennyboer.kicherkrabbe.fabrics.unpublish.AlreadyUnpublishedError;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class FabricServiceTest {

    private final EventSourcingRepo repo = new InMemoryEventSourcingRepo();

    private final LoggingEventPublisher eventPublisher = new LoggingEventPublisher();

    private final FabricService fabricService = new FabricService(repo, eventPublisher, Clock.systemUTC());

    @Test
    void shouldCreateFabric() {
        // when: creating a fabric
        var id = create(
                FabricName.of("Fabric"),
                ImageId.of("image"),
                Set.of(ColorId.of("color")),
                Set.of(TopicId.of("topic")),
                Set.of(FabricTypeAvailability.of(FabricTypeId.of("fabric-type"), true))
        );

        // then: the fabric is created
        var fabric = get(id);
        assertThat(fabric.getId()).isEqualTo(id);
        assertThat(fabric.getVersion()).isEqualTo(Version.zero());
        assertThat(fabric.getName()).isEqualTo(FabricName.of("Fabric"));
        assertThat(fabric.getImage()).isEqualTo(ImageId.of("image"));
        assertThat(fabric.getColors()).containsExactly(ColorId.of("color"));
        assertThat(fabric.getTopics()).containsExactly(TopicId.of("topic"));
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
                Set.of(TopicId.of("topic")),
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
                Set.of(TopicId.of("topic")),
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
                Set.of(TopicId.of("topic")),
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
                Set.of(TopicId.of("topic")),
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
                Set.of(TopicId.of("topic")),
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
                Set.of(TopicId.of("topic")),
                Set.of(FabricTypeAvailability.of(FabricTypeId.of("fabric-type"), true))
        );

        // and: the fabric is renamed
        rename(id, Version.zero(), FabricName.of("Fabric 2"));

        // when: publishing the fabric with an outdated version; then: an error is raised
        assertThatThrownBy(() -> publish(id, Version.zero()))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldRaiseErrorIfFabricAlreadyPublished() {
        // given: a published fabric
        var id = create(
                FabricName.of("Fabric"),
                ImageId.of("image"),
                Set.of(ColorId.of("color")),
                Set.of(TopicId.of("topic")),
                Set.of(FabricTypeAvailability.of(FabricTypeId.of("fabric-type"), true))
        );
        publish(id, Version.zero());

        // when: trying to publish the fabric again; then: an error is raised
        assertThatThrownBy(() -> publish(id, Version.of(1)))
                .isInstanceOf(AlreadyPublishedError.class);
    }

    @Test
    void shouldUnpublishFabric() {
        // given: a published fabric
        var id = create(
                FabricName.of("Fabric"),
                ImageId.of("image"),
                Set.of(ColorId.of("color")),
                Set.of(TopicId.of("topic")),
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
                Set.of(TopicId.of("topic")),
                Set.of(FabricTypeAvailability.of(FabricTypeId.of("fabric-type"), true))
        );
        var version = publish(id, Version.zero());

        // and: the fabric is renamed
        rename(id, version, FabricName.of("Fabric 2"));

        // when: unpublishing the fabric with an outdated version; then: an error is raised
        assertThatThrownBy(() -> unpublish(id, Version.of(1)))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldNotUnpublishFabricGivenAnAlreadyUnpublishedFabric() {
        // given: an unpublished fabric
        var id = create(
                FabricName.of("Fabric"),
                ImageId.of("image"),
                Set.of(ColorId.of("color")),
                Set.of(TopicId.of("topic")),
                Set.of(FabricTypeAvailability.of(FabricTypeId.of("fabric-type"), true))
        );

        // when: trying to unpublish the fabric; then: an error is raised
        assertThatThrownBy(() -> unpublish(id, Version.zero()))
                .isInstanceOf(AlreadyUnpublishedError.class);
    }

    @Test
    void shouldFeatureFabric() {
        // given: a fabric
        var id = create(
                FabricName.of("Fabric"),
                ImageId.of("image"),
                Set.of(ColorId.of("color")),
                Set.of(TopicId.of("topic")),
                Set.of(FabricTypeAvailability.of(FabricTypeId.of("fabric-type"), true))
        );

        // when: featuring the fabric
        var updatedVersion = feature(id, Version.zero());

        // then: the fabric is featured
        var fabric = get(id);
        assertThat(fabric.getVersion()).isEqualTo(updatedVersion);
        assertThat(fabric.isFeatured()).isTrue();
    }

    @Test
    void shouldNotFeatureFabricGivenAnOutdatedVersion() {
        // given: a fabric
        var id = create(
                FabricName.of("Fabric"),
                ImageId.of("image"),
                Set.of(ColorId.of("color")),
                Set.of(TopicId.of("topic")),
                Set.of(FabricTypeAvailability.of(FabricTypeId.of("fabric-type"), true))
        );

        // and: the fabric is renamed
        rename(id, Version.zero(), FabricName.of("Fabric 2"));

        // when: featuring the fabric with an outdated version; then: an error is raised
        assertThatThrownBy(() -> feature(id, Version.zero()))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldRaiseErrorIfFabricAlreadyFeatured() {
        // given: a featured fabric
        var id = create(
                FabricName.of("Fabric"),
                ImageId.of("image"),
                Set.of(ColorId.of("color")),
                Set.of(TopicId.of("topic")),
                Set.of(FabricTypeAvailability.of(FabricTypeId.of("fabric-type"), true))
        );
        feature(id, Version.zero());

        // when: trying to feature the fabric again; then: an error is raised
        assertThatThrownBy(() -> feature(id, Version.of(1)))
                .isInstanceOf(AlreadyFeaturedError.class);
    }

    @Test
    void shouldUnfeatureFabric() {
        // given: a featured fabric
        var id = create(
                FabricName.of("Fabric"),
                ImageId.of("image"),
                Set.of(ColorId.of("color")),
                Set.of(TopicId.of("topic")),
                Set.of(FabricTypeAvailability.of(FabricTypeId.of("fabric-type"), true))
        );
        feature(id, Version.zero());

        // when: unfeaturing the fabric
        var updatedVersion = unfeature(id, Version.of(1));

        // then: the fabric is unfeatured
        var fabric = get(id);
        assertThat(fabric.getVersion()).isEqualTo(updatedVersion);
        assertThat(fabric.isFeatured()).isFalse();
    }

    @Test
    void shouldNotUnfeatureFabricGivenAnOutdatedVersion() {
        // given: a featured fabric
        var id = create(
                FabricName.of("Fabric"),
                ImageId.of("image"),
                Set.of(ColorId.of("color")),
                Set.of(TopicId.of("topic")),
                Set.of(FabricTypeAvailability.of(FabricTypeId.of("fabric-type"), true))
        );
        var version = feature(id, Version.zero());

        // and: the fabric is renamed
        rename(id, version, FabricName.of("Fabric 2"));

        // when: unfeaturing the fabric with an outdated version; then: an error is raised
        assertThatThrownBy(() -> unfeature(id, Version.of(1)))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldNotUnfeatureFabricGivenAnAlreadyUnfeaturedFabric() {
        // given: an unfeatured fabric
        var id = create(
                FabricName.of("Fabric"),
                ImageId.of("image"),
                Set.of(ColorId.of("color")),
                Set.of(TopicId.of("topic")),
                Set.of(FabricTypeAvailability.of(FabricTypeId.of("fabric-type"), true))
        );

        // when: trying to unfeature the fabric; then: an error is raised
        assertThatThrownBy(() -> unfeature(id, Version.zero()))
                .isInstanceOf(AlreadyUnfeaturedError.class);
    }

    @Test
    void shouldUpdateColors() {
        // given: a fabric
        var id = create(
                FabricName.of("Fabric"),
                ImageId.of("image"),
                Set.of(ColorId.of("color")),
                Set.of(TopicId.of("topic")),
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
                Set.of(TopicId.of("topic")),
                Set.of(FabricTypeAvailability.of(FabricTypeId.of("fabric-type"), true))
        );

        // and: the fabric is renamed
        rename(id, Version.zero(), FabricName.of("Fabric 2"));

        // when: updating the colors with an outdated version; then: an error is raised
        assertThatThrownBy(() -> updateColors(id, Version.zero(), Set.of(ColorId.of("color 2"))))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldUpdateImages() {
        // given: a fabric
        var id = create(
                FabricName.of("Fabric"),
                ImageId.of("image"),
                Set.of(ColorId.of("color")),
                Set.of(TopicId.of("topic")),
                Set.of(FabricTypeAvailability.of(FabricTypeId.of("fabric-type"), true))
        );

        // when: updating the images
        var updatedVersion = updateImages(id, Version.zero(), ImageId.of("image 2"), List.of(ImageId.of("example1"), ImageId.of("example2")));

        // then: the images are updated
        var fabric = get(id);
        assertThat(fabric.getVersion()).isEqualTo(updatedVersion);
        assertThat(fabric.getImage()).isEqualTo(ImageId.of("image 2"));
        assertThat(fabric.getExampleImages()).containsExactly(ImageId.of("example1"), ImageId.of("example2"));
    }

    @Test
    void shouldNotUpdateImagesGivenAnOutdatedVersion() {
        // given: a fabric
        var id = create(
                FabricName.of("Fabric"),
                ImageId.of("image"),
                Set.of(ColorId.of("color")),
                Set.of(TopicId.of("topic")),
                Set.of(FabricTypeAvailability.of(FabricTypeId.of("fabric-type"), true))
        );

        // and: the fabric is renamed
        rename(id, Version.zero(), FabricName.of("Fabric 2"));

        // when: updating the images with an outdated version; then: an error is raised
        assertThatThrownBy(() -> updateImages(id, Version.zero(), ImageId.of("image 2"), List.of()))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldUpdateTopics() {
        // given: a fabric
        var id = create(
                FabricName.of("Fabric"),
                ImageId.of("image"),
                Set.of(ColorId.of("color")),
                Set.of(TopicId.of("topic")),
                Set.of(FabricTypeAvailability.of(FabricTypeId.of("fabric-type"), true))
        );

        // when: updating the topics
        var updatedVersion = updateTopics(id, Version.zero(), Set.of(TopicId.of("topic 2"), TopicId.of("topic 3")));

        // then: the topics are updated
        var fabric = get(id);
        assertThat(fabric.getVersion()).isEqualTo(updatedVersion);
        assertThat(fabric.getTopics()).containsExactlyInAnyOrder(TopicId.of("topic 2"), TopicId.of("topic 3"));
    }

    @Test
    void shouldNotUpdateTopicsGivenAnOutdatedVersion() {
        // given: a fabric
        var id = create(
                FabricName.of("Fabric"),
                ImageId.of("image"),
                Set.of(ColorId.of("color")),
                Set.of(TopicId.of("topic")),
                Set.of(FabricTypeAvailability.of(FabricTypeId.of("fabric-type"), true))
        );

        // and: the fabric is renamed
        rename(id, Version.zero(), FabricName.of("Fabric 2"));

        // when: updating the topics with an outdated version; then: an error is raised
        assertThatThrownBy(() -> updateTopics(id, Version.zero(), Set.of(TopicId.of("topic 2"))))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldUpdateAvailability() {
        // given: a fabric
        var id = create(
                FabricName.of("Fabric"),
                ImageId.of("image"),
                Set.of(ColorId.of("color")),
                Set.of(TopicId.of("topic")),
                Set.of(FabricTypeAvailability.of(FabricTypeId.of("fabric-type"), true))
        );

        // when: updating the availability
        var updatedVersion = updateAvailability(
                id, Version.zero(), Set.of(
                        FabricTypeAvailability.of(FabricTypeId.of("fabric-type 2"), true),
                        FabricTypeAvailability.of(FabricTypeId.of("fabric-type 3"), false)
                )
        );

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
                Set.of(TopicId.of("topic")),
                Set.of(FabricTypeAvailability.of(FabricTypeId.of("fabric-type"), true))
        );

        // and: the fabric is renamed
        rename(id, Version.zero(), FabricName.of("Fabric 2"));

        // when: updating the availability with an outdated version; then: an error is raised
        assertThatThrownBy(() -> updateAvailability(
                id, Version.zero(), Set.of(
                        FabricTypeAvailability.of(FabricTypeId.of("fabric-type 2"), true),
                        FabricTypeAvailability.of(FabricTypeId.of("fabric-type 3"), false)
                )
        )).matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldRemoveTopic() {
        // given: a fabric
        var id = create(
                FabricName.of("Fabric"),
                ImageId.of("image"),
                Set.of(ColorId.of("color")),
                Set.of(TopicId.of("topic")),
                Set.of(FabricTypeAvailability.of(FabricTypeId.of("fabric-type"), true))
        );

        // when: removing the topic
        var updatedVersion = removeTopic(id, Version.zero(), TopicId.of("topic"));

        // then: the topic is removed
        var fabric = get(id);
        assertThat(fabric.getVersion()).isEqualTo(updatedVersion);
        assertThat(fabric.getTopics()).isEmpty();
    }

    @Test
    void shouldNotRemoveTopicGivenAnOutdatedVersion() {
        // given: a fabric
        var id = create(
                FabricName.of("Fabric"),
                ImageId.of("image"),
                Set.of(ColorId.of("color")),
                Set.of(TopicId.of("topic")),
                Set.of(FabricTypeAvailability.of(FabricTypeId.of("fabric-type"), true))
        );

        // and: the fabric is renamed
        rename(id, Version.zero(), FabricName.of("Fabric 2"));

        // when: removing the topic with an outdated version; then: an error is raised
        assertThatThrownBy(() -> removeTopic(id, Version.zero(), TopicId.of("topic")))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldRemoveColor() {
        // given: a fabric
        var id = create(
                FabricName.of("Fabric"),
                ImageId.of("image"),
                Set.of(ColorId.of("color")),
                Set.of(TopicId.of("topic")),
                Set.of(FabricTypeAvailability.of(FabricTypeId.of("fabric-type"), true))
        );

        // when: removing the color
        var updatedVersion = removeColor(id, Version.zero(), ColorId.of("color"));

        // then: the color is removed
        var fabric = get(id);
        assertThat(fabric.getVersion()).isEqualTo(updatedVersion);
        assertThat(fabric.getColors()).isEmpty();
    }

    @Test
    void shouldNotRemoveColorGivenAnOutdatedVersion() {
        // given: a fabric
        var id = create(
                FabricName.of("Fabric"),
                ImageId.of("image"),
                Set.of(ColorId.of("color")),
                Set.of(TopicId.of("topic")),
                Set.of(FabricTypeAvailability.of(FabricTypeId.of("fabric-type"), true))
        );

        // and: the fabric is renamed
        rename(id, Version.zero(), FabricName.of("Fabric 2"));

        // when: removing the color with an outdated version; then: an error is raised
        assertThatThrownBy(() -> removeColor(id, Version.zero(), ColorId.of("color")))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldRemoveFabricType() {
        // given: a fabric
        var id = create(
                FabricName.of("Fabric"),
                ImageId.of("image"),
                Set.of(ColorId.of("color")),
                Set.of(TopicId.of("topic")),
                Set.of(FabricTypeAvailability.of(FabricTypeId.of("fabric-type"), true))
        );

        // when: removing the fabric type
        var updatedVersion = removeFabricType(id, Version.zero(), FabricTypeId.of("fabric-type"));

        // then: the fabric type is removed
        var fabric = get(id);
        assertThat(fabric.getVersion()).isEqualTo(updatedVersion);
        assertThat(fabric.getAvailability()).isEmpty();
    }

    @Test
    void shouldNotRemoveFabricTypeGivenAnOutdatedVersion() {
        // given: a fabric
        var id = create(
                FabricName.of("Fabric"),
                ImageId.of("image"),
                Set.of(ColorId.of("color")),
                Set.of(TopicId.of("topic")),
                Set.of(FabricTypeAvailability.of(FabricTypeId.of("fabric-type"), true))
        );

        // and: the fabric is renamed
        rename(id, Version.zero(), FabricName.of("Fabric 2"));

        // when: removing the fabric type with an outdated version; then: an error is raised
        assertThatThrownBy(() -> removeFabricType(id, Version.zero(), FabricTypeId.of("fabric-type")))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldSnapshotEvery100Events() {
        // given: a fabric
        var id = create(
                FabricName.of("Fabric"),
                ImageId.of("image"),
                Set.of(ColorId.of("color")),
                Set.of(TopicId.of("topic")),
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
            Set<TopicId> topics,
            Set<FabricTypeAvailability> availability
    ) {
        return fabricService.create(
                name,
                imageId,
                colors,
                topics,
                availability,
                Agent.system()
        ).block().getId();
    }

    private FabricId create(SampleFabric sample) {
        return create(
                sample.getName(),
                sample.getImageId(),
                sample.getColorIds(),
                sample.getTopicIds(),
                sample.getAvailabilities()
        );
    }

    private FabricId createSampleFabric() {
        return create(SampleFabric.builder().build());
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

    private Version feature(FabricId id, Version version) {
        return fabricService.feature(id, version, Agent.system()).block();
    }

    private Version unfeature(FabricId id, Version version) {
        return fabricService.unfeature(id, version, Agent.system()).block();
    }

    private Version updateColors(FabricId id, Version version, Set<ColorId> colors) {
        return fabricService.updateColors(id, version, colors, Agent.system()).block();
    }

    private Version updateImages(FabricId id, Version version, ImageId imageId, List<ImageId> exampleImages) {
        return fabricService.updateImages(id, version, imageId, exampleImages, Agent.system()).block();
    }

    private Version updateTopics(FabricId id, Version version, Set<TopicId> topics) {
        return fabricService.updateTopics(id, version, topics, Agent.system()).block();
    }

    private Version updateAvailability(FabricId id, Version version, Set<FabricTypeAvailability> availability) {
        return fabricService.updateAvailability(id, version, availability, Agent.system()).block();
    }

    private Version removeTopic(FabricId id, Version version, TopicId topicId) {
        return fabricService.removeTopic(id, version, topicId, Agent.system()).block();
    }

    private Version removeColor(FabricId id, Version version, ColorId colorId) {
        return fabricService.removeColor(id, version, colorId, Agent.system()).block();
    }

    private Version removeFabricType(FabricId id, Version version, FabricTypeId fabricTypeId) {
        return fabricService.removeFabricType(id, version, fabricTypeId, Agent.system()).block();
    }

}
