package de.bennyboer.kicherkrabbe.patterns;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.patterns.feature.AlreadyFeaturedError;
import de.bennyboer.kicherkrabbe.patterns.samples.SamplePattern;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class FeaturePatternTest extends PatternsModuleTest {

    @Test
    void shouldFeaturePatternAsUser() {
        // given: a user is allowed to create patterns
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates a pattern
        String patternId = createPattern(SamplePattern.builder().build(), agent);

        // when: the user features the pattern
        featurePattern(patternId, 0L, agent);

        // then: the pattern is featured
        var patterns = getPatterns(agent);
        assertThat(patterns).hasSize(1);
        var pattern = patterns.getFirst();
        assertThat(pattern.getId()).isEqualTo(PatternId.of(patternId));
        assertThat(pattern.getVersion()).isEqualTo(Version.of(1L));
        assertThat(pattern.isFeatured()).isTrue();
    }

    @Test
    void shouldNotBeAbleToFeaturePatternIfItIsAlreadyFeatured() {
        // given: a user is allowed to create patterns
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates a pattern
        String patternId = createPattern(SamplePattern.builder().build(), agent);

        // and: the pattern is featured
        featurePattern(patternId, 0L, agent);

        // when: the user tries to feature the pattern again; then: an error is raised
        assertThatThrownBy(() -> featurePattern(
                patternId,
                1L,
                agent
        )).isInstanceOf(AlreadyFeaturedError.class);
    }

    @Test
    void shouldNotFeaturePatternWhenUserIsNotAllowed() {
        // when: a user that is not allowed to feature a pattern tries to feature a pattern; then: an error is raised
        assertThatThrownBy(() -> featurePattern(
                "PATTERN_ID",
                0L,
                Agent.user(AgentId.of("USER_ID"))
        )).matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldNotFeaturePatternGivenAnOutdatedVersion() {
        // given: a user that is allowed to create patterns
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates a pattern
        String patternId = createPattern(SamplePattern.builder().build(), agent);

        // and: the pattern is renamed
        renamePattern(patternId, 0L, "Test", agent);

        // when: the user tries to feature the pattern with an outdated version
        assertThatThrownBy(() -> featurePattern(
                patternId,
                0L,
                agent
        )).matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

}
