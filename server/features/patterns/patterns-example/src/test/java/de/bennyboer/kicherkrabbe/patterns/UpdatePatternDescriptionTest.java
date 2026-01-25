package de.bennyboer.kicherkrabbe.patterns;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UpdatePatternDescriptionTest extends PatternsModuleTest {

    @Test
    void shouldUpdatePatternDescriptionAsUser() {
        // given: a user is allowed to create patterns
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates a pattern without description
        String patternId = createSamplePattern(agent);

        // when: the user updates the description of the pattern
        updatePatternDescription(patternId, 0L, "A beautiful dress for high temperatures.", agent);

        // then: the pattern has the new description
        var patterns = getPatterns(agent);
        assertThat(patterns).hasSize(1);
        var pattern = patterns.getFirst();
        assertThat(pattern.getId()).isEqualTo(PatternId.of(patternId));
        assertThat(pattern.getVersion()).isEqualTo(Version.of(1));
        assertThat(pattern.getDescription()).contains(
                PatternDescription.of("A beautiful dress for high temperatures.")
        );
    }

    @Test
    void shouldNotUpdatePatternDescriptionWhenUserIsNotAllowed() {
        // when: a user that is not allowed to update pattern description tries to update pattern description;
        // then: an error is raised
        assertThatThrownBy(() -> updatePatternDescription(
                "PATTERN_ID",
                0L,
                "A beautiful dress for high temperatures.",
                Agent.user(AgentId.of("USER_ID"))
        )).matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldNotUpdatePatternDescriptionGivenAnOutdatedVersion() {
        // given: a user that is allowed to create patterns
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates a pattern without description
        String patternId = createSamplePattern(agent);

        // and: the pattern is published
        publishPattern(patternId, 0L, agent);

        // when: the user tries to update the pattern description with an outdated version
        assertThatThrownBy(() -> updatePatternDescription(
                patternId,
                0L,
                "A beautiful dress for high temperatures.",
                agent
        )).matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

}
