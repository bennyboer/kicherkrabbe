package de.bennyboer.kicherkrabbe.patterns;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.patterns.samples.SamplePatternAttribution;
import de.bennyboer.kicherkrabbe.patterns.samples.SamplePatternVariant;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class QueryFeaturedPatternsTest extends PatternsModuleTest {

    @Test
    void shouldQueryFeaturedPatterns() {
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        markCategoryAsAvailable("DRESS_ID", "Dress");

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
                Set.of("DRESS_ID"),
                List.of("IMAGE_ID"),
                List.of(variant),
                List.of(),
                agent
        );

        var result = getFeaturedPatterns(agent);
        assertThat(result).isEmpty();

        publishPattern(patternId1, 0L, agent);
        publishPattern(patternId2, 0L, agent);

        result = getFeaturedPatterns(agent);
        assertThat(result).isEmpty();

        featurePattern(patternId1, 1L, agent);

        result = getFeaturedPatterns(agent);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(PatternId.of(patternId1));
        assertThat(result.get(0).getName()).isEqualTo(PatternName.of("Summerdress"));

        featurePattern(patternId2, 1L, agent);

        result = getFeaturedPatterns(agent);
        assertThat(result).hasSize(2);

        unfeaturePattern(patternId1, 2L, agent);

        result = getFeaturedPatterns(agent);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(PatternId.of(patternId2));
    }

    @Test
    void shouldAllowAnonymousUserToQueryFeaturedPatterns() {
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        var variant = SamplePatternVariant.builder().build().toDTO();

        String patternId = createPattern(
                "Summerdress",
                "S-D-SUM-1",
                null,
                SamplePatternAttribution.builder().build().toDTO(),
                Set.of(),
                List.of("IMAGE_ID"),
                List.of(variant),
                List.of(),
                agent
        );

        publishPattern(patternId, 0L, agent);
        featurePattern(patternId, 1L, agent);

        var result = getFeaturedPatterns(Agent.anonymous());
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(PatternId.of(patternId));
    }

    @Test
    void shouldAllowSystemUserToQueryFeaturedPatterns() {
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        var variant = SamplePatternVariant.builder().build().toDTO();

        String patternId = createPattern(
                "Summerdress",
                "S-D-SUM-1",
                null,
                SamplePatternAttribution.builder().build().toDTO(),
                Set.of(),
                List.of("IMAGE_ID"),
                List.of(variant),
                List.of(),
                agent
        );

        publishPattern(patternId, 0L, agent);
        featurePattern(patternId, 1L, agent);

        var result = getFeaturedPatterns(Agent.system());
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(PatternId.of(patternId));
    }

    @Test
    void shouldNotReturnUnpublishedFeaturedPatterns() {
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        var variant = SamplePatternVariant.builder().build().toDTO();

        String patternId = createPattern(
                "Summerdress",
                "S-D-SUM-1",
                null,
                SamplePatternAttribution.builder().build().toDTO(),
                Set.of(),
                List.of("IMAGE_ID"),
                List.of(variant),
                List.of(),
                agent
        );

        publishPattern(patternId, 0L, agent);
        featurePattern(patternId, 1L, agent);

        var result = getFeaturedPatterns(agent);
        assertThat(result).hasSize(1);

        unpublishPattern(patternId, 2L, agent);

        result = getFeaturedPatterns(agent);
        assertThat(result).isEmpty();
    }

}
