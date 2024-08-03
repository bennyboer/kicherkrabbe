package de.bennyboer.kicherkrabbe.patterns;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.LoggingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.inmemory.InMemoryEventSourcingRepo;
import de.bennyboer.kicherkrabbe.money.Money;
import de.bennyboer.kicherkrabbe.patterns.publish.AlreadyPublishedError;
import de.bennyboer.kicherkrabbe.patterns.unpublish.AlreadyUnpublishedError;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PatternServiceTest {

    private final EventSourcingRepo repo = new InMemoryEventSourcingRepo();

    private final LoggingEventPublisher eventPublisher = new LoggingEventPublisher();

    private final PatternService patternService = new PatternService(repo, eventPublisher);

    @Test
    void shouldCreatePattern() {
        // given: some details to create a pattern for
        var name = PatternName.of("Sommerkleid");
        var attribution = PatternAttribution.of(
                OriginalPatternName.of("Sommerkleid EXTREME"),
                PatternDesigner.of("EXTREME PATTERNS")
        );
        var categories = Set.of(PatternCategoryId.of("CATEGORY_ID_1"), PatternCategoryId.of("CATEGORY_ID_2"));
        var images = List.of(ImageId.of("IMAGE_ID_1"));
        var variants = List.of(
                PatternVariant.of(
                        PatternVariantName.of("Kurze Variante"),
                        Set.of(
                                PricedSizeRange.of(80L, 86L, "EU", Money.euro(2900)),
                                PricedSizeRange.of(92L, 98L, "EU", Money.euro(3100)),
                                PricedSizeRange.of(104L, 110L, "EU", Money.euro(3300)),
                                PricedSizeRange.of(116L, null, "EU", Money.euro(3500))
                        )
                ),
                PatternVariant.of(
                        PatternVariantName.of("Lange Variante"),
                        Set.of(
                                PricedSizeRange.of(80L, 86L, "EU", Money.euro(3100)),
                                PricedSizeRange.of(92L, 98L, "EU", Money.euro(3300)),
                                PricedSizeRange.of(104L, 110L, "EU", Money.euro(3500)),
                                PricedSizeRange.of(116L, null, "EU", Money.euro(3700))
                        )
                )
        );
        var extras = List.of(
                PatternExtra.of(
                        PatternExtraName.of("Verstärkte Bündchen"),
                        Money.euro(200)
                ),
                PatternExtra.of(
                        PatternExtraName.of("Knopfleiste"),
                        Money.euro(300)
                )
        );

        // when: creating a pattern
        var id = create(
                name,
                attribution,
                categories,
                images,
                variants,
                extras
        );

        // then: the pattern is created
        var pattern = get(id);
        assertThat(pattern.getId()).isEqualTo(id);
        assertThat(pattern.getVersion()).isEqualTo(Version.zero());
        assertThat(pattern.getName()).isEqualTo(name);
        assertThat(pattern.isNotDeleted()).isTrue();
        assertThat(pattern.getAttribution()).isEqualTo(attribution);
        assertThat(pattern.getCategories()).isEqualTo(categories);
        assertThat(pattern.getImages()).isEqualTo(images);
        assertThat(pattern.getVariants()).isEqualTo(variants);
        assertThat(pattern.getExtras()).isEqualTo(extras);
    }

    @Test
    void shouldCreatePatternWithMinimalData() {
        // given: some minimal details to create a pattern for
        var name = PatternName.of("Sommerkleid");
        var images = List.of(ImageId.of("IMAGE_ID_1"));
        var variants = List.of(PatternVariant.of(
                PatternVariantName.of("Kurze Variante"),
                Set.of(
                        PricedSizeRange.of(80L, 86L, "EU", Money.euro(2900))
                )
        ));

        // when: creating a pattern
        var id = create(
                name,
                PatternAttribution.empty(),
                Set.of(),
                images,
                variants,
                List.of()
        );

        // then: the pattern is created
        var pattern = get(id);
        assertThat(pattern.getId()).isEqualTo(id);
        assertThat(pattern.getVersion()).isEqualTo(Version.zero());
        assertThat(pattern.getName()).isEqualTo(name);
        assertThat(pattern.isNotDeleted()).isTrue();
        assertThat(pattern.getAttribution()).isEqualTo(PatternAttribution.empty());
        assertThat(pattern.getCategories()).isEmpty();
        assertThat(pattern.getImages()).isEqualTo(images);
        assertThat(pattern.getVariants()).isEqualTo(variants);
        assertThat(pattern.getExtras()).isEmpty();
    }

    @Test
    void shouldFailCreatingPatternGivenNoImages() {
        // when: creating a pattern without images; then: an error is raised
        assertThatThrownBy(() -> create(
                PatternName.of("Sommerkleid"),
                PatternAttribution.empty(),
                Set.of(),
                List.of(),
                List.of(PatternVariant.of(
                        PatternVariantName.of("Short"),
                        Set.of(PricedSizeRange.of(80L, 86L, "EU", Money.euro(2900)))
                )),
                List.of()
        )).matches(e -> e instanceof IllegalArgumentException);
    }

    @Test
    void shouldFailCreatingPatternGivenNoVariants() {
        // when: creating a pattern without variants; then: an error is raised
        assertThatThrownBy(() -> create(
                PatternName.of("Sommerkleid"),
                PatternAttribution.empty(),
                Set.of(),
                List.of(ImageId.of("IMAGE_ID_1")),
                List.of(),
                List.of()
        )).matches(e -> e instanceof IllegalArgumentException);
    }

    @Test
    void shouldRenamePattern() {
        // given: a pattern
        var id = create(
                PatternName.of("Sommerkleid"),
                PatternAttribution.empty(),
                Set.of(),
                List.of(ImageId.of("IMAGE_ID_1")),
                List.of(PatternVariant.of(
                        PatternVariantName.of("Short"),
                        Set.of(PricedSizeRange.of(80L, 86L, "EU", Money.euro(2900)))
                )),
                List.of()
        );

        // when: renaming the pattern
        var updatedVersion = rename(id, Version.zero(), PatternName.of("Sommerkleid 2024"));

        // then: the pattern is renamed
        var pattern = get(id);
        assertThat(pattern.getId()).isEqualTo(id);
        assertThat(pattern.getVersion()).isEqualTo(updatedVersion);
        assertThat(pattern.getName()).isEqualTo(PatternName.of("Sommerkleid 2024"));
        assertThat(pattern.isNotDeleted()).isTrue();
    }

    @Test
    void shouldNotRenamePatternGivenAnOutdatedVersion() {
        // given: a pattern
        var id = create(
                PatternName.of("Sommerkleid"),
                PatternAttribution.empty(),
                Set.of(),
                List.of(ImageId.of("IMAGE_ID_1")),
                List.of(PatternVariant.of(
                        PatternVariantName.of("Short"),
                        Set.of(PricedSizeRange.of(80L, 86L, "EU", Money.euro(2900)))
                )),
                List.of()
        );

        // and: the pattern is renamed
        rename(id, Version.zero(), PatternName.of("Sommerkleid 2024"));

        // when: renaming the pattern with an outdated version; then: an error is raised
        assertThatThrownBy(() -> rename(id, Version.zero(), PatternName.of("Sommerkleid 2025")))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldDeletePattern() {
        // given: a pattern
        var id = create(
                PatternName.of("Sommerkleid"),
                PatternAttribution.empty(),
                Set.of(),
                List.of(ImageId.of("IMAGE_ID_1")),
                List.of(
                        PatternVariant.of(
                                PatternVariantName.of("Short"),
                                Set.of(PricedSizeRange.of(80L, 86L, "EU", Money.euro(2900)))
                        )
                ),
                List.of()
        );

        // when: deleting the pattern
        delete(id, Version.zero());

        // then: the pattern is deleted
        assertThat(get(id)).isNull();
    }

    @Test
    void shouldNotDeletePatternGivenAnOutdatedVersion() {
        // given: a pattern
        var id = create(
                PatternName.of("Sommerkleid"),
                PatternAttribution.empty(),
                Set.of(),
                List.of(ImageId.of("IMAGE_ID_1")),
                List.of(
                        PatternVariant.of(
                                PatternVariantName.of("Short"),
                                Set.of(PricedSizeRange.of(80L, 86L, "EU", Money.euro(2900)))
                        )
                ),
                List.of()
        );

        // and: the pattern is renamed
        rename(id, Version.zero(), PatternName.of("Sommerkleid 2024"));

        // when: deleting the pattern with an outdated version; then: an error is raised
        assertThatThrownBy(() -> delete(id, Version.zero()))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldUpdateCategories() {
        // given: a pattern
        var id = create(
                PatternName.of("Sommerkleid"),
                PatternAttribution.empty(),
                Set.of(PatternCategoryId.of("CATEGORY_ID_1")),
                List.of(ImageId.of("IMAGE_ID_1")),
                List.of(
                        PatternVariant.of(
                                PatternVariantName.of("Short"),
                                Set.of(PricedSizeRange.of(80L, 86L, "EU", Money.euro(2900)))
                        )
                ),
                List.of()
        );

        // when: updating the categories
        var updatedVersion = patternService.updateCategories(
                id,
                Version.zero(),
                Set.of(PatternCategoryId.of("CATEGORY_ID_1"), PatternCategoryId.of("CATEGORY_ID_2")),
                Agent.system()
        ).block();

        // then: the categories are updated
        var pattern = get(id);
        assertThat(pattern.getId()).isEqualTo(id);
        assertThat(pattern.getVersion()).isEqualTo(updatedVersion);
        assertThat(pattern.getCategories()).containsExactlyInAnyOrder(
                PatternCategoryId.of("CATEGORY_ID_1"),
                PatternCategoryId.of("CATEGORY_ID_2")
        );
    }

    @Test
    void shouldNotUpdateCategoriesGivenAnOutdatedVersion() {
        // given: a pattern
        var id = create(
                PatternName.of("Sommerkleid"),
                PatternAttribution.empty(),
                Set.of(PatternCategoryId.of("CATEGORY_ID_1")),
                List.of(ImageId.of("IMAGE_ID_1")),
                List.of(
                        PatternVariant.of(
                                PatternVariantName.of("Short"),
                                Set.of(PricedSizeRange.of(80L, 86L, "EU", Money.euro(2900)))
                        )
                ),
                List.of()
        );

        // and: the pattern is renamed
        rename(id, Version.zero(), PatternName.of("Sommerkleid 2024"));

        // when: updating the categories with an outdated version; then: an error is raised
        assertThatThrownBy(() -> updateCategories(
                id,
                Version.zero(),
                Set.of(PatternCategoryId.of("CATEGORY_ID_1"), PatternCategoryId.of("CATEGORY_ID_2"))
        )).matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldUpdateAttribution() {
        // given: a pattern
        var id = create(
                PatternName.of("Sommerkleid"),
                PatternAttribution.empty(),
                Set.of(),
                List.of(ImageId.of("IMAGE_ID_1")),
                List.of(
                        PatternVariant.of(
                                PatternVariantName.of("Short"),
                                Set.of(PricedSizeRange.of(80L, 86L, "EU", Money.euro(2900)))
                        )
                ),
                List.of()
        );

        // when: updating the attribution
        var updatedVersion = updateAttribution(
                id,
                Version.zero(),
                PatternAttribution.of(
                        OriginalPatternName.of("Sommerkleid EXTREME"),
                        PatternDesigner.of("EXTREME PATTERNS")
                )
        );

        // then: the attribution is updated
        var pattern = get(id);
        assertThat(pattern.getId()).isEqualTo(id);
        assertThat(pattern.getVersion()).isEqualTo(updatedVersion);
        assertThat(pattern.getAttribution()).isEqualTo(
                PatternAttribution.of(
                        OriginalPatternName.of("Sommerkleid EXTREME"),
                        PatternDesigner.of("EXTREME PATTERNS")
                )
        );
    }

    @Test
    void shouldNotUpdateAttributionGivenAnOutdatedVersion() {
        // given: a pattern
        var id = create(
                PatternName.of("Sommerkleid"),
                PatternAttribution.empty(),
                Set.of(),
                List.of(ImageId.of("IMAGE_ID_1")),
                List.of(
                        PatternVariant.of(
                                PatternVariantName.of("Short"),
                                Set.of(PricedSizeRange.of(80L, 86L, "EU", Money.euro(2900)))
                        )
                ),
                List.of()
        );

        // and: the pattern is renamed
        rename(id, Version.zero(), PatternName.of("Sommerkleid 2024"));

        // when: updating the attribution with an outdated version; then: an error is raised
        assertThatThrownBy(() -> updateAttribution(
                id,
                Version.zero(),
                PatternAttribution.of(
                        OriginalPatternName.of("Sommerkleid EXTREME"),
                        PatternDesigner.of("EXTREME PATTERNS")
                )
        )).matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldUpdateImages() {
        // given: a pattern
        var id = create(
                PatternName.of("Sommerkleid"),
                PatternAttribution.empty(),
                Set.of(),
                List.of(ImageId.of("IMAGE_ID_1")),
                List.of(
                        PatternVariant.of(
                                PatternVariantName.of("Short"),
                                Set.of(PricedSizeRange.of(80L, 86L, "EU", Money.euro(2900)))
                        )
                ),
                List.of()
        );

        // when: updating the images
        var updatedVersion = updateImages(
                id,
                Version.zero(),
                List.of(
                        ImageId.of("IMAGE_ID_2"),
                        ImageId.of("IMAGE_ID_3")
                )
        );

        // then: the images are updated
        var pattern = get(id);
        assertThat(pattern.getId()).isEqualTo(id);
        assertThat(pattern.getVersion()).isEqualTo(updatedVersion);
        assertThat(pattern.getImages()).containsExactlyInAnyOrder(
                ImageId.of("IMAGE_ID_2"),
                ImageId.of("IMAGE_ID_3")
        );
    }

    @Test
    void shouldNotUpdateImagesGivenAnOutdatedVersion() {
        // given: a pattern
        var id = create(
                PatternName.of("Sommerkleid"),
                PatternAttribution.empty(),
                Set.of(),
                List.of(ImageId.of("IMAGE_ID_1")),
                List.of(
                        PatternVariant.of(
                                PatternVariantName.of("Short"),
                                Set.of(PricedSizeRange.of(80L, 86L, "EU", Money.euro(2900)))
                        )
                ),
                List.of()
        );

        // and: the pattern is renamed
        rename(id, Version.zero(), PatternName.of("Sommerkleid 2024"));

        // when: updating the images with an outdated version; then: an error is raised
        assertThatThrownBy(() -> updateImages(
                id,
                Version.zero(),
                List.of(
                        ImageId.of("IMAGE_ID_2"),
                        ImageId.of("IMAGE_ID_3")
                )
        )).matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldUpdateVariants() {
        // given: a pattern
        var id = create(
                PatternName.of("Sommerkleid"),
                PatternAttribution.empty(),
                Set.of(),
                List.of(ImageId.of("IMAGE_ID_1")),
                List.of(
                        PatternVariant.of(
                                PatternVariantName.of("Short"),
                                Set.of(PricedSizeRange.of(80L, 86L, "EU", Money.euro(2900)))
                        )
                ),
                List.of()
        );

        // when: updating the variants
        var updatedVersion = updateVariants(
                id,
                Version.zero(),
                List.of(
                        PatternVariant.of(
                                PatternVariantName.of("Short"),
                                Set.of(
                                        PricedSizeRange.of(80L, 86L, "EU", Money.euro(2900)),
                                        PricedSizeRange.of(92L, 98L, "EU", Money.euro(3100))
                                )
                        ),
                        PatternVariant.of(
                                PatternVariantName.of("Long"),
                                Set.of(
                                        PricedSizeRange.of(80L, 86L, "EU", Money.euro(3100)),
                                        PricedSizeRange.of(92L, 98L, "EU", Money.euro(3300))
                                )
                        )
                )
        );

        // then: the variants are updated
        var pattern = get(id);
        assertThat(pattern.getId()).isEqualTo(id);
        assertThat(pattern.getVersion()).isEqualTo(updatedVersion);
        assertThat(pattern.getVariants()).containsExactlyInAnyOrder(
                PatternVariant.of(
                        PatternVariantName.of("Short"),
                        Set.of(
                                PricedSizeRange.of(80L, 86L, "EU", Money.euro(2900)),
                                PricedSizeRange.of(92L, 98L, "EU", Money.euro(3100))
                        )
                ),
                PatternVariant.of(
                        PatternVariantName.of("Long"),
                        Set.of(
                                PricedSizeRange.of(80L, 86L, "EU", Money.euro(3100)),
                                PricedSizeRange.of(92L, 98L, "EU", Money.euro(3300))
                        )
                )
        );
    }

    @Test
    void shouldNotUpdateVariantsGivenAnOutdatedVersion() {
        // given: a pattern
        var id = create(
                PatternName.of("Sommerkleid"),
                PatternAttribution.empty(),
                Set.of(),
                List.of(ImageId.of("IMAGE_ID_1")),
                List.of(
                        PatternVariant.of(
                                PatternVariantName.of("Short"),
                                Set.of(PricedSizeRange.of(80L, 86L, "EU", Money.euro(2900)))
                        )
                ),
                List.of()
        );

        // and: the pattern is renamed
        rename(id, Version.zero(), PatternName.of("Sommerkleid 2024"));

        // when: updating the variants with an outdated version; then: an error is raised
        assertThatThrownBy(() -> updateVariants(
                id,
                Version.zero(),
                List.of(
                        PatternVariant.of(
                                PatternVariantName.of("Short"),
                                Set.of(
                                        PricedSizeRange.of(80L, 86L, "EU", Money.euro(2900)),
                                        PricedSizeRange.of(92L, 98L, "EU", Money.euro(3100))
                                )
                        ),
                        PatternVariant.of(
                                PatternVariantName.of("Long"),
                                Set.of(
                                        PricedSizeRange.of(80L, 86L, "EU", Money.euro(3100)),
                                        PricedSizeRange.of(92L, 98L, "EU", Money.euro(3300))
                                )
                        )
                )
        )).matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldUpdateExtras() {
        // given: a pattern
        var id = create(
                PatternName.of("Sommerkleid"),
                PatternAttribution.empty(),
                Set.of(),
                List.of(ImageId.of("IMAGE_ID_1")),
                List.of(
                        PatternVariant.of(
                                PatternVariantName.of("Short"),
                                Set.of(PricedSizeRange.of(80L, 86L, "EU", Money.euro(2900)))
                        )
                ),
                List.of(
                        PatternExtra.of(
                                PatternExtraName.of("Verstärkte Bündchen"),
                                Money.euro(200)
                        )
                )
        );

        // when: updating the extras
        var updatedVersion = updateExtras(
                id,
                Version.zero(),
                List.of(
                        PatternExtra.of(
                                PatternExtraName.of("Verstärkte Bündchen"),
                                Money.euro(300)
                        ),
                        PatternExtra.of(
                                PatternExtraName.of("Knopfleiste"),
                                Money.euro(400)
                        )
                )
        );

        // then: the extras are updated
        var pattern = get(id);
        assertThat(pattern.getId()).isEqualTo(id);
        assertThat(pattern.getVersion()).isEqualTo(updatedVersion);
        assertThat(pattern.getExtras()).containsExactlyInAnyOrder(
                PatternExtra.of(
                        PatternExtraName.of("Verstärkte Bündchen"),
                        Money.euro(300)
                ),
                PatternExtra.of(
                        PatternExtraName.of("Knopfleiste"),
                        Money.euro(400)
                )
        );
    }

    @Test
    void shouldNotUpdateExtrasGivenAnOutdatedVersion() {
        // given: a pattern
        var id = create(
                PatternName.of("Sommerkleid"),
                PatternAttribution.empty(),
                Set.of(),
                List.of(ImageId.of("IMAGE_ID_1")),
                List.of(
                        PatternVariant.of(
                                PatternVariantName.of("Short"),
                                Set.of(PricedSizeRange.of(80L, 86L, "EU", Money.euro(2900)))
                        )
                ),
                List.of(
                        PatternExtra.of(
                                PatternExtraName.of("Verstärkte Bündchen"),
                                Money.euro(200)
                        )
                )
        );

        // and: the pattern is renamed
        rename(id, Version.zero(), PatternName.of("Sommerkleid 2024"));

        // when: updating the extras with an outdated version; then: an error is raised
        assertThatThrownBy(() -> updateExtras(
                id,
                Version.zero(),
                List.of(
                        PatternExtra.of(
                                PatternExtraName.of("Verstärkte Bündchen"),
                                Money.euro(300)
                        ),
                        PatternExtra.of(
                                PatternExtraName.of("Knopfleiste"),
                                Money.euro(400)
                        )
                )
        )).matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldPublishPattern() {
        // given: a pattern
        var id = create(
                PatternName.of("Sommerkleid"),
                PatternAttribution.empty(),
                Set.of(),
                List.of(ImageId.of("IMAGE_ID_1")),
                List.of(
                        PatternVariant.of(
                                PatternVariantName.of("Short"),
                                Set.of(PricedSizeRange.of(80L, 86L, "EU", Money.euro(2900)))
                        )
                ),
                List.of()
        );

        // when: publishing the pattern
        var updatedVersion = publish(id, Version.zero());

        // then: the pattern is published
        var pattern = get(id);
        assertThat(pattern.getId()).isEqualTo(id);
        assertThat(pattern.getVersion()).isEqualTo(updatedVersion);
        assertThat(pattern.isPublished()).isTrue();
    }

    @Test
    void shouldNotPublishPatternGivenAnOutdatedVersion() {
        // given: a pattern
        var id = create(
                PatternName.of("Sommerkleid"),
                PatternAttribution.empty(),
                Set.of(),
                List.of(ImageId.of("IMAGE_ID_1")),
                List.of(
                        PatternVariant.of(
                                PatternVariantName.of("Short"),
                                Set.of(PricedSizeRange.of(80L, 86L, "EU", Money.euro(2900)))
                        )
                ),
                List.of()
        );

        // and: the pattern is renamed
        rename(id, Version.zero(), PatternName.of("Sommerkleid 2024"));

        // when: publishing the pattern with an outdated version; then: an error is raised
        assertThatThrownBy(() -> publish(id, Version.zero()))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldUnpublishPattern() {
        // given: a published pattern
        var id = create(
                PatternName.of("Sommerkleid"),
                PatternAttribution.empty(),
                Set.of(),
                List.of(ImageId.of("IMAGE_ID_1")),
                List.of(
                        PatternVariant.of(
                                PatternVariantName.of("Short"),
                                Set.of(PricedSizeRange.of(80L, 86L, "EU", Money.euro(2900)))
                        )
                ),
                List.of()
        );
        var version = publish(id, Version.zero());

        // when: unpublishing the pattern
        var updatedVersion = unpublish(id, version);

        // then: the pattern is unpublished
        var pattern = get(id);
        assertThat(pattern.getId()).isEqualTo(id);
        assertThat(pattern.getVersion()).isEqualTo(updatedVersion);
        assertThat(pattern.isPublished()).isFalse();
    }

    @Test
    void shouldNotUnpublishPatternGivenAnOutdatedVersion() {
        // given: a published pattern
        var id = create(
                PatternName.of("Sommerkleid"),
                PatternAttribution.empty(),
                Set.of(),
                List.of(ImageId.of("IMAGE_ID_1")),
                List.of(
                        PatternVariant.of(
                                PatternVariantName.of("Short"),
                                Set.of(PricedSizeRange.of(80L, 86L, "EU", Money.euro(2900)))
                        )
                ),
                List.of()
        );
        var version = publish(id, Version.zero());

        // and: the pattern is renamed
        rename(id, version, PatternName.of("Sommerkleid 2024"));

        // when: unpublishing the pattern with an outdated version; then: an error is raised
        assertThatThrownBy(() -> unpublish(id, version))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldNotBeAbleToPublishAlreadyPublishedPattern() {
        // given: a published pattern
        var id = create(
                PatternName.of("Sommerkleid"),
                PatternAttribution.empty(),
                Set.of(),
                List.of(ImageId.of("IMAGE_ID_1")),
                List.of(
                        PatternVariant.of(
                                PatternVariantName.of("Short"),
                                Set.of(PricedSizeRange.of(80L, 86L, "EU", Money.euro(2900)))
                        )
                ),
                List.of()
        );
        var version = publish(id, Version.zero());

        // when: trying to publish the pattern again; then: an error is raised
        assertThatThrownBy(() -> publish(id, version))
                .matches(e -> e instanceof AlreadyPublishedError);
    }

    @Test
    void shouldNotBeAbleToUnpublishAlreadyUnpublishedPattern() {
        // given: an unpublished pattern
        var id = create(
                PatternName.of("Sommerkleid"),
                PatternAttribution.empty(),
                Set.of(),
                List.of(ImageId.of("IMAGE_ID_1")),
                List.of(
                        PatternVariant.of(
                                PatternVariantName.of("Short"),
                                Set.of(PricedSizeRange.of(80L, 86L, "EU", Money.euro(2900)))
                        )
                ),
                List.of()
        );

        // when: trying to unpublish the pattern; then: an error is raised
        assertThatThrownBy(() -> unpublish(id, Version.zero()))
                .matches(e -> e instanceof AlreadyUnpublishedError);
    }

    @Test
    void shouldRestoreFromSnapshot() {
        // given: a pattern
        var name = PatternName.of("Sommerkleid");
        var attribution = PatternAttribution.of(
                OriginalPatternName.of("Sommerkleid EXTREME"),
                PatternDesigner.of("EXTREME PATTERNS")
        );
        var categories = Set.of(PatternCategoryId.of("CATEGORY_ID_1"), PatternCategoryId.of("CATEGORY_ID_2"));
        var images = List.of(ImageId.of("IMAGE_ID_1"));
        var variants = List.of(
                PatternVariant.of(
                        PatternVariantName.of("Kurze Variante"),
                        Set.of(
                                PricedSizeRange.of(80L, 86L, "EU", Money.euro(2900)),
                                PricedSizeRange.of(92L, 98L, "EU", Money.euro(3100)),
                                PricedSizeRange.of(104L, 110L, "EU", Money.euro(3300)),
                                PricedSizeRange.of(116L, null, "EU", Money.euro(3500))
                        )
                ),
                PatternVariant.of(
                        PatternVariantName.of("Lange Variante"),
                        Set.of(
                                PricedSizeRange.of(80L, 86L, "EU", Money.euro(3100)),
                                PricedSizeRange.of(92L, 98L, "EU", Money.euro(3300)),
                                PricedSizeRange.of(104L, 110L, "EU", Money.euro(3500)),
                                PricedSizeRange.of(116L, null, "EU", Money.euro(3700))
                        )
                )
        );
        var extras = List.of(
                PatternExtra.of(
                        PatternExtraName.of("Verstärkte Bündchen"),
                        Money.euro(200)
                ),
                PatternExtra.of(
                        PatternExtraName.of("Knopfleiste"),
                        Money.euro(300)
                )
        );
        var id = create(
                name,
                attribution,
                categories,
                images,
                variants,
                extras
        );

        // when: renaming the pattern 99 times
        var version = Version.zero();
        for (int i = 0; i < 99; i++) {
            version = rename(id, version, PatternName.of("Sommerkleid " + i));
        }

        // then: a snapshot event is present
        var events = repo.findEventsByAggregateIdAndType(
                AggregateId.of(id.getValue()),
                Pattern.TYPE,
                Version.zero()
        ).collectList().block();
        var snapshotEvent = events.getLast();
        assertThat(snapshotEvent.getMetadata().isSnapshot()).isTrue();
        assertThat(snapshotEvent.getMetadata().getAggregateVersion()).isEqualTo(Version.of(100));

        // when: aggregating the pattern
        var pattern = patternService.get(id).block();

        // then: the pattern is restored from the snapshot
        assertThat(pattern.getVersion()).isEqualTo(Version.of(100));
        assertThat(pattern.isPublished()).isFalse();
        assertThat(pattern.getName()).isEqualTo(PatternName.of("Sommerkleid 98"));
        assertThat(pattern.getAttribution()).isEqualTo(attribution);
        assertThat(pattern.getCategories()).isEqualTo(categories);
        assertThat(pattern.getImages()).isEqualTo(images);
        assertThat(pattern.getVariants()).isEqualTo(variants);
        assertThat(pattern.getExtras()).isEqualTo(extras);
        assertThat(pattern.isNotDeleted()).isTrue();
    }

    @Test
    void shouldSnapshotEvery100Events() {
        // given: a pattern
        var id = create(
                PatternName.of("Sommerkleid"),
                PatternAttribution.empty(),
                Set.of(),
                List.of(ImageId.of("IMAGE_ID_1")),
                List.of(PatternVariant.of(
                        PatternVariantName.of("Short"),
                        Set.of(PricedSizeRange.of(80L, 86L, "EU", Money.euro(2900)))
                )),
                List.of()
        );

        // when: renaming the pattern 200 times
        var version = Version.zero();
        for (int i = 0; i < 200; i++) {
            version = rename(id, version, PatternName.of("Sommerkleid " + i));
        }

        // then: the pattern is renamed
        var pattern = get(id);
        assertThat(pattern.getVersion()).isEqualTo(Version.of(202));
        assertThat(pattern.getName()).isEqualTo(PatternName.of("Sommerkleid 199"));

        // and: there are exactly 2 snapshot events in the repository
        var events = repo.findEventsByAggregateIdAndType(
                AggregateId.of(id.getValue()),
                Pattern.TYPE,
                Version.zero()
        ).collectList().block();
        var snapshotEvents = events.stream().filter(e -> e.getMetadata().isSnapshot()).toList();
        assertThat(snapshotEvents).hasSize(2);
        assertThat(snapshotEvents.getFirst().getMetadata().getAggregateVersion()).isEqualTo(Version.of(100));
        assertThat(snapshotEvents.getLast().getMetadata().getAggregateVersion()).isEqualTo(Version.of(200));
    }

    private Pattern get(PatternId id) {
        return patternService.get(id).block();
    }

    private PatternId create(
            PatternName name,
            PatternAttribution attribution,
            Set<PatternCategoryId> categories,
            List<ImageId> images,
            List<PatternVariant> variants,
            List<PatternExtra> extras
    ) {
        return patternService.create(
                name,
                attribution,
                categories,
                images,
                variants,
                extras,
                Agent.system()
        ).block().getId();
    }

    private Version publish(PatternId id, Version version) {
        return patternService.publish(id, version, Agent.system()).block();
    }

    private Version unpublish(PatternId id, Version version) {
        return patternService.unpublish(id, version, Agent.system()).block();
    }

    private Version rename(PatternId id, Version version, PatternName name) {
        return patternService.rename(id, version, name, Agent.system()).block();
    }

    private Version updateAttribution(PatternId id, Version version, PatternAttribution attribution) {
        return patternService.updateAttribution(id, version, attribution, Agent.system()).block();
    }

    private Version updateCategories(PatternId id, Version version, Set<PatternCategoryId> categories) {
        return patternService.updateCategories(id, version, categories, Agent.system()).block();
    }

    private Version updateImages(PatternId id, Version version, List<ImageId> images) {
        return patternService.updateImages(id, version, images, Agent.system()).block();
    }

    private Version updateVariants(PatternId id, Version version, List<PatternVariant> variants) {
        return patternService.updateVariants(id, version, variants, Agent.system()).block();
    }

    private Version updateExtras(PatternId id, Version version, List<PatternExtra> extras) {
        return patternService.updateExtras(id, version, extras, Agent.system()).block();
    }

    private void delete(PatternId id, Version version) {
        patternService.delete(id, version, Agent.system()).block();
    }

}
