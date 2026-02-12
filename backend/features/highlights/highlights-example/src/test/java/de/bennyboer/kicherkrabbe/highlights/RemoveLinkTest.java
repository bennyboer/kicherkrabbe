package de.bennyboer.kicherkrabbe.highlights;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RemoveLinkTest extends HighlightsModuleTest {

    @Test
    void shouldRemoveLink() {
        allowUserToCreateHighlightsAndReadLinks("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        String highlightId = createHighlight("IMAGE_ID", 0L, agent);
        long version = addLink(highlightId, 0L, LinkType.PATTERN, "PATTERN_ID", "Summer Dress", agent);

        version = removeLink(highlightId, version, LinkType.PATTERN, "PATTERN_ID", agent);

        assertThat(version).isEqualTo(2L);
        var highlight = getHighlight(highlightId, agent);
        assertThat(highlight.getLinks().getLinks()).isEmpty();
    }

    @Test
    void shouldNotRemoveLinkWhenUserIsNotAllowed() {
        allowUserToCreateHighlightsAndReadLinks("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        String highlightId = createHighlight("IMAGE_ID", 0L, agent);
        long version = addLink(highlightId, 0L, LinkType.PATTERN, "PATTERN_ID", "Summer Dress", agent);

        var otherAgent = Agent.user(AgentId.of("OTHER_USER_ID"));
        long finalVersion = version;
        assertThatThrownBy(() -> removeLink(highlightId, finalVersion, LinkType.PATTERN, "PATTERN_ID", otherAgent))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

}
