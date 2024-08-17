package de.bennyboer.kicherkrabbe.patterns;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.patterns.http.api.*;
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
        var variant = new PatternVariantDTO();
        variant.name = "Normal";
        var pricedSizeRange = new PricedSizeRangeDTO();
        pricedSizeRange.from = 80;
        pricedSizeRange.to = 86L;
        pricedSizeRange.price = new MoneyDTO();
        pricedSizeRange.price.amount = 1000;
        pricedSizeRange.price.currency = "EUR";
        variant.pricedSizeRanges = Set.of(pricedSizeRange);

        String patternId1 = createPattern(
                "Summerdress",
                new PatternAttributionDTO(),
                Set.of("DRESS_ID"),
                List.of("IMAGE_ID"),
                List.of(variant),
                List.of(),
                agent
        );
        String patternId2 = createPattern(
                "Dressskirt",
                new PatternAttributionDTO(),
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

        var variant = new PatternVariantDTO();
        variant.name = "Normal";
        var pricedSizeRange = new PricedSizeRangeDTO();
        pricedSizeRange.from = 80;
        pricedSizeRange.to = 86L;
        pricedSizeRange.price = new MoneyDTO();
        pricedSizeRange.price.amount = 1000;
        pricedSizeRange.price.currency = "EUR";
        variant.pricedSizeRanges = Set.of(pricedSizeRange);

        String patternId1 = createPattern(
                "Summerdress",
                new PatternAttributionDTO(),
                Set.of("DRESS_ID"),
                List.of("IMAGE_ID"),
                List.of(variant),
                List.of(),
                agent
        );
        String patternId2 = createPattern(
                "Dressskirt",
                new PatternAttributionDTO(),
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

        var variant = new PatternVariantDTO();
        variant.name = "Normal";
        var pricedSizeRange = new PricedSizeRangeDTO();
        pricedSizeRange.from = 80;
        pricedSizeRange.to = 86L;
        pricedSizeRange.price = new MoneyDTO();
        pricedSizeRange.price.amount = 1000;
        pricedSizeRange.price.currency = "EUR";
        variant.pricedSizeRanges = Set.of(pricedSizeRange);

        String patternId1 = createPattern(
                "Summerdress",
                new PatternAttributionDTO(),
                Set.of("DRESS_ID"),
                List.of("IMAGE_ID"),
                List.of(variant),
                List.of(),
                agent
        );
        String patternId2 = createPattern(
                "Dressskirt",
                new PatternAttributionDTO(),
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

        var variant = new PatternVariantDTO();
        variant.name = "Normal";
        var pricedSizeRange = new PricedSizeRangeDTO();
        pricedSizeRange.from = 80;
        pricedSizeRange.to = 86L;
        pricedSizeRange.price = new MoneyDTO();
        pricedSizeRange.price.amount = 1000;
        pricedSizeRange.price.currency = "EUR";
        variant.pricedSizeRanges = Set.of(pricedSizeRange);

        String patternId1 = createPattern(
                "Summerdress",
                new PatternAttributionDTO(),
                Set.of("DRESS_ID"),
                List.of("IMAGE_ID"),
                List.of(variant),
                List.of(),
                agent
        );
        String patternId2 = createPattern(
                "Dressskirt",
                new PatternAttributionDTO(),
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

        var pricedSizeRange1 = new PricedSizeRangeDTO();
        pricedSizeRange1.from = 80;
        pricedSizeRange1.to = 86L;
        pricedSizeRange1.price = new MoneyDTO();
        pricedSizeRange1.price.amount = 1000;
        pricedSizeRange1.price.currency = "EUR";

        var pricedSizeRange2 = new PricedSizeRangeDTO();
        pricedSizeRange2.from = 92;
        pricedSizeRange2.to = 98L;
        pricedSizeRange2.price = new MoneyDTO();
        pricedSizeRange2.price.amount = 1200;
        pricedSizeRange2.price.currency = "EUR";

        var variantWithBothSizeRanges = new PatternVariantDTO();
        variantWithBothSizeRanges.name = "Normal";
        variantWithBothSizeRanges.pricedSizeRanges = Set.of(pricedSizeRange1, pricedSizeRange2);

        var variantWithSizeRange1 = new PatternVariantDTO();
        variantWithSizeRange1.name = "Normal";
        variantWithSizeRange1.pricedSizeRanges = Set.of(pricedSizeRange1);

        var variantWithSizeRange2 = new PatternVariantDTO();
        variantWithSizeRange2.name = "Normal";
        variantWithSizeRange2.pricedSizeRanges = Set.of(pricedSizeRange2);

        String patternId1 = createPattern(
                "Summerdress 1",
                new PatternAttributionDTO(),
                Set.of(),
                List.of("IMAGE_ID"),
                List.of(variantWithBothSizeRanges),
                List.of(),
                agent
        );

        String patternId2 = createPattern(
                "Summerdress 2",
                new PatternAttributionDTO(),
                Set.of(),
                List.of("IMAGE_ID"),
                List.of(variantWithSizeRange1),
                List.of(),
                agent
        );

        String patternId3 = createPattern(
                "Summerdress 3",
                new PatternAttributionDTO(),
                Set.of(),
                List.of("IMAGE_ID"),
                List.of(variantWithSizeRange2),
                List.of(),
                agent
        );

        String patternId4 = createPattern(
                "Summerdress 4",
                new PatternAttributionDTO(),
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

        var variant = new PatternVariantDTO();
        variant.name = "Normal";
        var pricedSizeRange = new PricedSizeRangeDTO();
        pricedSizeRange.from = 80;
        pricedSizeRange.to = 86L;
        pricedSizeRange.price = new MoneyDTO();
        pricedSizeRange.price.amount = 1000;
        pricedSizeRange.price.currency = "EUR";
        variant.pricedSizeRanges = Set.of(pricedSizeRange);

        String patternId1 = createPattern(
                "Summerdress",
                new PatternAttributionDTO(),
                Set.of("DRESS_ID"),
                List.of("IMAGE_ID"),
                List.of(variant),
                List.of(),
                agent
        );
        String patternId2 = createPattern(
                "Dressskirt",
                new PatternAttributionDTO(),
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
