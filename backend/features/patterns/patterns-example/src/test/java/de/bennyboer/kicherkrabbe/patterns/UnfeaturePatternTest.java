package de.bennyboer.kicherkrabbe.patterns;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.patterns.samples.SamplePattern;
import de.bennyboer.kicherkrabbe.patterns.unfeature.AlreadyUnfeaturedError;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UnfeaturePatternTest extends PatternsModuleTest {

    @Test
    void shouldUnfeaturePatternAsUser() {
        // given: a user is allowed to create patterns
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates a pattern
        String patternId = createPattern(SamplePattern.builder().build(), agent);

        // and: the user features the pattern
        featurePattern(patternId, 0L, agent);

        // when: the user unfeatures the pattern
        unfeaturePattern(patternId, 1L, agent);

        // then: the pattern is not featured anymore
        var patterns = getPatterns(agent);
        assertThat(patterns).hasSize(1);
        var pattern = patterns.getFirst();
        assertThat(pattern.getId()).isEqualTo(PatternId.of(patternId));
        assertThat(pattern.getVersion()).isEqualTo(Version.of(2L));
        assertThat(pattern.isFeatured()).isFalse();
    }

    @Test
    void shouldNotBeAbleToUnfeaturePatternIfItIsAlreadyUnfeatured() {
        // given: a user is allowed to create patterns
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates a pattern
        String patternId = createPattern(SamplePattern.builder().build(), agent);

        // when: the user tries to unfeature the pattern; then: an error is raised
        assertThatThrownBy(() -> unfeaturePattern(
                patternId,
                0L,
                agent
        )).isInstanceOf(AlreadyUnfeaturedError.class);
    }

    @Test
    void shouldNotUnfeaturePatternWhenUserIsNotAllowed() {
        // when: a user that is not allowed to unfeature a pattern tries to unfeature a pattern; then: an error is raised
        assertThatThrownBy(() -> unfeaturePattern(
                "PATTERN_ID",
                0L,
                Agent.user(AgentId.of("USER_ID"))
        )).matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldNotUnfeaturePatternGivenAnOutdatedVersion() {
        // given: a user that is allowed to create patterns
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates a pattern
        String patternId = createPattern(SamplePattern.builder().build(), agent);

        // and: the pattern is featured
        featurePattern(patternId, 0L, agent);

        // and: the pattern is renamed
        renamePattern(patternId, 1L, "Test", agent);

        // when: the user tries to unfeature the pattern with an outdated version
        assertThatThrownBy(() -> unfeaturePattern(
                patternId,
                1L,
                agent
        )).matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

}
