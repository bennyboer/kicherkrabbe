package de.bennyboer.kicherkrabbe.patterns;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.patterns.unpublish.AlreadyUnpublishedError;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UnpublishPatternTest extends PatternsModuleTest {

    @Test
    void shouldUnpublishPatternAsUser() {
        // given: a user is allowed to create patterns
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates a pattern
        String patternId = createSamplePattern(agent);

        // and: the user publishes the pattern
        publishPattern(patternId, 0L, agent);

        // when: the user unpublishes the pattern
        unpublishPattern(patternId, 1L, agent);

        // then: the pattern is not published anymore
        var patterns = getPatterns(agent);
        assertThat(patterns).hasSize(1);
        var pattern = patterns.getFirst();
        assertThat(pattern.getId()).isEqualTo(PatternId.of(patternId));
        assertThat(pattern.getVersion()).isEqualTo(Version.of(2L));
        assertThat(pattern.isPublished()).isFalse();
    }

    @Test
    void shouldNotBeAbleToUnpublishPatternIfItIsAlreadyUnpublished() {
        // given: a user is allowed to create patterns
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates a pattern
        String patternId = createSamplePattern(agent);

        // when: the user tries to unpublish the pattern; then: an error is raised
        assertThatThrownBy(() -> unpublishPattern(
                patternId,
                0L,
                agent
        )).isInstanceOf(AlreadyUnpublishedError.class);
    }

    @Test
    void shouldNotUnpublishPatternWhenUserIsNotAllowed() {
        // when: a user that is not allowed to unpublish a pattern tries to unpublish a pattern; then: an error is
        // raised
        assertThatThrownBy(() -> unpublishPattern(
                "PATTERN_ID",
                0L,
                Agent.user(AgentId.of("USER_ID"))
        )).matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldNotUnpublishPatternGivenAnOutdatedVersion() {
        // given: a user that is allowed to create patterns
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates a pattern
        String patternId = createSamplePattern(agent);

        // and: the pattern is published
        publishPattern(patternId, 0L, agent);

        // and: the pattern is renamed
        renamePattern(patternId, 1L, "Test", agent);

        // when: the user tries to unpublish the pattern with an outdated version
        assertThatThrownBy(() -> unpublishPattern(
                patternId,
                1L,
                agent
        )).matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

}
