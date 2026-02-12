package de.bennyboer.kicherkrabbe.highlights;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class QueryPublishedHighlightsTest extends HighlightsModuleTest {

    @Test
    void shouldReturnOnlyPublishedHighlights() {
        allowUserToCreateHighlightsAndReadLinks("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        String highlight1 = createHighlight("IMAGE_1", 100L, agent);
        String highlight2 = createHighlight("IMAGE_2", 200L, agent);
        String highlight3 = createHighlight("IMAGE_3", 300L, agent);

        publishHighlight(highlight1, 0L, agent);
        publishHighlight(highlight3, 0L, agent);

        var publishedHighlights = getPublishedHighlights();
        assertThat(publishedHighlights).hasSize(2);
        assertThat(publishedHighlights.get(0).getId()).isEqualTo(HighlightId.of(highlight1));
        assertThat(publishedHighlights.get(1).getId()).isEqualTo(HighlightId.of(highlight3));
    }

    @Test
    void shouldReturnPublishedHighlightsOrderedBySortOrder() {
        allowUserToCreateHighlightsAndReadLinks("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        String highlight1 = createHighlight("IMAGE_1", 300L, agent);
        String highlight2 = createHighlight("IMAGE_2", 100L, agent);
        String highlight3 = createHighlight("IMAGE_3", 200L, agent);

        publishHighlight(highlight1, 0L, agent);
        publishHighlight(highlight2, 0L, agent);
        publishHighlight(highlight3, 0L, agent);

        var publishedHighlights = getPublishedHighlights();
        assertThat(publishedHighlights).hasSize(3);
        assertThat(publishedHighlights.get(0).getId()).isEqualTo(HighlightId.of(highlight2));
        assertThat(publishedHighlights.get(1).getId()).isEqualTo(HighlightId.of(highlight3));
        assertThat(publishedHighlights.get(2).getId()).isEqualTo(HighlightId.of(highlight1));
    }

    @Test
    void shouldReturnEmptyListWhenNoHighlightsArePublished() {
        allowUserToCreateHighlightsAndReadLinks("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        createHighlight("IMAGE_1", 100L, agent);
        createHighlight("IMAGE_2", 200L, agent);

        var publishedHighlights = getPublishedHighlights();
        assertThat(publishedHighlights).isEmpty();
    }

}
