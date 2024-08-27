package de.bennyboer.kicherkrabbe.patterns;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.patterns.http.api.MoneyDTO;
import de.bennyboer.kicherkrabbe.patterns.http.api.PatternAttributionDTO;
import de.bennyboer.kicherkrabbe.patterns.http.api.PatternVariantDTO;
import de.bennyboer.kicherkrabbe.patterns.http.api.PricedSizeRangeDTO;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class CleanupCategoriesTest extends PatternsModuleTest {

    @Test
    void shouldCleanupDeletedCategoryFromPatterns() {
        // given: a user is allowed to create patterns
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: some categories are available
        markCategoryAsAvailable("DRESS_ID", "Dress");
        markCategoryAsAvailable("SKIRT_ID", "SKIRT");
        markCategoryAsAvailable("JACKET_ID", "JACKET");
        markCategoryAsAvailable("BELT_ID", "BELT");

        // and: the user created some patterns referencing some categories
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
                "A nice dress for the summer",
                new PatternAttributionDTO(),
                Set.of("DRESS_ID", "SKIRT_ID"),
                List.of("IMAGE_ID"),
                List.of(variant),
                List.of(),
                agent
        );
        String patternId2 = createPattern(
                "Jacket",
                null,
                new PatternAttributionDTO(),
                Set.of("JACKET_ID", "DRESS_ID"),
                List.of("IMAGE_ID"),
                List.of(variant),
                List.of(),
                agent
        );
        String patternId3 = createPattern(
                "Leatherbelt",
                null,
                new PatternAttributionDTO(),
                Set.of("BELT_ID"),
                List.of("IMAGE_ID"),
                List.of(variant),
                List.of(),
                agent
        );

        // when: a category is removed from all patterns
        removeCategoryFromPatterns("DRESS_ID");

        // then: the category is removed from all patterns
        var pattern1 = getPattern(patternId1, agent);
        assertThat(pattern1.getCategories()).containsExactlyInAnyOrder(PatternCategoryId.of("SKIRT_ID"));

        var pattern2 = getPattern(patternId2, agent);
        assertThat(pattern2.getCategories()).containsExactlyInAnyOrder(PatternCategoryId.of("JACKET_ID"));

        var pattern3 = getPattern(patternId3, agent);
        assertThat(pattern3.getCategories()).containsExactlyInAnyOrder(PatternCategoryId.of("BELT_ID"));
    }

}
