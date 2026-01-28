package de.bennyboer.kicherkrabbe.patterns;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.patterns.samples.SamplePatternAttribution;
import de.bennyboer.kicherkrabbe.patterns.samples.SamplePatternVariant;
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
        var variant = SamplePatternVariant.builder().build().toDTO();

        String patternId1 = createPattern(
                "Summerdress",
                "S-D-SUM-1",
                "A nice dress for the summer",
                SamplePatternAttribution.builder().build().toDTO(),
                Set.of("DRESS_ID", "SKIRT_ID"),
                List.of("IMAGE_ID"),
                List.of(variant),
                List.of(),
                agent
        );
        String patternId2 = createPattern(
                "Jacket",
                "S-J-DEF-1",
                null,
                SamplePatternAttribution.builder().build().toDTO(),
                Set.of("JACKET_ID", "DRESS_ID"),
                List.of("IMAGE_ID"),
                List.of(variant),
                List.of(),
                agent
        );
        String patternId3 = createPattern(
                "Leatherbelt",
                "S-B-LEA-1",
                null,
                SamplePatternAttribution.builder().build().toDTO(),
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
