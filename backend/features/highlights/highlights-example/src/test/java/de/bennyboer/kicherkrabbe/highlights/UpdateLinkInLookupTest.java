package de.bennyboer.kicherkrabbe.highlights;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.highlights.api.LinkDTO;
import de.bennyboer.kicherkrabbe.highlights.api.LinkTypeDTO;
import de.bennyboer.kicherkrabbe.highlights.api.requests.RemoveLinkFromLookupRequest;
import de.bennyboer.kicherkrabbe.highlights.api.requests.UpdateLinkInLookupRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class UpdateLinkInLookupTest extends HighlightsModuleTest {

    @Test
    void shouldUpdateLinkInHighlightsWhenLinkIsRenamed() {
        allowUserToCreateHighlightsAndReadLinks("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        String highlightId1 = createHighlight("IMAGE_ID_1", 0L, agent);
        String highlightId2 = createHighlight("IMAGE_ID_2", 1L, agent);

        addLink(highlightId1, 0L, LinkType.PATTERN, "PATTERN_ID", "Old Name", agent);
        addLink(highlightId2, 0L, LinkType.PATTERN, "PATTERN_ID", "Old Name", agent);

        var highlight1 = getHighlight(highlightId1, agent);
        var highlight2 = getHighlight(highlightId2, agent);
        assertThat(highlight1.getLinks().getLinks()).hasSize(1);
        assertThat(highlight2.getLinks().getLinks()).hasSize(1);
        assertThat(highlight1.getLinks().getLinks().iterator().next().getName().getValue()).isEqualTo("Old Name");
        assertThat(highlight2.getLinks().getLinks().iterator().next().getName().getValue()).isEqualTo("Old Name");

        var request = new UpdateLinkInLookupRequest();
        request.link = new LinkDTO();
        request.link.type = LinkTypeDTO.PATTERN;
        request.link.id = "PATTERN_ID";
        request.link.name = "New Name";
        request.version = 1L;
        List<String> updatedHighlightIds = updateLinkInLookup(request);

        assertThat(updatedHighlightIds).containsExactlyInAnyOrder(highlightId1, highlightId2);

        highlight1 = getHighlight(highlightId1, agent);
        highlight2 = getHighlight(highlightId2, agent);
        assertThat(highlight1.getLinks().getLinks().iterator().next().getName().getValue()).isEqualTo("New Name");
        assertThat(highlight2.getLinks().getLinks().iterator().next().getName().getValue()).isEqualTo("New Name");
    }

    @Test
    void shouldNotUpdateHighlightsWithoutMatchingLink() {
        allowUserToCreateHighlightsAndReadLinks("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        String highlightId1 = createHighlight("IMAGE_ID_1", 0L, agent);
        String highlightId2 = createHighlight("IMAGE_ID_2", 1L, agent);

        addLink(highlightId1, 0L, LinkType.PATTERN, "PATTERN_ID_1", "Pattern 1", agent);
        addLink(highlightId2, 0L, LinkType.PATTERN, "PATTERN_ID_2", "Pattern 2", agent);

        var request = new UpdateLinkInLookupRequest();
        request.link = new LinkDTO();
        request.link.type = LinkTypeDTO.PATTERN;
        request.link.id = "PATTERN_ID_1";
        request.link.name = "Updated Pattern 1";
        request.version = 1L;
        List<String> updatedHighlightIds = updateLinkInLookup(request);

        assertThat(updatedHighlightIds).containsExactly(highlightId1);

        var highlight1 = getHighlight(highlightId1, agent);
        var highlight2 = getHighlight(highlightId2, agent);
        assertThat(highlight1.getLinks().getLinks().iterator().next().getName().getValue()).isEqualTo("Updated Pattern 1");
        assertThat(highlight2.getLinks().getLinks().iterator().next().getName().getValue()).isEqualTo("Pattern 2");
    }

    @Test
    void shouldUpdateOnlyMatchingLinkType() {
        allowUserToCreateHighlightsAndReadLinks("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        String highlightId = createHighlight("IMAGE_ID", 0L, agent);

        addLink(highlightId, 0L, LinkType.PATTERN, "ID_1", "Pattern", agent);
        addLink(highlightId, 1L, LinkType.FABRIC, "ID_1", "Fabric", agent);

        var request = new UpdateLinkInLookupRequest();
        request.link = new LinkDTO();
        request.link.type = LinkTypeDTO.PATTERN;
        request.link.id = "ID_1";
        request.link.name = "Updated Pattern";
        request.version = 1L;
        updateLinkInLookup(request);

        var highlight = getHighlight(highlightId, agent);
        var links = highlight.getLinks().getLinks();
        assertThat(links).hasSize(2);

        var patternLink = links.stream().filter(l -> l.getType() == LinkType.PATTERN).findFirst().orElseThrow();
        var fabricLink = links.stream().filter(l -> l.getType() == LinkType.FABRIC).findFirst().orElseThrow();

        assertThat(patternLink.getName().getValue()).isEqualTo("Updated Pattern");
        assertThat(fabricLink.getName().getValue()).isEqualTo("Fabric");
    }

    @Test
    void shouldCleanupLinkInHighlightsOnRemoval() {
        allowUserToCreateHighlightsAndReadLinks("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        String highlightId1 = createHighlight("IMAGE_ID_1", 0L, agent);
        String highlightId2 = createHighlight("IMAGE_ID_2", 1L, agent);

        addLink(highlightId1, 0L, LinkType.PATTERN, "PATTERN_ID", "Pattern", agent);
        addLink(highlightId2, 0L, LinkType.PATTERN, "PATTERN_ID", "Pattern", agent);

        var highlight1 = getHighlight(highlightId1, agent);
        var highlight2 = getHighlight(highlightId2, agent);
        assertThat(highlight1.getLinks().getLinks()).hasSize(1);
        assertThat(highlight2.getLinks().getLinks()).hasSize(1);

        var request = new RemoveLinkFromLookupRequest();
        request.linkType = LinkTypeDTO.PATTERN;
        request.linkId = "PATTERN_ID";
        List<String> updatedHighlightIds = removeLinkFromLookup(request);

        assertThat(updatedHighlightIds).containsExactlyInAnyOrder(highlightId1, highlightId2);

        highlight1 = getHighlight(highlightId1, agent);
        highlight2 = getHighlight(highlightId2, agent);
        assertThat(highlight1.getLinks().getLinks()).isEmpty();
        assertThat(highlight2.getLinks().getLinks()).isEmpty();
    }

    @Test
    void shouldOnlyRemoveMatchingLinkFromHighlights() {
        allowUserToCreateHighlightsAndReadLinks("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        String highlightId = createHighlight("IMAGE_ID", 0L, agent);

        addLink(highlightId, 0L, LinkType.PATTERN, "PATTERN_ID", "Pattern", agent);
        addLink(highlightId, 1L, LinkType.FABRIC, "FABRIC_ID", "Fabric", agent);

        var highlight = getHighlight(highlightId, agent);
        assertThat(highlight.getLinks().getLinks()).hasSize(2);

        var request = new RemoveLinkFromLookupRequest();
        request.linkType = LinkTypeDTO.PATTERN;
        request.linkId = "PATTERN_ID";
        removeLinkFromLookup(request);

        highlight = getHighlight(highlightId, agent);
        assertThat(highlight.getLinks().getLinks()).hasSize(1);
        var remainingLink = highlight.getLinks().getLinks().iterator().next();
        assertThat(remainingLink.getType()).isEqualTo(LinkType.FABRIC);
        assertThat(remainingLink.getId().getValue()).isEqualTo("FABRIC_ID");
    }

}
