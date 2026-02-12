package de.bennyboer.kicherkrabbe.highlights;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.highlights.unpublish.AlreadyUnpublishedError;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UnpublishHighlightTest extends HighlightsModuleTest {

    @Test
    void shouldUnpublishHighlight() {
        allowUserToCreateHighlightsAndReadLinks("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        String highlightId = createHighlight("IMAGE_ID", 0L, agent);
        long version = publishHighlight(highlightId, 0L, agent);

        version = unpublishHighlight(highlightId, version, agent);

        assertThat(version).isEqualTo(2L);
        var highlight = getHighlight(highlightId, agent);
        assertThat(highlight.isPublished()).isFalse();
    }

    @Test
    void shouldNotUnpublishAlreadyUnpublishedHighlight() {
        allowUserToCreateHighlightsAndReadLinks("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        String highlightId = createHighlight("IMAGE_ID", 0L, agent);

        assertThatThrownBy(() -> unpublishHighlight(highlightId, 0L, agent))
                .isInstanceOf(AlreadyUnpublishedError.class);
    }

    @Test
    void shouldNotUnpublishHighlightWhenUserIsNotAllowed() {
        allowUserToCreateHighlightsAndReadLinks("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        String highlightId = createHighlight("IMAGE_ID", 0L, agent);
        long version = publishHighlight(highlightId, 0L, agent);

        var otherAgent = Agent.user(AgentId.of("OTHER_USER_ID"));
        long finalVersion = version;
        assertThatThrownBy(() -> unpublishHighlight(highlightId, finalVersion, otherAgent))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

}
