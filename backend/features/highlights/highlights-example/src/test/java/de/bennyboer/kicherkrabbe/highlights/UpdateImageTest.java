package de.bennyboer.kicherkrabbe.highlights;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UpdateImageTest extends HighlightsModuleTest {

    @Test
    void shouldUpdateImage() {
        allowUserToCreateHighlights("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        String highlightId = createHighlight("IMAGE_ID", 0L, agent);

        long version = updateImage(highlightId, 0L, "NEW_IMAGE_ID", agent);

        assertThat(version).isEqualTo(1L);
        var highlight = getHighlight(highlightId, agent);
        assertThat(highlight.getImageId()).isEqualTo(ImageId.of("NEW_IMAGE_ID"));
    }

    @Test
    void shouldNotUpdateImageWhenUserIsNotAllowed() {
        allowUserToCreateHighlights("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        String highlightId = createHighlight("IMAGE_ID", 0L, agent);

        var otherAgent = Agent.user(AgentId.of("OTHER_USER_ID"));
        assertThatThrownBy(() -> updateImage(highlightId, 0L, "NEW_IMAGE_ID", otherAgent))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

}
