package de.bennyboer.kicherkrabbe.highlights;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DeleteHighlightTest extends HighlightsModuleTest {

    @Test
    void shouldDeleteHighlight() {
        allowUserToCreateHighlightsAndReadLinks("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        String highlightId = createHighlight("IMAGE_ID", 0L, agent);

        long version = deleteHighlight(highlightId, 0L, agent);

        assertThat(version).isEqualTo(1L);
        var highlights = getHighlights(agent);
        assertThat(highlights).isEmpty();
    }

    @Test
    void shouldNotDeleteHighlightWhenUserIsNotAllowed() {
        allowUserToCreateHighlightsAndReadLinks("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        String highlightId = createHighlight("IMAGE_ID", 0L, agent);

        var otherAgent = Agent.user(AgentId.of("OTHER_USER_ID"));
        assertThatThrownBy(() -> deleteHighlight(highlightId, 0L, otherAgent))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

}
