package de.bennyboer.kicherkrabbe.patterns;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.patterns.samples.SamplePatternAttribution;
import de.bennyboer.kicherkrabbe.patterns.samples.SamplePatternVariant;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class QueryCategoriesUsedInPatternsTest extends PatternsModuleTest {

    @Test
    void shouldQueryCategoriesUsedInPatterns() {
        // given: a user is allowed to create patterns
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: some categories are available
        markCategoryAsAvailable("DRESS_ID", "Dress");
        markCategoryAsAvailable("SKIRT_ID", "Skirt");
        markCategoryAsAvailable("TROUSERS_ID", "Trousers");
        markCategoryAsAvailable("SHOES_ID", "Shoes");

        // and: the user creates some patterns
        var variant = SamplePatternVariant.builder().build().toDTO();

        createPattern(
                "Summerdress",
                "S-D-SUM-1",
                null,
                SamplePatternAttribution.builder().build().toDTO(),
                Set.of("DRESS_ID", "SKIRT_ID"),
                List.of("IMAGE_ID"),
                List.of(variant),
                List.of(),
                agent
        );
        createPattern(
                "Long tight skirt with pockets",
                "S-S-LON-1",
                null,
                SamplePatternAttribution.builder().build().toDTO(),
                Set.of("TROUSERS_ID"),
                List.of("IMAGE_ID"),
                List.of(variant),
                List.of(),
                agent
        );

        // when: querying the categories used in patterns with the user agent
        var categories = getCategoriesUsedInPatterns(agent);

        // then: the categories are returned
        assertThat(categories).containsExactlyInAnyOrder(
                PatternCategory.of(PatternCategoryId.of("DRESS_ID"), PatternCategoryName.of("Dress")),
                PatternCategory.of(PatternCategoryId.of("SKIRT_ID"), PatternCategoryName.of("Skirt")),
                PatternCategory.of(PatternCategoryId.of("TROUSERS_ID"), PatternCategoryName.of("Trousers"))
        );

        // when: querying the categories used in patterns with an anonymous agent
        categories = getCategoriesUsedInPatterns(Agent.anonymous());

        // then: the categories are returned
        assertThat(categories).containsExactlyInAnyOrder(
                PatternCategory.of(PatternCategoryId.of("DRESS_ID"), PatternCategoryName.of("Dress")),
                PatternCategory.of(PatternCategoryId.of("SKIRT_ID"), PatternCategoryName.of("Skirt")),
                PatternCategory.of(PatternCategoryId.of("TROUSERS_ID"), PatternCategoryName.of("Trousers"))
        );

        // when: querying the categories used in patterns with a system agent
        categories = getCategoriesUsedInPatterns(Agent.system());

        // then: the categories are returned
        assertThat(categories).containsExactlyInAnyOrder(
                PatternCategory.of(PatternCategoryId.of("DRESS_ID"), PatternCategoryName.of("Dress")),
                PatternCategory.of(PatternCategoryId.of("SKIRT_ID"), PatternCategoryName.of("Skirt")),
                PatternCategory.of(PatternCategoryId.of("TROUSERS_ID"), PatternCategoryName.of("Trousers"))
        );
    }

}
