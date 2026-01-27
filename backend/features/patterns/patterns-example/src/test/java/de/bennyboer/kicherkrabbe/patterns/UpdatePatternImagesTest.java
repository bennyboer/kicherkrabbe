package de.bennyboer.kicherkrabbe.patterns;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UpdatePatternImagesTest extends PatternsModuleTest {

    @Test
    void shouldUpdatePatternImagesAsUser() {
        // given: a user is allowed to create patterns
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates a pattern
        String patternId = createSamplePattern(agent);

        // when: the user updates the images of the pattern
        updatePatternImages(patternId, 0L, List.of("NEW_IMAGE_ID", "IMAGE_ID"), agent);

        // then: the pattern has the new images
        var patterns = getPatterns(agent);
        assertThat(patterns).hasSize(1);
        var pattern = patterns.getFirst();
        assertThat(pattern.getId()).isEqualTo(PatternId.of(patternId));
        assertThat(pattern.getVersion()).isEqualTo(Version.of(1));
        assertThat(pattern.getImages()).containsExactly(ImageId.of("NEW_IMAGE_ID"), ImageId.of("IMAGE_ID"));
    }

    @Test
    void shouldNotUpdatePatternImagesWhenUserIsNotAllowed() {
        // when: a user that is not allowed to update pattern images tries to update pattern images; then: an error
        // is raised
        assertThatThrownBy(() -> updatePatternImages(
                "PATTERN_ID",
                0L,
                List.of("NEW_IMAGE_ID"),
                Agent.user(AgentId.of("USER_ID"))
        )).matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldNotUpdatePatternImagesGivenAnOutdatedVersion() {
        // given: a user that is allowed to create patterns
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates a pattern
        String patternId = createSamplePattern(agent);

        // and: the pattern is published
        publishPattern(patternId, 0L, agent);

        // when: the user tries to update the pattern images with an outdated version
        assertThatThrownBy(() -> updatePatternImages(
                patternId,
                0L,
                List.of("NEW_IMAGE_ID"),
                agent
        )).matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

}
