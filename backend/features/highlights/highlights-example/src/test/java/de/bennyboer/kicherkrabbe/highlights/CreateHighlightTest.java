package de.bennyboer.kicherkrabbe.highlights;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CreateHighlightTest extends HighlightsModuleTest {

    @Test
    void shouldCreateHighlightAsUser() {
        allowUserToCreateHighlightsAndReadLinks("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        String highlightId = createHighlight("IMAGE_ID", 100L, agent);

        var highlights = getHighlights(agent);
        assertThat(highlights).hasSize(1);
        var highlight = highlights.getFirst();
        assertThat(highlight.getId()).isEqualTo(HighlightId.of(highlightId));
        assertThat(highlight.getImageId()).isEqualTo(ImageId.of("IMAGE_ID"));
        assertThat(highlight.getSortOrder()).isEqualTo(100L);
        assertThat(highlight.isPublished()).isFalse();
    }

    @Test
    void shouldNotCreateHighlightWhenUserIsNotAllowed() {
        assertThatThrownBy(() -> createHighlight("IMAGE_ID", 0L, Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldCreateMultipleHighlights() {
        allowUserToCreateHighlightsAndReadLinks("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        createHighlight("IMAGE_1", 0L, agent);
        createHighlight("IMAGE_2", 100L, agent);
        createHighlight("IMAGE_3", 200L, agent);

        var highlights = getHighlights(agent);
        assertThat(highlights).hasSize(3);
    }

}
