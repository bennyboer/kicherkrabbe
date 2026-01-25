package de.bennyboer.kicherkrabbe.patterns;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.patterns.http.api.*;
import de.bennyboer.kicherkrabbe.patterns.samples.SampleMoney;
import de.bennyboer.kicherkrabbe.patterns.samples.SamplePatternAttribution;
import de.bennyboer.kicherkrabbe.patterns.samples.SamplePatternVariant;
import de.bennyboer.kicherkrabbe.patterns.samples.SamplePricedSizeRange;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static de.bennyboer.kicherkrabbe.patterns.http.api.PatternsSortDirectionDTO.ASCENDING;
import static de.bennyboer.kicherkrabbe.patterns.http.api.PatternsSortDirectionDTO.DESCENDING;
import static de.bennyboer.kicherkrabbe.patterns.http.api.PatternsSortPropertyDTO.ALPHABETICAL;
import static org.assertj.core.api.Assertions.assertThat;

public class QueryPublishedPatternsTest extends PatternsModuleTest {

    @Test
    void shouldQueryPublishedPatterns() {
        // given: a user is allowed to create patterns
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: some categories are available
        markCategoryAsAvailable("DRESS_ID", "Dress");
        markCategoryAsAvailable("SKIRT_ID", "Skirt");

        // and: the user creates some patterns
        var variant = SamplePatternVariant.builder().build().toDTO();

        String patternId1 = createPattern(
                "Summerdress",
                "S-D-SUM-1",
                "A dress for high temperatures!",
                SamplePatternAttribution.builder().build().toDTO(),
                Set.of("DRESS_ID"),
                List.of("IMAGE_ID"),
                List.of(variant),
                List.of(),
                agent
        );
        String patternId2 = createPattern(
                "Dressskirt",
                "S-S-DRE-1",
                null,
                SamplePatternAttribution.builder().build().toDTO(),
                Set.of("SKIRT_ID"),
                List.of("IMAGE_ID"),
                List.of(variant),
                List.of(),
                agent
        );

        // when: the user queries the published patterns
        var sort = new PatternsSortDTO();
        sort.property = ALPHABETICAL;
        sort.direction = ASCENDING;

        var result = getPublishedPatterns(
                "",
                Set.of(),
                Set.of(),
                sort,
                0,
                100,
                agent
        );

        // then: the published patterns are returned (there are none)
        assertThat(result.getSkip()).isEqualTo(0);
        assertThat(result.getLimit()).isEqualTo(100);
        assertThat(result.getTotal()).isEqualTo(0);
        assertThat(result.getResults()).isEmpty();

        // when: the patterns are published
        publishPattern(patternId1, 0L, agent);
        publishPattern(patternId2, 0L, agent);

        // and: the user queries the published patterns
        result = getPublishedPatterns(
                "",
                Set.of(),
                Set.of(),
                sort,
                0,
                100,
                agent
        );

        // then: the published patterns are returned
        assertThat(result.getSkip()).isEqualTo(0);
        assertThat(result.getLimit()).isEqualTo(100);
        assertThat(result.getTotal()).isEqualTo(2);
        assertThat(result.getResults()).hasSize(2);

        var pattern1 = result.getResults()
                .stream()
                .filter(f -> f.getId().equals(PatternId.of(patternId1)))
                .findFirst()
                .orElseThrow();
        assertThat(pattern1.getName()).isEqualTo(PatternName.of("Summerdress"));
        assertThat(pattern1.getDescription()).contains(PatternDescription.of("A dress for high temperatures!"));
        assertThat(pattern1.getAlias()).isEqualTo(PatternAlias.of("summerdress"));
        assertThat(pattern1.getImages()).containsExactly(ImageId.of("IMAGE_ID"));
        assertThat(pattern1.getCategories()).containsExactlyInAnyOrder(
                PatternCategoryId.of("DRESS_ID")
        );

        var pattern2 = result.getResults()
                .stream()
                .filter(f -> f.getId().equals(PatternId.of(patternId2)))
                .findFirst()
                .orElseThrow();
        assertThat(pattern2.getName()).isEqualTo(PatternName.of("Dressskirt"));
        assertThat(pattern2.getDescription()).isEmpty();
        assertThat(pattern2.getAlias()).isEqualTo(PatternAlias.of("dressskirt"));
        assertThat(pattern2.getImages()).containsExactly(ImageId.of("IMAGE_ID"));
        assertThat(pattern2.getCategories()).containsExactlyInAnyOrder(
                PatternCategoryId.of("SKIRT_ID")
        );

        // and: the results are sorted alphabetically in ascending order
        assertThat(result.getResults().get(0).getName().getValue()).isEqualTo("Dressskirt");
        assertThat(result.getResults().get(1).getName().getValue()).isEqualTo("Summerdress");

        // when: an anonymous user queries the published patterns
        result = getPublishedPatterns(
                "",
                Set.of(),
                Set.of(),
                sort,
                0,
                100,
                Agent.anonymous()
        );

        // then: the published patterns are returned
        assertThat(result.getSkip()).isEqualTo(0);
        assertThat(result.getLimit()).isEqualTo(100);
        assertThat(result.getTotal()).isEqualTo(2);
        assertThat(result.getResults()).hasSize(2);

        // when: the system user queries the published patterns
        result = getPublishedPatterns(
                "",
                Set.of(),
                Set.of(),
                sort,
                0,
                100,
                Agent.system()
        );

        // then: the published patterns are returned
        assertThat(result.getSkip()).isEqualTo(0);
        assertThat(result.getLimit()).isEqualTo(100);
        assertThat(result.getTotal()).isEqualTo(2);
        assertThat(result.getResults()).hasSize(2);
    }

    @Test
    void shouldFilterBySearchTerm() {
        // given: some categories are available
        markCategoryAsAvailable("DRESS_ID", "Dress");
        markCategoryAsAvailable("SKIRT_ID", "Skirt");

        // and: some patterns are published
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        var variant = SamplePatternVariant.builder().build().toDTO();

        String patternId1 = createPattern(
                "Summerdress",
                "S-D-SUM-1",
                null,
                SamplePatternAttribution.builder().build().toDTO(),
                Set.of("DRESS_ID"),
                List.of("IMAGE_ID"),
                List.of(variant),
                List.of(),
                agent
        );
        String patternId2 = createPattern(
                "Dressskirt",
                "S-S-DRE-1",
                null,
                SamplePatternAttribution.builder().build().toDTO(),
                Set.of("SKIRT_ID"),
                List.of("IMAGE_ID"),
                List.of(variant),
                List.of(),
                agent
        );

        publishPattern(patternId1, 0L, agent);
        publishPattern(patternId2, 0L, agent);

        // when: the user queries the published patterns with a search term
        var sort = new PatternsSortDTO();
        sort.property = ALPHABETICAL;
        sort.direction = ASCENDING;

        var result = getPublishedPatterns(
                "summer",
                Set.of(),
                Set.of(),
                sort,
                0,
                100,
                agent
        );

        // then: the published patterns are returned
        assertThat(result.getSkip()).isEqualTo(0);
        assertThat(result.getLimit()).isEqualTo(100);
        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getResults()).hasSize(1);

        var pattern = result.getResults().get(0);
        assertThat(pattern.getName()).isEqualTo(PatternName.of("Summerdress"));

        // when: the user queries the published patterns with a search term that does not match any pattern
        result = getPublishedPatterns(
                "test",
                Set.of(),
                Set.of(),
                sort,
                0,
                100,
                agent
        );

        // then: no patterns are returned
        assertThat(result.getSkip()).isEqualTo(0);
        assertThat(result.getLimit()).isEqualTo(100);
        assertThat(result.getTotal()).isEqualTo(0);
        assertThat(result.getResults()).isEmpty();

        // when: the user queries the published patterns with a search term that matches multiple patterns
        result = getPublishedPatterns(
                "dress",
                Set.of(),
                Set.of(),
                sort,
                0,
                100,
                agent
        );

        // then: all matching patterns are returned
        assertThat(result.getSkip()).isEqualTo(0);
        assertThat(result.getLimit()).isEqualTo(100);
        assertThat(result.getTotal()).isEqualTo(2);
        assertThat(result.getResults()).hasSize(2);
    }

    @Test
    void shouldSortAlphabeticallyDescending() {
        // given: some categories are available
        markCategoryAsAvailable("DRESS_ID", "Dress");
        markCategoryAsAvailable("SKIRT_ID", "Skirt");

        // and: some patterns are published
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        var variant = SamplePatternVariant.builder().build().toDTO();

        String patternId1 = createPattern(
                "Summerdress",
                "S-D-SUM-1",
                null,
                SamplePatternAttribution.builder().build().toDTO(),
                Set.of("DRESS_ID"),
                List.of("IMAGE_ID"),
                List.of(variant),
                List.of(),
                agent
        );
        String patternId2 = createPattern(
                "Dressskirt",
                "S-S-DRE-1",
                null,
                SamplePatternAttribution.builder().build().toDTO(),
                Set.of("SKIRT_ID"),
                List.of("IMAGE_ID"),
                List.of(variant),
                List.of(),
                agent
        );

        publishPattern(patternId1, 0L, agent);
        publishPattern(patternId2, 0L, agent);

        // when: the user queries the published patterns with a descending alphabetical sort
        var sort = new PatternsSortDTO();
        sort.property = ALPHABETICAL;
        sort.direction = DESCENDING;

        var result = getPublishedPatterns(
                "",
                Set.of(),
                Set.of(),
                sort,
                0,
                100,
                agent
        );

        // then: the results are sorted alphabetically in descending order
        assertThat(result.getResults().get(0).getName().getValue()).isEqualTo("Summerdress");
        assertThat(result.getResults().get(1).getName().getValue()).isEqualTo("Dressskirt");
    }

    @Test
    void shouldFilterByCategories() {
        // given: some categories are available
        markCategoryAsAvailable("DRESS_ID", "Dress");
        markCategoryAsAvailable("SKIRT_ID", "Skirt");

        // and: some patterns are published
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        var variant = SamplePatternVariant.builder().build().toDTO();

        String patternId1 = createPattern(
                "Summerdress",
                "S-D-SUM-1",
                null,
                SamplePatternAttribution.builder().build().toDTO(),
                Set.of("DRESS_ID"),
                List.of("IMAGE_ID"),
                List.of(variant),
                List.of(),
                agent
        );
        String patternId2 = createPattern(
                "Dressskirt",
                "S-S-DRE-1",
                null,
                SamplePatternAttribution.builder().build().toDTO(),
                Set.of("SKIRT_ID"),
                List.of("IMAGE_ID"),
                List.of(variant),
                List.of(),
                agent
        );

        publishPattern(patternId1, 0L, agent);
        publishPattern(patternId2, 0L, agent);

        // when: the user queries the published patterns with a categories filter
        var sort = new PatternsSortDTO();
        sort.property = ALPHABETICAL;
        sort.direction = ASCENDING;

        var result = getPublishedPatterns(
                "",
                Set.of("DRESS_ID"),
                Set.of(),
                sort,
                0,
                100,
                agent
        );

        // then: only the matching patterns are returned
        assertThat(result.getSkip()).isEqualTo(0);
        assertThat(result.getLimit()).isEqualTo(100);
        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getResults()).hasSize(1);

        var pattern = result.getResults().get(0);
        assertThat(pattern.getName()).isEqualTo(PatternName.of("Summerdress"));

        // when: we filter by the skirt category
        result = getPublishedPatterns(
                "",
                Set.of("SKIRT_ID"),
                Set.of(),
                sort,
                0,
                100,
                agent
        );

        // then: only the matching patterns are returned
        assertThat(result.getSkip()).isEqualTo(0);
        assertThat(result.getLimit()).isEqualTo(100);
        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getResults()).hasSize(1);

        pattern = result.getResults().get(0);
        assertThat(pattern.getName()).isEqualTo(PatternName.of("Dressskirt"));
    }

    @Test
    void shouldFilterBySizes() {
        // given: some patterns with different size ranges
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        var pricedSizeRange1 = SamplePricedSizeRange.builder()
                .from(80)
                .to(86L)
                .price(SampleMoney.builder().amount(1000).build())
                .build();

        var pricedSizeRange2 = SamplePricedSizeRange.builder()
                .from(92)
                .to(98L)
                .price(SampleMoney.builder().amount(1200).build())
                .build();

        var variantWithBothSizeRanges = SamplePatternVariant.builder()
                .pricedSizeRange(pricedSizeRange1)
                .pricedSizeRange(pricedSizeRange2)
                .build()
                .toDTO();

        var variantWithSizeRange1 = SamplePatternVariant.builder()
                .pricedSizeRange(pricedSizeRange1)
                .build()
                .toDTO();

        var variantWithSizeRange2 = SamplePatternVariant.builder()
                .pricedSizeRange(pricedSizeRange2)
                .build()
                .toDTO();

        String patternId1 = createPattern(
                "Summerdress 1",
                "S-D-SUM-1",
                null,
                SamplePatternAttribution.builder().build().toDTO(),
                Set.of(),
                List.of("IMAGE_ID"),
                List.of(variantWithBothSizeRanges),
                List.of(),
                agent
        );

        String patternId2 = createPattern(
                "Summerdress 2",
                "S-D-SUM-2",
                null,
                SamplePatternAttribution.builder().build().toDTO(),
                Set.of(),
                List.of("IMAGE_ID"),
                List.of(variantWithSizeRange1),
                List.of(),
                agent
        );

        String patternId3 = createPattern(
                "Summerdress 3",
                "S-D-SUM-3",
                null,
                SamplePatternAttribution.builder().build().toDTO(),
                Set.of(),
                List.of("IMAGE_ID"),
                List.of(variantWithSizeRange2),
                List.of(),
                agent
        );

        String patternId4 = createPattern(
                "Summerdress 4",
                "S-D-SUM-4",
                null,
                SamplePatternAttribution.builder().build().toDTO(),
                Set.of(),
                List.of("IMAGE_ID"),
                List.of(variantWithSizeRange1, variantWithSizeRange2),
                List.of(),
                agent
        );

        publishPattern(patternId1, 0L, agent);
        publishPattern(patternId2, 0L, agent);
        publishPattern(patternId3, 0L, agent);
        publishPattern(patternId4, 0L, agent);

        // when: the user queries the published patterns with a size filter
        var sort = new PatternsSortDTO();
        sort.property = ALPHABETICAL;
        sort.direction = ASCENDING;

        var result = getPublishedPatterns(
                "",
                Set.of(),
                Set.of(86L),
                sort,
                0,
                100,
                agent
        );

        // then: only the matching patterns are returned
        assertThat(result.getSkip()).isEqualTo(0);
        assertThat(result.getLimit()).isEqualTo(100);
        assertThat(result.getTotal()).isEqualTo(3);
        assertThat(result.getResults()).hasSize(3);

        var patternIds = result.getResults()
                .stream()
                .map(PublishedPattern::getId)
                .map(PatternId::getValue)
                .toList();
        assertThat(patternIds).containsExactlyInAnyOrder(patternId1, patternId2, patternId4);

        // when: the user queries the published patterns with another size filter
        result = getPublishedPatterns(
                "",
                Set.of(),
                Set.of(98L),
                sort,
                0,
                100,
                agent
        );

        // then: only the matching patterns are returned
        assertThat(result.getSkip()).isEqualTo(0);
        assertThat(result.getLimit()).isEqualTo(100);
        assertThat(result.getTotal()).isEqualTo(3);
        assertThat(result.getResults()).hasSize(3);

        patternIds = result.getResults()
                .stream()
                .map(PublishedPattern::getId)
                .map(PatternId::getValue)
                .toList();
        assertThat(patternIds).containsExactlyInAnyOrder(patternId1, patternId3, patternId4);

        // when: a size is filtered that does not match any pattern
        result = getPublishedPatterns(
                "",
                Set.of(),
                Set.of(100L),
                sort,
                0,
                100,
                agent
        );

        // then: no patterns are returned
        assertThat(result.getSkip()).isEqualTo(0);
        assertThat(result.getLimit()).isEqualTo(100);
        assertThat(result.getTotal()).isEqualTo(0);
        assertThat(result.getResults()).isEmpty();
    }

    @Test
    void shouldDoPaging() {
        // given: some categories are available
        markCategoryAsAvailable("DRESS_ID", "Dress");
        markCategoryAsAvailable("SKIRT_ID", "Skirt");

        // and: some patterns
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        var variant = SamplePatternVariant.builder().build().toDTO();

        String patternId1 = createPattern(
                "Summerdress",
                "S-D-SUM-1",
                null,
                SamplePatternAttribution.builder().build().toDTO(),
                Set.of("DRESS_ID"),
                List.of("IMAGE_ID"),
                List.of(variant),
                List.of(),
                agent
        );
        String patternId2 = createPattern(
                "Dressskirt",
                "S-S-DRE-1",
                null,
                SamplePatternAttribution.builder().build().toDTO(),
                Set.of("SKIRT_ID"),
                List.of("IMAGE_ID"),
                List.of(variant),
                List.of(),
                agent
        );

        publishPattern(patternId1, 0L, agent);
        publishPattern(patternId2, 0L, agent);

        // when: the user queries the published patterns with a limit
        var sort = new PatternsSortDTO();
        sort.property = ALPHABETICAL;
        sort.direction = ASCENDING;

        var result = getPublishedPatterns(
                "",
                Set.of(),
                Set.of(),
                sort,
                0,
                1,
                agent
        );

        // then: only the first pattern is returned
        assertThat(result.getSkip()).isEqualTo(0);
        assertThat(result.getLimit()).isEqualTo(1);
        assertThat(result.getTotal()).isEqualTo(2);
        assertThat(result.getResults()).hasSize(1);

        var pattern = result.getResults().get(0);
        assertThat(pattern.getName()).isEqualTo(PatternName.of("Dressskirt"));

        // when: the user queries the published patterns with a skip
        result = getPublishedPatterns(
                "",
                Set.of(),
                Set.of(),
                sort,
                1,
                1,
                agent
        );

        // then: only the second pattern is returned
        assertThat(result.getSkip()).isEqualTo(1);
        assertThat(result.getLimit()).isEqualTo(1);
        assertThat(result.getTotal()).isEqualTo(2);
        assertThat(result.getResults()).hasSize(1);

        pattern = result.getResults().get(0);
        assertThat(pattern.getName()).isEqualTo(PatternName.of("Summerdress"));
    }

}
