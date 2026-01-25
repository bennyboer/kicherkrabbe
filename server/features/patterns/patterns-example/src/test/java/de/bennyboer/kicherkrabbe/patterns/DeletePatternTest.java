package de.bennyboer.kicherkrabbe.patterns;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.patterns.samples.SamplePattern;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DeletePatternTest extends PatternsModuleTest {

    @Test
    void shouldDeletePatternAsUser() {
        // given: a user that is allowed to create patterns
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates some patterns
        String patternId1 = createPattern(SamplePattern.builder().number("S-X-DEL-1").build(), agent);
        String patternId2 = createPattern(SamplePattern.builder().number("S-X-DEL-2").build(), agent);

        // when: the user deletes the first pattern
        deletePattern(patternId1, 0L, agent);

        // then: the first pattern is deleted
        var patterns = getPatterns(agent);
        assertThat(patterns).hasSize(1);
        var pattern = patterns.getFirst();
        assertThat(pattern.getId()).isEqualTo(PatternId.of(patternId2));
    }

    @Test
    void shouldNotDeletePatternGivenAnOutdatedVersion() {
        // given: a user that is allowed to create patterns
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates a pattern
        String patternId = createSamplePattern(agent);

        // and: the pattern is renamed
        renamePattern(patternId, 0L, "New name", agent);

        // when: the user tries to delete the pattern with an outdated version
        assertThatThrownBy(() -> deletePattern(
                patternId,
                0L,
                agent
        )).matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldNotDeletePatternWhenUserIsNotAllowed() {
        // when: a user that is not allowed to delete a pattern tries to delete a pattern; then: an error is raised
        assertThatThrownBy(() -> deletePattern(
                "PATTERN_ID",
                0L,
                Agent.user(AgentId.of("USER_ID"))
        )).matches(e -> e.getCause() instanceof MissingPermissionError);
    }

}
