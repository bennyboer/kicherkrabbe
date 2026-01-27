package de.bennyboer.kicherkrabbe.patterns;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RenamePatternTest extends PatternsModuleTest {

    @Test
    void shouldRenamePatternAsUser() {
        // given: a user is allowed to create patterns
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates a pattern
        String patternId = createSamplePattern(agent);

        // when: the user renames the pattern
        renamePattern(patternId, 0L, "New name", agent);

        // then: the pattern is renamed
        var patterns = getPatterns(agent);
        assertThat(patterns).hasSize(1);
        var pattern = patterns.getFirst();
        assertThat(pattern.getId()).isEqualTo(PatternId.of(patternId));
        assertThat(pattern.getVersion()).isEqualTo(Version.of(1));
        assertThat(pattern.getName()).isEqualTo(PatternName.of("New name"));
    }

    @Test
    void shouldNotBeAbleToRenamePatternGivenAnInvalidName() {
        String invalidName1 = "";
        String invalidName2 = null;

        // given: a user is allowed to create patterns
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates a pattern
        String patternId = createSamplePattern(agent);

        // when: the user tries to rename the pattern with an invalid name; then: an error is raised
        assertThatThrownBy(() -> renamePattern(
                patternId,
                0L,
                invalidName1,
                agent
        )).isInstanceOf(IllegalArgumentException.class);

        // when: the user tries to rename the pattern with an invalid name; then: an error is raised
        assertThatThrownBy(() -> renamePattern(
                patternId,
                0L,
                invalidName2,
                agent
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldNotRenamePatternWhenUserIsNotAllowed() {
        // when: a user that is not allowed to rename a pattern tries to rename a pattern; then: an error is raised
        assertThatThrownBy(() -> renamePattern(
                "PATTERN_ID",
                0L,
                "Test",
                Agent.user(AgentId.of("USER_ID"))
        )).matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldNotRenamePatternGivenAnOutdatedVersion() {
        // given: a user that is allowed to create patterns
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates a pattern
        String patternId = createSamplePattern(agent);

        // and: the pattern is renamed
        renamePattern(patternId, 0L, "New name", agent);

        // when: the user tries to rename the pattern with an outdated version
        assertThatThrownBy(() -> renamePattern(
                patternId,
                0L,
                "New name",
                agent
        )).matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

}
