package de.bennyboer.kicherkrabbe.embroideries;

import de.bennyboer.kicherkrabbe.embroideries.feature.AlreadyFeaturedError;
import de.bennyboer.kicherkrabbe.embroideries.publish.AlreadyPublishedError;
import de.bennyboer.kicherkrabbe.embroideries.samples.SampleEmbroidery;
import de.bennyboer.kicherkrabbe.embroideries.unfeature.AlreadyUnfeaturedError;
import de.bennyboer.kicherkrabbe.embroideries.unpublish.AlreadyUnpublishedError;
import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.LoggingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.inmemory.InMemoryEventSourcingRepo;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class EmbroideryServiceTest {

    private final EventSourcingRepo repo = new InMemoryEventSourcingRepo();

    private final LoggingEventPublisher eventPublisher = new LoggingEventPublisher();

    private final EmbroideryService embroideryService = new EmbroideryService(repo, eventPublisher, Clock.systemUTC());

    @Test
    void shouldCreateEmbroidery() {
        // when: creating an embroidery
        var sample = SampleEmbroidery.builder()
                .categoryId("ANIMALS")
                .categoryId("FLOWERS")
                .build();
        var id = create(sample);

        // then: the embroidery is created
        var embroidery = get(id);
        assertThat(embroidery.getId()).isEqualTo(id);
        assertThat(embroidery.getVersion()).isEqualTo(Version.zero());
        assertThat(embroidery.getName()).isEqualTo(sample.getName());
        assertThat(embroidery.getImage()).isEqualTo(sample.getImageId());
        assertThat(embroidery.getCategories()).isEqualTo(sample.getCategoryIds());
        assertThat(embroidery.isPublished()).isFalse();
        assertThat(embroidery.isFeatured()).isFalse();
        assertThat(embroidery.getCreatedAt()).isNotNull();
        assertThat(embroidery.isNotDeleted()).isTrue();
    }

    @Test
    void shouldCreateEmbroideryWithoutCategories() {
        // when: creating an embroidery without categories
        var id = embroideryService.create(
                EmbroideryName.of("Crab"),
                ImageId.of("IMAGE_ID"),
                Set.of(),
                Agent.system()
        ).block().getId();

        // then: the embroidery is created without categories
        var embroidery = get(id);
        assertThat(embroidery.getCategories()).isEmpty();
    }

    @Test
    void shouldNotCreateEmbroideryWithoutImage() {
        // when: creating an embroidery without an image; then: an error is raised
        assertThatThrownBy(() -> embroideryService.create(
                EmbroideryName.of("Crab"),
                null,
                Set.of(),
                Agent.system()
        ).block()).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRenameEmbroidery() {
        // given: an embroidery
        var id = create(SampleEmbroidery.builder().build());

        // when: renaming the embroidery
        var updatedVersion = rename(id, Version.zero(), EmbroideryName.of("Embroidery 2"));

        // then: the embroidery is renamed
        var embroidery = get(id);
        assertThat(embroidery.getVersion()).isEqualTo(updatedVersion);
        assertThat(embroidery.getName()).isEqualTo(EmbroideryName.of("Embroidery 2"));
    }

    @Test
    void shouldNotRenameEmbroideryGivenAnOutdatedVersion() {
        // given: an embroidery
        var id = create(SampleEmbroidery.builder().build());

        // and: the embroidery is renamed
        rename(id, Version.zero(), EmbroideryName.of("Embroidery 2"));

        // when: renaming the embroidery with an outdated version; then: an error is raised
        assertThatThrownBy(() -> rename(id, Version.zero(), EmbroideryName.of("Embroidery 3")))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldDeleteEmbroidery() {
        // given: an embroidery
        var id = create(SampleEmbroidery.builder().build());

        // when: deleting the embroidery
        delete(id, Version.zero());

        // then: the embroidery is deleted
        var embroidery = get(id);
        assertThat(embroidery).isNull();
    }

    @Test
    void shouldNotDeleteEmbroideryGivenAnOutdatedVersion() {
        // given: an embroidery
        var id = create(SampleEmbroidery.builder().build());

        // and: the embroidery is renamed
        rename(id, Version.zero(), EmbroideryName.of("Embroidery 2"));

        // when: deleting the embroidery with an outdated version; then: an error is raised
        assertThatThrownBy(() -> delete(id, Version.zero()))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldPublishEmbroidery() {
        // given: an embroidery
        var id = create(SampleEmbroidery.builder().build());

        // when: publishing the embroidery
        var updatedVersion = publish(id, Version.zero());

        // then: the embroidery is published
        var embroidery = get(id);
        assertThat(embroidery.getVersion()).isEqualTo(updatedVersion);
        assertThat(embroidery.isPublished()).isTrue();
    }

    @Test
    void shouldNotPublishEmbroideryGivenAnOutdatedVersion() {
        // given: an embroidery
        var id = create(SampleEmbroidery.builder().build());

        // and: the embroidery is renamed
        rename(id, Version.zero(), EmbroideryName.of("Embroidery 2"));

        // when: publishing the embroidery with an outdated version; then: an error is raised
        assertThatThrownBy(() -> publish(id, Version.zero()))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldRaiseErrorIfEmbroideryAlreadyPublished() {
        // given: a published embroidery
        var id = create(SampleEmbroidery.builder().build());
        publish(id, Version.zero());

        // when: trying to publish the embroidery again; then: an error is raised
        assertThatThrownBy(() -> publish(id, Version.of(1)))
                .isInstanceOf(AlreadyPublishedError.class);
    }

    @Test
    void shouldUnpublishEmbroidery() {
        // given: a published embroidery
        var id = create(SampleEmbroidery.builder().build());
        publish(id, Version.zero());

        // when: unpublishing the embroidery
        var updatedVersion = unpublish(id, Version.of(1));

        // then: the embroidery is unpublished
        var embroidery = get(id);
        assertThat(embroidery.getVersion()).isEqualTo(updatedVersion);
        assertThat(embroidery.isPublished()).isFalse();
    }

    @Test
    void shouldNotUnpublishEmbroideryGivenAnOutdatedVersion() {
        // given: a published embroidery
        var id = create(SampleEmbroidery.builder().build());
        var version = publish(id, Version.zero());

        // and: the embroidery is renamed
        rename(id, version, EmbroideryName.of("Embroidery 2"));

        // when: unpublishing the embroidery with an outdated version; then: an error is raised
        assertThatThrownBy(() -> unpublish(id, Version.of(1)))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldNotUnpublishEmbroideryGivenAnAlreadyUnpublishedEmbroidery() {
        // given: an unpublished embroidery
        var id = create(SampleEmbroidery.builder().build());

        // when: trying to unpublish the embroidery; then: an error is raised
        assertThatThrownBy(() -> unpublish(id, Version.zero()))
                .isInstanceOf(AlreadyUnpublishedError.class);
    }

    @Test
    void shouldFeatureEmbroidery() {
        // given: an embroidery
        var id = create(SampleEmbroidery.builder().build());

        // when: featuring the embroidery
        var updatedVersion = feature(id, Version.zero());

        // then: the embroidery is featured
        var embroidery = get(id);
        assertThat(embroidery.getVersion()).isEqualTo(updatedVersion);
        assertThat(embroidery.isFeatured()).isTrue();
    }

    @Test
    void shouldNotFeatureEmbroideryGivenAnOutdatedVersion() {
        // given: an embroidery
        var id = create(SampleEmbroidery.builder().build());

        // and: the embroidery is renamed
        rename(id, Version.zero(), EmbroideryName.of("Embroidery 2"));

        // when: featuring the embroidery with an outdated version; then: an error is raised
        assertThatThrownBy(() -> feature(id, Version.zero()))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldRaiseErrorIfEmbroideryAlreadyFeatured() {
        // given: a featured embroidery
        var id = create(SampleEmbroidery.builder().build());
        feature(id, Version.zero());

        // when: trying to feature the embroidery again; then: an error is raised
        assertThatThrownBy(() -> feature(id, Version.of(1)))
                .isInstanceOf(AlreadyFeaturedError.class);
    }

    @Test
    void shouldUnfeatureEmbroidery() {
        // given: a featured embroidery
        var id = create(SampleEmbroidery.builder().build());
        feature(id, Version.zero());

        // when: unfeaturing the embroidery
        var updatedVersion = unfeature(id, Version.of(1));

        // then: the embroidery is unfeatured
        var embroidery = get(id);
        assertThat(embroidery.getVersion()).isEqualTo(updatedVersion);
        assertThat(embroidery.isFeatured()).isFalse();
    }

    @Test
    void shouldNotUnfeatureEmbroideryGivenAnOutdatedVersion() {
        // given: a featured embroidery
        var id = create(SampleEmbroidery.builder().build());
        var version = feature(id, Version.zero());

        // and: the embroidery is renamed
        rename(id, version, EmbroideryName.of("Embroidery 2"));

        // when: unfeaturing the embroidery with an outdated version; then: an error is raised
        assertThatThrownBy(() -> unfeature(id, Version.of(1)))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldNotUnfeatureEmbroideryGivenAnAlreadyUnfeaturedEmbroidery() {
        // given: an unfeatured embroidery
        var id = create(SampleEmbroidery.builder().build());

        // when: trying to unfeature the embroidery; then: an error is raised
        assertThatThrownBy(() -> unfeature(id, Version.zero()))
                .isInstanceOf(AlreadyUnfeaturedError.class);
    }

    @Test
    void shouldUpdateImage() {
        // given: an embroidery
        var id = create(SampleEmbroidery.builder().build());

        // when: updating the image
        var updatedVersion = updateImage(id, Version.zero(), ImageId.of("IMAGE_ID_2"));

        // then: the image is updated
        var embroidery = get(id);
        assertThat(embroidery.getVersion()).isEqualTo(updatedVersion);
        assertThat(embroidery.getImage()).isEqualTo(ImageId.of("IMAGE_ID_2"));
    }

    @Test
    void shouldNotUpdateImageGivenAnOutdatedVersion() {
        // given: an embroidery
        var id = create(SampleEmbroidery.builder().build());

        // and: the embroidery is renamed
        rename(id, Version.zero(), EmbroideryName.of("Embroidery 2"));

        // when: updating the image with an outdated version; then: an error is raised
        assertThatThrownBy(() -> updateImage(id, Version.zero(), ImageId.of("IMAGE_ID_2")))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldUpdateCategories() {
        // given: an embroidery
        var id = create(SampleEmbroidery.builder().build());

        // when: updating the categories
        var updatedVersion = updateCategories(id, Version.zero(), Set.of(
                EmbroideryCategoryId.of("ANIMALS"),
                EmbroideryCategoryId.of("FLOWERS")
        ));

        // then: the categories are updated
        var embroidery = get(id);
        assertThat(embroidery.getVersion()).isEqualTo(updatedVersion);
        assertThat(embroidery.getCategories()).containsExactlyInAnyOrder(
                EmbroideryCategoryId.of("ANIMALS"),
                EmbroideryCategoryId.of("FLOWERS")
        );
    }

    @Test
    void shouldNotUpdateCategoriesGivenAnOutdatedVersion() {
        // given: an embroidery
        var id = create(SampleEmbroidery.builder().build());

        // and: the embroidery is renamed
        rename(id, Version.zero(), EmbroideryName.of("Embroidery 2"));

        // when: updating the categories with an outdated version; then: an error is raised
        assertThatThrownBy(() -> updateCategories(id, Version.zero(), Set.of(EmbroideryCategoryId.of("ANIMALS"))))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldRemoveCategory() {
        // given: an embroidery with two categories
        var id = create(SampleEmbroidery.builder()
                .categoryId("ANIMALS")
                .categoryId("FLOWERS")
                .build());

        // when: removing a category
        var updatedVersion = removeCategory(id, Version.zero(), EmbroideryCategoryId.of("ANIMALS"));

        // then: the category is removed
        var embroidery = get(id);
        assertThat(embroidery.getVersion()).isEqualTo(updatedVersion);
        assertThat(embroidery.getCategories()).containsExactly(EmbroideryCategoryId.of("FLOWERS"));
    }

    @Test
    void shouldNotRemoveCategoryThatIsNotInEmbroidery() {
        // given: an embroidery
        var id = create(SampleEmbroidery.builder().categoryId("ANIMALS").build());

        // when: removing a category that is not in the embroidery; then: an error is raised
        assertThatThrownBy(() -> removeCategory(id, Version.zero(), EmbroideryCategoryId.of("FLOWERS")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldNotRemoveCategoryGivenAnOutdatedVersion() {
        // given: an embroidery
        var id = create(SampleEmbroidery.builder().build());

        // and: the embroidery is renamed
        rename(id, Version.zero(), EmbroideryName.of("Embroidery 2"));

        // when: removing the category with an outdated version; then: an error is raised
        assertThatThrownBy(() -> removeCategory(id, Version.zero(), EmbroideryCategoryId.of("CATEGORY_ID")))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldNotApplyCommandsToDeletedEmbroidery() {
        // given: a deleted embroidery
        var id = create(SampleEmbroidery.builder().build());
        delete(id, Version.zero());

        // when: renaming the deleted embroidery; then: an error is raised
        assertThatThrownBy(() -> rename(id, Version.of(1), EmbroideryName.of("Embroidery 2")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldSnapshotEvery100Events() {
        // given: an embroidery
        var id = create(SampleEmbroidery.builder().build());

        // when: updating the embroidery 200 times
        var version = Version.zero();
        for (int i = 0; i < 200; i++) {
            version = rename(id, version, EmbroideryName.of("Embroidery " + i));
        }

        // then: the embroidery is updated
        var embroidery = get(id);
        assertThat(embroidery.getVersion()).isEqualTo(Version.of(202));
        assertThat(embroidery.getName()).isEqualTo(EmbroideryName.of("Embroidery 199"));

        // and: there are exactly 2 snapshot events in the repository
        var events = repo.findEventsByAggregateIdAndType(
                AggregateId.of(id.getValue()),
                Embroidery.TYPE,
                Version.zero()
        ).collectList().block();
        var snapshotEvents = events.stream().filter(e -> e.getMetadata().isSnapshot()).toList();
        assertThat(snapshotEvents).hasSize(2);
        assertThat(snapshotEvents.getFirst().getMetadata().getAggregateVersion()).isEqualTo(Version.of(100));
        assertThat(snapshotEvents.getLast().getMetadata().getAggregateVersion()).isEqualTo(Version.of(200));
    }

    private Embroidery get(EmbroideryId id) {
        return embroideryService.get(id).block();
    }

    private EmbroideryId create(SampleEmbroidery sample) {
        return embroideryService.create(
                sample.getName(),
                sample.getImageId(),
                sample.getCategoryIds(),
                Agent.system()
        ).block().getId();
    }

    private Version delete(EmbroideryId id, Version version) {
        return embroideryService.delete(id, version, Agent.system()).block();
    }

    private Version rename(EmbroideryId id, Version version, EmbroideryName name) {
        return embroideryService.rename(id, version, name, Agent.system()).block();
    }

    private Version publish(EmbroideryId id, Version version) {
        return embroideryService.publish(id, version, Agent.system()).block();
    }

    private Version unpublish(EmbroideryId id, Version version) {
        return embroideryService.unpublish(id, version, Agent.system()).block();
    }

    private Version feature(EmbroideryId id, Version version) {
        return embroideryService.feature(id, version, Agent.system()).block();
    }

    private Version unfeature(EmbroideryId id, Version version) {
        return embroideryService.unfeature(id, version, Agent.system()).block();
    }

    private Version updateImage(EmbroideryId id, Version version, ImageId image) {
        return embroideryService.updateImage(id, version, image, Agent.system()).block();
    }

    private Version updateCategories(EmbroideryId id, Version version, Set<EmbroideryCategoryId> categories) {
        return embroideryService.updateCategories(id, version, categories, Agent.system()).block();
    }

    private Version removeCategory(EmbroideryId id, Version version, EmbroideryCategoryId categoryId) {
        return embroideryService.removeCategory(id, version, categoryId, Agent.system()).block();
    }

}
