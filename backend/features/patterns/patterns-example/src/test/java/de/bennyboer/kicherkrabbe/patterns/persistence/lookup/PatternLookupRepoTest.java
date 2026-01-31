package de.bennyboer.kicherkrabbe.patterns.persistence.lookup;

import de.bennyboer.kicherkrabbe.money.Money;
import de.bennyboer.kicherkrabbe.patterns.*;
import de.bennyboer.kicherkrabbe.patterns.samples.SampleLookupPattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class PatternLookupRepoTest {

    private PatternLookupRepo repo;

    protected abstract PatternLookupRepo createRepo();

    @BeforeEach
    public void setUp() {
        repo = createRepo();
    }

    @Test
    void shouldUpdatePattern() {
        var pattern = SampleLookupPattern.builder()
                .name(PatternName.of("Summerdress"))
                .number(PatternNumber.of("S-D-SUM-1"))
                .description(PatternDescription.of("A beautiful summer dress"))
                .alias(PatternAlias.of("summerdress"))
                .attribution(PatternAttribution.of(
                        OriginalPatternName.of("Summerdress EXTREME"),
                        PatternDesigner.of("EXTREME PATTERNS inc.")
                ))
                .category(PatternCategoryId.of("DRESS_ID"))
                .category(PatternCategoryId.of("SKIRT_ID"))
                .image(ImageId.of("IMAGE_ID_1"))
                .image(ImageId.of("IMAGE_ID_2"))
                .variant(PatternVariant.of(
                        PatternVariantName.of("Short"),
                        Set.of(
                                PricedSizeRange.of(86, 92L, null, Money.euro(2900)),
                                PricedSizeRange.of(98, 104L, null, Money.euro(3100))
                        )
                ))
                .extra(PatternExtra.of(PatternExtraName.of("Sewing instructions"), Money.euro(200)))
                .published(true)
                .build()
                .toModel();

        update(pattern);

        var patterns = find(Set.of(pattern.getId()));
        assertThat(patterns).containsExactly(pattern);
    }

    @Test
    void shouldRemovePattern() {
        var pattern1 = SampleLookupPattern.builder()
                .name(PatternName.of("Summerdress"))
                .number(PatternNumber.of("S-D-SUM-1"))
                .alias(PatternAlias.of("summerdress"))
                .attribution(PatternAttribution.of(
                        OriginalPatternName.of("Summerdress EXTREME"),
                        PatternDesigner.of("EXTREME PATTERNS inc.")
                ))
                .category(PatternCategoryId.of("DRESS_ID"))
                .category(PatternCategoryId.of("SKIRT_ID"))
                .image(ImageId.of("IMAGE_ID_1"))
                .image(ImageId.of("IMAGE_ID_2"))
                .variant(PatternVariant.of(
                        PatternVariantName.of("Short"),
                        Set.of(
                                PricedSizeRange.of(86, 92L, null, Money.euro(2900)),
                                PricedSizeRange.of(98, 104L, null, Money.euro(3100))
                        )
                ))
                .extra(PatternExtra.of(PatternExtraName.of("Sewing instructions"), Money.euro(200)))
                .published(true)
                .build()
                .toModel();
        var pattern2 = SampleLookupPattern.builder()
                .name(PatternName.of("Some trousers"))
                .number(PatternNumber.of("S-T-SOM-1"))
                .alias(PatternAlias.of("some-trousers"))
                .attribution(PatternAttribution.of(
                        OriginalPatternName.of("Summerdress EXTREME"),
                        PatternDesigner.of("EXTREME PATTERNS inc.")
                ))
                .category(PatternCategoryId.of("TROUSERS_ID"))
                .image(ImageId.of("IMAGE_ID_3"))
                .variant(PatternVariant.of(
                        PatternVariantName.of("Normal"),
                        Set.of(PricedSizeRange.of(86, 92L, null, Money.euro(2900)))
                ))
                .published(false)
                .build()
                .toModel();
        update(pattern1);
        update(pattern2);

        remove(pattern1.getId());

        var patterns = find(Set.of(pattern1.getId(), pattern2.getId()));
        assertThat(patterns).containsExactly(pattern2);
    }

    @Test
    void shouldFindPatterns() {
        var pattern1 = SampleLookupPattern.builder()
                .name(PatternName.of("Summerdress"))
                .number(PatternNumber.of("S-D-SUM-1"))
                .alias(PatternAlias.of("summerdress"))
                .attribution(PatternAttribution.of(
                        OriginalPatternName.of("Summerdress EXTREME"),
                        PatternDesigner.of("EXTREME PATTERNS inc.")
                ))
                .category(PatternCategoryId.of("DRESS_ID"))
                .category(PatternCategoryId.of("SKIRT_ID"))
                .image(ImageId.of("IMAGE_ID_1"))
                .image(ImageId.of("IMAGE_ID_2"))
                .variant(PatternVariant.of(
                        PatternVariantName.of("Short"),
                        Set.of(
                                PricedSizeRange.of(86, 92L, null, Money.euro(2900)),
                                PricedSizeRange.of(98, 104L, null, Money.euro(3100))
                        )
                ))
                .extra(PatternExtra.of(PatternExtraName.of("Sewing instructions"), Money.euro(200)))
                .published(true)
                .createdAt(Instant.parse("2024-03-12T12:30:00.00Z"))
                .build()
                .toModel();
        var pattern2 = SampleLookupPattern.builder()
                .name(PatternName.of("Some trousers"))
                .number(PatternNumber.of("S-T-SOM-1"))
                .alias(PatternAlias.of("some-trousers"))
                .category(PatternCategoryId.of("TROUSERS_ID"))
                .image(ImageId.of("IMAGE_ID_3"))
                .variant(PatternVariant.of(
                        PatternVariantName.of("Normal"),
                        Set.of(PricedSizeRange.of(86, 92L, null, Money.euro(2900)))
                ))
                .published(false)
                .createdAt(Instant.parse("2024-03-12T12:00:00.00Z"))
                .build()
                .toModel();
        update(pattern1);
        update(pattern2);

        var patterns = find(Set.of(pattern1.getId(), pattern2.getId()));

        assertThat(patterns).containsExactly(pattern2, pattern1);
    }

    @Test
    void shouldFindPatternsBySearchTerm() {
        var pattern1 = SampleLookupPattern.builder()
                .name(PatternName.of("Summerdress"))
                .number(PatternNumber.of("S-D-SUM-1"))
                .alias(PatternAlias.of("summerdress"))
                .attribution(PatternAttribution.of(
                        OriginalPatternName.of("Summerdress EXTREME"),
                        PatternDesigner.of("EXTREME PATTERNS inc.")
                ))
                .category(PatternCategoryId.of("DRESS_ID"))
                .category(PatternCategoryId.of("SKIRT_ID"))
                .image(ImageId.of("IMAGE_ID_1"))
                .image(ImageId.of("IMAGE_ID_2"))
                .variant(PatternVariant.of(
                        PatternVariantName.of("Short"),
                        Set.of(
                                PricedSizeRange.of(86, 92L, null, Money.euro(2900)),
                                PricedSizeRange.of(98, 104L, null, Money.euro(3100))
                        )
                ))
                .extra(PatternExtra.of(PatternExtraName.of("Sewing instructions"), Money.euro(200)))
                .published(true)
                .createdAt(Instant.parse("2024-03-12T12:30:00.00Z"))
                .build()
                .toModel();
        var pattern2 = SampleLookupPattern.builder()
                .name(PatternName.of("Some trousers"))
                .number(PatternNumber.of("S-T-SOM-1"))
                .alias(PatternAlias.of("some-trousers"))
                .category(PatternCategoryId.of("TROUSERS_ID"))
                .image(ImageId.of("IMAGE_ID_3"))
                .variant(PatternVariant.of(
                        PatternVariantName.of("Normal"),
                        Set.of(PricedSizeRange.of(86, 92L, null, Money.euro(2900)))
                ))
                .published(false)
                .createdAt(Instant.parse("2024-03-12T12:00:00.00Z"))
                .build()
                .toModel();
        var pattern3 = SampleLookupPattern.builder()
                .name(PatternName.of("A little hat"))
                .number(PatternNumber.of("S-H-ALH-1"))
                .alias(PatternAlias.of("a-little-hat"))
                .category(PatternCategoryId.of("HATS_ID"))
                .image(ImageId.of("IMAGE_ID_3"))
                .variant(PatternVariant.of(
                        PatternVariantName.of("Normal"),
                        Set.of(PricedSizeRange.of(86, 92L, null, Money.euro(2900)))
                ))
                .published(false)
                .createdAt(Instant.parse("2024-03-11T11:00:00.00Z"))
                .build()
                .toModel();
        update(pattern1);
        update(pattern2);
        update(pattern3);

        var patternIds = Set.of(pattern1.getId(), pattern2.getId(), pattern3.getId());
        var patterns = find(patternIds, "o");

        assertThat(patterns).containsExactly(pattern2);

        patterns = find(patternIds, "r");

        assertThat(patterns).containsExactly(pattern2, pattern1);

        patterns = find(patternIds, "    ");

        assertThat(patterns).containsExactly(pattern3, pattern2, pattern1);

        patterns = find(patternIds, "blblblbll");

        assertThat(patterns).isEmpty();
    }

    @Test
    void shouldFindPatternsWithPaging() {
        var pattern1 = SampleLookupPattern.builder()
                .name(PatternName.of("Summerdress"))
                .number(PatternNumber.of("S-D-SUM-1"))
                .alias(PatternAlias.of("summerdress"))
                .attribution(PatternAttribution.of(
                        OriginalPatternName.of("Summerdress EXTREME"),
                        PatternDesigner.of("EXTREME PATTERNS inc.")
                ))
                .category(PatternCategoryId.of("DRESS_ID"))
                .category(PatternCategoryId.of("SKIRT_ID"))
                .image(ImageId.of("IMAGE_ID_1"))
                .image(ImageId.of("IMAGE_ID_2"))
                .variant(PatternVariant.of(
                        PatternVariantName.of("Short"),
                        Set.of(
                                PricedSizeRange.of(86, 92L, null, Money.euro(2900)),
                                PricedSizeRange.of(98, 104L, null, Money.euro(3100))
                        )
                ))
                .extra(PatternExtra.of(PatternExtraName.of("Sewing instructions"), Money.euro(200)))
                .published(true)
                .createdAt(Instant.parse("2024-03-12T12:30:00.00Z"))
                .build()
                .toModel();
        var pattern2 = SampleLookupPattern.builder()
                .name(PatternName.of("Some trousers"))
                .number(PatternNumber.of("S-T-SOM-1"))
                .alias(PatternAlias.of("some-trousers"))
                .category(PatternCategoryId.of("TROUSERS_ID"))
                .image(ImageId.of("IMAGE_ID_3"))
                .variant(PatternVariant.of(
                        PatternVariantName.of("Normal"),
                        Set.of(PricedSizeRange.of(86, 92L, null, Money.euro(2900)))
                ))
                .published(false)
                .createdAt(Instant.parse("2024-03-12T12:00:00.00Z"))
                .build()
                .toModel();
        var pattern3 = SampleLookupPattern.builder()
                .name(PatternName.of("A little hat"))
                .number(PatternNumber.of("S-H-ALH-1"))
                .alias(PatternAlias.of("a-little-hat"))
                .category(PatternCategoryId.of("HATS_ID"))
                .image(ImageId.of("IMAGE_ID_3"))
                .variant(PatternVariant.of(
                        PatternVariantName.of("Normal"),
                        Set.of(PricedSizeRange.of(86, 92L, null, Money.euro(2900)))
                ))
                .published(false)
                .createdAt(Instant.parse("2024-03-11T11:00:00.00Z"))
                .build()
                .toModel();
        update(pattern1);
        update(pattern2);
        update(pattern3);

        var patternIds = Set.of(pattern1.getId(), pattern2.getId(), pattern3.getId());
        var patterns = find(patternIds, 1, 1);

        assertThat(patterns).containsExactly(pattern2);

        patterns = find(patternIds, 2, 1);

        assertThat(patterns).containsExactly(pattern1);

        patterns = find(patternIds, 3, 1);

        assertThat(patterns).isEmpty();

        patterns = find(patternIds, 0, 2);

        assertThat(patterns).containsExactly(pattern3, pattern2);
    }

    @Test
    void shouldFindWithSearchTermAndPaging() {
        var pattern1 = SampleLookupPattern.builder()
                .name(PatternName.of("Summerdress"))
                .number(PatternNumber.of("S-D-SUM-1"))
                .alias(PatternAlias.of("summerdress"))
                .attribution(PatternAttribution.of(
                        OriginalPatternName.of("Summerdress EXTREME"),
                        PatternDesigner.of("EXTREME PATTERNS inc.")
                ))
                .category(PatternCategoryId.of("DRESS_ID"))
                .category(PatternCategoryId.of("SKIRT_ID"))
                .image(ImageId.of("IMAGE_ID_1"))
                .image(ImageId.of("IMAGE_ID_2"))
                .variant(PatternVariant.of(
                        PatternVariantName.of("Short"),
                        Set.of(
                                PricedSizeRange.of(86, 92L, null, Money.euro(2900)),
                                PricedSizeRange.of(98, 104L, null, Money.euro(3100))
                        )
                ))
                .extra(PatternExtra.of(PatternExtraName.of("Sewing instructions"), Money.euro(200)))
                .published(true)
                .createdAt(Instant.parse("2024-03-12T12:30:00.00Z"))
                .build()
                .toModel();
        var pattern2 = SampleLookupPattern.builder()
                .name(PatternName.of("Some trousers"))
                .number(PatternNumber.of("S-T-SOM-1"))
                .alias(PatternAlias.of("some-trousers"))
                .category(PatternCategoryId.of("TROUSERS_ID"))
                .image(ImageId.of("IMAGE_ID_3"))
                .variant(PatternVariant.of(
                        PatternVariantName.of("Normal"),
                        Set.of(PricedSizeRange.of(86, 92L, null, Money.euro(2900)))
                ))
                .published(false)
                .createdAt(Instant.parse("2024-03-12T12:00:00.00Z"))
                .build()
                .toModel();
        var pattern3 = SampleLookupPattern.builder()
                .name(PatternName.of("A little hat"))
                .number(PatternNumber.of("S-H-ALH-1"))
                .alias(PatternAlias.of("a-little-hat"))
                .category(PatternCategoryId.of("HATS_ID"))
                .image(ImageId.of("IMAGE_ID_3"))
                .variant(PatternVariant.of(
                        PatternVariantName.of("Normal"),
                        Set.of(PricedSizeRange.of(86, 92L, null, Money.euro(2900)))
                ))
                .published(false)
                .createdAt(Instant.parse("2024-03-11T11:00:00.00Z"))
                .build()
                .toModel();
        update(pattern1);
        update(pattern2);
        update(pattern3);

        var patternIds = Set.of(pattern1.getId(), pattern2.getId(), pattern3.getId());
        var page = findPage(patternIds, Set.of(), "r", 0, 1);

        assertThat(page.getResults()).containsExactly(pattern2);
        assertThat(page.getTotal()).isEqualTo(2);

        page = findPage(patternIds, Set.of(), "hat", 1, 1);

        assertThat(page.getResults()).isEmpty();
        assertThat(page.getTotal()).isEqualTo(1);
    }

    @Test
    void shouldFindPublishedPattern() {
        var pattern1 = SampleLookupPattern.builder()
                .name(PatternName.of("Summerdress"))
                .number(PatternNumber.of("S-D-SUM-1"))
                .alias(PatternAlias.of("summerdress"))
                .attribution(PatternAttribution.of(
                        OriginalPatternName.of("Summerdress EXTREME"),
                        PatternDesigner.of("EXTREME PATTERNS inc.")
                ))
                .category(PatternCategoryId.of("DRESS_ID"))
                .category(PatternCategoryId.of("SKIRT_ID"))
                .image(ImageId.of("IMAGE_ID_1"))
                .image(ImageId.of("IMAGE_ID_2"))
                .variant(PatternVariant.of(
                        PatternVariantName.of("Short"),
                        Set.of(
                                PricedSizeRange.of(86, 92L, null, Money.euro(2900)),
                                PricedSizeRange.of(98, 104L, null, Money.euro(3100))
                        )
                ))
                .extra(PatternExtra.of(PatternExtraName.of("Sewing instructions"), Money.euro(200)))
                .published(true)
                .createdAt(Instant.parse("2024-03-12T12:30:00.00Z"))
                .build()
                .toModel();
        var pattern2 = SampleLookupPattern.builder()
                .name(PatternName.of("Some trousers"))
                .number(PatternNumber.of("S-T-SOM-1"))
                .alias(PatternAlias.of("some-trousers"))
                .category(PatternCategoryId.of("TROUSERS_ID"))
                .image(ImageId.of("IMAGE_ID_3"))
                .variant(PatternVariant.of(
                        PatternVariantName.of("Normal"),
                        Set.of(PricedSizeRange.of(86, 92L, null, Money.euro(2900)))
                ))
                .published(false)
                .createdAt(Instant.parse("2024-03-12T12:00:00.00Z"))
                .build()
                .toModel();
        var pattern3 = SampleLookupPattern.builder()
                .name(PatternName.of("A little hat"))
                .number(PatternNumber.of("S-H-ALH-1"))
                .alias(PatternAlias.of("a-little-hat"))
                .category(PatternCategoryId.of("HATS_ID"))
                .image(ImageId.of("IMAGE_ID_3"))
                .variant(PatternVariant.of(
                        PatternVariantName.of("Normal"),
                        Set.of(PricedSizeRange.of(86, 92L, null, Money.euro(2900)))
                ))
                .published(false)
                .createdAt(Instant.parse("2024-03-11T11:00:00.00Z"))
                .build()
                .toModel();
        update(pattern1);
        update(pattern2);
        update(pattern3);

        var foundPattern1 = findPublished(pattern1.getId());

        assertThat(foundPattern1).isEqualTo(pattern1);

        var foundPattern2 = findPublished(pattern2.getId());

        assertThat(foundPattern2).isNull();
    }

    @Test
    void shouldFindPublishedPatterns() {
        var pattern1 = SampleLookupPattern.builder()
                .name(PatternName.of("C"))
                .number(PatternNumber.of("S-D-SUM-1"))
                .alias(PatternAlias.of("c"))
                .attribution(PatternAttribution.of(
                        OriginalPatternName.of("Summerdress EXTREME"),
                        PatternDesigner.of("EXTREME PATTERNS inc.")
                ))
                .category(PatternCategoryId.of("DRESS_ID"))
                .category(PatternCategoryId.of("SKIRT_ID"))
                .image(ImageId.of("IMAGE_ID_1"))
                .image(ImageId.of("IMAGE_ID_2"))
                .variant(PatternVariant.of(
                        PatternVariantName.of("Short"),
                        Set.of(
                                PricedSizeRange.of(86, 92L, null, Money.euro(2900)),
                                PricedSizeRange.of(98, 104L, null, Money.euro(3100))
                        )
                ))
                .extra(PatternExtra.of(PatternExtraName.of("Sewing instructions"), Money.euro(200)))
                .published(true)
                .createdAt(Instant.parse("2024-03-12T12:30:00.00Z"))
                .build()
                .toModel();
        var pattern2 = SampleLookupPattern.builder()
                .name(PatternName.of("B"))
                .number(PatternNumber.of("S-T-SOM-1"))
                .alias(PatternAlias.of("b"))
                .category(PatternCategoryId.of("TROUSERS_ID"))
                .image(ImageId.of("IMAGE_ID_3"))
                .variant(PatternVariant.of(
                        PatternVariantName.of("Normal"),
                        Set.of(PricedSizeRange.of(86, 92L, null, Money.euro(2900)))
                ))
                .published(false)
                .createdAt(Instant.parse("2024-03-12T12:00:00.00Z"))
                .build()
                .toModel();
        var pattern3 = SampleLookupPattern.builder()
                .name(PatternName.of("A"))
                .number(PatternNumber.of("S-H-ALH-1"))
                .alias(PatternAlias.of("a"))
                .category(PatternCategoryId.of("HATS_ID"))
                .image(ImageId.of("IMAGE_ID_3"))
                .variant(PatternVariant.of(
                        PatternVariantName.of("Normal"),
                        Set.of(PricedSizeRange.of(86, 92L, null, Money.euro(2900)))
                ))
                .published(true)
                .createdAt(Instant.parse("2024-03-11T11:00:00.00Z"))
                .build()
                .toModel();
        update(pattern1);
        update(pattern2);
        update(pattern3);

        var result = findPublished("", Set.of(), Set.of(), true, 0, 10);

        assertThat(result.getResults()).containsExactly(pattern3, pattern1);

        result = findPublished("a", Set.of(), Set.of(), false, 0, 10);

        assertThat(result.getResults()).containsExactly(pattern3);

        result = findPublished("C", Set.of(), Set.of(), true, 0, 10);

        assertThat(result.getResults()).containsExactly(pattern1);

        result = findPublished("", Set.of(PatternCategoryId.of("HATS_ID")), Set.of(), true, 0, 10);

        assertThat(result.getResults()).containsExactly(pattern3);

        result = findPublished("", Set.of(), Set.of(92L), true, 0, 10);

        assertThat(result.getResults()).containsExactlyInAnyOrder(pattern1, pattern3);

        result = findPublished("", Set.of(), Set.of(104L), true, 0, 10);

        assertThat(result.getResults()).containsExactly(pattern1);

        result = findPublished("", Set.of(), Set.of(170L), true, 0, 10);

        assertThat(result.getResults()).isEmpty();

        result = findPublished("", Set.of(), Set.of(86L, 104L), true, 0, 10);

        assertThat(result.getResults()).containsExactlyInAnyOrder(pattern1, pattern3);

        result = findPublished("", Set.of(), Set.of(), true, 0, 1);

        assertThat(result.getResults()).containsExactly(pattern3);

        result = findPublished("", Set.of(), Set.of(), true, 1, 1);

        assertThat(result.getResults()).containsExactly(pattern1);
    }

    @Test
    void shouldFindPatternsByCategory() {
        var categoryId1 = PatternCategoryId.of("CATEGORY_ID_1");
        var categoryId2 = PatternCategoryId.of("CATEGORY_ID_2");
        var categoryId3 = PatternCategoryId.of("CATEGORY_ID_3");

        var pattern1 = SampleLookupPattern.builder()
                .name(PatternName.of("C"))
                .number(PatternNumber.of("S-D-SUM-1"))
                .alias(PatternAlias.of("c"))
                .attribution(PatternAttribution.of(
                        OriginalPatternName.of("Summerdress EXTREME"),
                        PatternDesigner.of("EXTREME PATTERNS inc.")
                ))
                .category(categoryId1)
                .category(categoryId3)
                .image(ImageId.of("IMAGE_ID_1"))
                .image(ImageId.of("IMAGE_ID_2"))
                .variant(PatternVariant.of(
                        PatternVariantName.of("Short"),
                        Set.of(
                                PricedSizeRange.of(86, 92L, null, Money.euro(2900)),
                                PricedSizeRange.of(98, 104L, null, Money.euro(3100))
                        )
                ))
                .extra(PatternExtra.of(PatternExtraName.of("Sewing instructions"), Money.euro(200)))
                .published(true)
                .createdAt(Instant.parse("2024-03-12T12:30:00.00Z"))
                .build()
                .toModel();
        var pattern2 = SampleLookupPattern.builder()
                .name(PatternName.of("B"))
                .number(PatternNumber.of("S-T-SOM-1"))
                .alias(PatternAlias.of("b"))
                .category(categoryId2)
                .image(ImageId.of("IMAGE_ID_3"))
                .variant(PatternVariant.of(
                        PatternVariantName.of("Normal"),
                        Set.of(PricedSizeRange.of(86, 92L, null, Money.euro(2900)))
                ))
                .published(false)
                .createdAt(Instant.parse("2024-03-12T12:00:00.00Z"))
                .build()
                .toModel();
        var pattern3 = SampleLookupPattern.builder()
                .name(PatternName.of("A"))
                .number(PatternNumber.of("S-H-ALH-1"))
                .alias(PatternAlias.of("a"))
                .category(categoryId3)
                .category(categoryId2)
                .image(ImageId.of("IMAGE_ID_3"))
                .variant(PatternVariant.of(
                        PatternVariantName.of("Normal"),
                        Set.of(PricedSizeRange.of(86, 92L, null, Money.euro(2900)))
                ))
                .published(true)
                .createdAt(Instant.parse("2024-03-11T11:00:00.00Z"))
                .build()
                .toModel();
        update(pattern1);
        update(pattern2);
        update(pattern3);

        var patterns = findByCategory(categoryId2);

        assertThat(patterns).containsExactlyInAnyOrder(pattern2, pattern3);

        patterns = findByCategory(categoryId1);

        assertThat(patterns).containsExactly(pattern1);

        patterns = findByCategory(PatternCategoryId.of("CATEGORY_ID_4"));

        assertThat(patterns).isEmpty();
    }

    @Test
    void shouldFindUniqueCategories() {
        var categoryId1 = PatternCategoryId.of("CATEGORY_ID_1");
        var categoryId2 = PatternCategoryId.of("CATEGORY_ID_2");
        var categoryId3 = PatternCategoryId.of("CATEGORY_ID_3");

        var pattern1 = SampleLookupPattern.builder()
                .name(PatternName.of("C"))
                .number(PatternNumber.of("S-D-SUM-1"))
                .alias(PatternAlias.of("c"))
                .attribution(PatternAttribution.of(
                        OriginalPatternName.of("Summerdress EXTREME"),
                        PatternDesigner.of("EXTREME PATTERNS inc.")
                ))
                .category(categoryId1)
                .category(categoryId3)
                .image(ImageId.of("IMAGE_ID_1"))
                .image(ImageId.of("IMAGE_ID_2"))
                .variant(PatternVariant.of(
                        PatternVariantName.of("Short"),
                        Set.of(
                                PricedSizeRange.of(86, 92L, null, Money.euro(2900)),
                                PricedSizeRange.of(98, 104L, null, Money.euro(3100))
                        )
                ))
                .extra(PatternExtra.of(PatternExtraName.of("Sewing instructions"), Money.euro(200)))
                .published(true)
                .createdAt(Instant.parse("2024-03-12T12:30:00.00Z"))
                .build()
                .toModel();
        var pattern2 = SampleLookupPattern.builder()
                .name(PatternName.of("B"))
                .number(PatternNumber.of("S-T-SOM-1"))
                .alias(PatternAlias.of("b"))
                .category(categoryId2)
                .image(ImageId.of("IMAGE_ID_3"))
                .variant(PatternVariant.of(
                        PatternVariantName.of("Normal"),
                        Set.of(PricedSizeRange.of(86, 92L, null, Money.euro(2900)))
                ))
                .published(false)
                .createdAt(Instant.parse("2024-03-12T12:00:00.00Z"))
                .build()
                .toModel();
        var pattern3 = SampleLookupPattern.builder()
                .name(PatternName.of("A"))
                .number(PatternNumber.of("S-H-ALH-1"))
                .alias(PatternAlias.of("a"))
                .category(categoryId3)
                .category(categoryId2)
                .image(ImageId.of("IMAGE_ID_3"))
                .variant(PatternVariant.of(
                        PatternVariantName.of("Normal"),
                        Set.of(PricedSizeRange.of(86, 92L, null, Money.euro(2900)))
                ))
                .published(true)
                .createdAt(Instant.parse("2024-03-11T11:00:00.00Z"))
                .build()
                .toModel();
        update(pattern1);
        update(pattern2);
        update(pattern3);

        var categories = findUniqueCategories();

        assertThat(categories).containsExactlyInAnyOrder(categoryId1, categoryId2, categoryId3);

        remove(pattern1.getId());
        remove(pattern2.getId());
        remove(pattern3.getId());

        categories = findUniqueCategories();

        assertThat(categories).isEmpty();
    }

    @Test
    void shouldFindPatternByAlias() {
        var pattern1 = SampleLookupPattern.builder()
                .name(PatternName.of("Summerdress"))
                .number(PatternNumber.of("S-D-SUM-1"))
                .alias(PatternAlias.of("summerdress"))
                .attribution(PatternAttribution.of(
                        OriginalPatternName.of("Summerdress EXTREME"),
                        PatternDesigner.of("EXTREME PATTERNS inc.")
                ))
                .category(PatternCategoryId.of("DRESS_ID"))
                .category(PatternCategoryId.of("SKIRT_ID"))
                .image(ImageId.of("IMAGE_ID_1"))
                .image(ImageId.of("IMAGE_ID_2"))
                .variant(PatternVariant.of(
                        PatternVariantName.of("Short"),
                        Set.of(
                                PricedSizeRange.of(86, 92L, null, Money.euro(2900)),
                                PricedSizeRange.of(98, 104L, null, Money.euro(3100))
                        )
                ))
                .extra(PatternExtra.of(PatternExtraName.of("Sewing instructions"), Money.euro(200)))
                .published(true)
                .createdAt(Instant.parse("2024-03-12T12:30:00.00Z"))
                .build()
                .toModel();

        var pattern2 = SampleLookupPattern.builder()
                .name(PatternName.of("Some trousers"))
                .number(PatternNumber.of("S-T-SOM-1"))
                .alias(PatternAlias.of("some-trousers"))
                .category(PatternCategoryId.of("TROUSERS_ID"))
                .image(ImageId.of("IMAGE_ID_3"))
                .variant(PatternVariant.of(
                        PatternVariantName.of("Normal"),
                        Set.of(PricedSizeRange.of(86, 92L, null, Money.euro(2900)))
                ))
                .published(false)
                .createdAt(Instant.parse("2024-03-12T12:00:00.00Z"))
                .build()
                .toModel();

        update(pattern1);
        update(pattern2);

        var foundPattern1 = findByAlias(PatternAlias.of("summerdress"));

        assertThat(foundPattern1).isEqualTo(pattern1);

        var foundPattern2 = findByAlias(PatternAlias.of("some-trousers"));

        assertThat(foundPattern2).isEqualTo(pattern2);

        var foundPattern3 = findByAlias(PatternAlias.of("non-existing-alias"));

        assertThat(foundPattern3).isNull();
    }

    private List<PatternCategoryId> findUniqueCategories() {
        return repo.findUniqueCategories().collectList().block();
    }

    private List<LookupPattern> findByCategory(PatternCategoryId categoryId) {
        return repo.findByCategory(categoryId).collectList().block();
    }

    private LookupPattern findPublished(PatternId id) {
        return repo.findPublished(id).block();
    }

    private LookupPattern findByAlias(PatternAlias alias) {
        return repo.findByAlias(alias).block();
    }

    private LookupPatternPage findPublished(
            String searchTerm,
            Set<PatternCategoryId> categories,
            Set<Long> sizes,
            boolean ascending,
            long skip,
            long limit
    ) {
        return repo.findPublished(searchTerm, categories, sizes, ascending, skip, limit).block();
    }

    private void update(LookupPattern pattern) {
        repo.update(pattern).block();
    }

    private void remove(PatternId patternId) {
        repo.remove(patternId).block();
    }

    private List<LookupPattern> find(Set<PatternId> patternIds) {
        return find(patternIds, Set.of(), "", 0, Integer.MAX_VALUE);
    }

    private List<LookupPattern> find(Set<PatternId> patternIds, String searchTerm) {
        return find(patternIds, Set.of(), searchTerm, 0, Integer.MAX_VALUE);
    }

    private List<LookupPattern> find(Set<PatternId> patternIds, long skip, long limit) {
        return find(patternIds, Set.of(), "", skip, limit);
    }

    private List<LookupPattern> find(
            Set<PatternId> patternIds,
            Set<PatternCategoryId> categories,
            String searchTerm,
            long skip,
            long limit
    ) {
        return repo.find(patternIds, categories, searchTerm, skip, limit).block().getResults();
    }

    private LookupPatternPage findPage(
            Set<PatternId> patternIds,
            Set<PatternCategoryId> categories,
            String searchTerm,
            long skip,
            long limit
    ) {
        return repo.find(patternIds, categories, searchTerm, skip, limit).block();
    }

}
