package de.bennyboer.kicherkrabbe.highlights;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AddLinkTest extends HighlightsModuleTest {

    @Test
    void shouldAddLink() {
        allowUserToCreateHighlightsAndReadLinks("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        String highlightId = createHighlight("IMAGE_ID", 0L, agent);

        long version = addLink(highlightId, 0L, LinkType.PATTERN, "PATTERN_ID", "Summer Dress", agent);

        assertThat(version).isEqualTo(1L);
        var highlight = getHighlight(highlightId, agent);
        assertThat(highlight.getLinks().getLinks()).hasSize(1);
        var link = highlight.getLinks().getLinks().iterator().next();
        assertThat(link.getType()).isEqualTo(LinkType.PATTERN);
        assertThat(link.getId()).isEqualTo(LinkId.of("PATTERN_ID"));
        assertThat(link.getName()).isEqualTo(LinkName.of("Summer Dress"));
    }

    @Test
    void shouldAddMultipleLinks() {
        allowUserToCreateHighlightsAndReadLinks("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        String highlightId = createHighlight("IMAGE_ID", 0L, agent);

        long version = addLink(highlightId, 0L, LinkType.PATTERN, "PATTERN_ID", "Summer Dress", agent);
        version = addLink(highlightId, version, LinkType.FABRIC, "FABRIC_ID", "Cotton Blue", agent);

        assertThat(version).isEqualTo(2L);
        var highlight = getHighlight(highlightId, agent);
        assertThat(highlight.getLinks().getLinks()).hasSize(2);
    }

    @Test
    void shouldNotAddLinkWhenUserIsNotAllowed() {
        allowUserToCreateHighlightsAndReadLinks("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        String highlightId = createHighlight("IMAGE_ID", 0L, agent);

        var otherAgent = Agent.user(AgentId.of("OTHER_USER_ID"));
        assertThatThrownBy(() -> addLink(highlightId, 0L, LinkType.PATTERN, "PATTERN_ID", "Summer Dress", otherAgent))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

}
