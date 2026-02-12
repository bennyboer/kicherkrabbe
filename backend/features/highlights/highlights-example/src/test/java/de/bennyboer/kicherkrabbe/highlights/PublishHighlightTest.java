package de.bennyboer.kicherkrabbe.highlights;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.highlights.publish.AlreadyPublishedError;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PublishHighlightTest extends HighlightsModuleTest {

    @Test
    void shouldPublishHighlight() {
        allowUserToCreateHighlights("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        String highlightId = createHighlight("IMAGE_ID", 0L, agent);

        long version = publishHighlight(highlightId, 0L, agent);

        assertThat(version).isEqualTo(1L);
        var highlight = getHighlight(highlightId, agent);
        assertThat(highlight.isPublished()).isTrue();
    }

    @Test
    void shouldNotPublishAlreadyPublishedHighlight() {
        allowUserToCreateHighlights("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        String highlightId = createHighlight("IMAGE_ID", 0L, agent);
        long version = publishHighlight(highlightId, 0L, agent);

        long finalVersion = version;
        assertThatThrownBy(() -> publishHighlight(highlightId, finalVersion, agent))
                .isInstanceOf(AlreadyPublishedError.class);
    }

    @Test
    void shouldNotPublishHighlightWhenUserIsNotAllowed() {
        allowUserToCreateHighlights("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        String highlightId = createHighlight("IMAGE_ID", 0L, agent);

        var otherAgent = Agent.user(AgentId.of("OTHER_USER_ID"));
        assertThatThrownBy(() -> publishHighlight(highlightId, 0L, otherAgent))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

}
