package de.bennyboer.kicherkrabbe.highlights;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UpdateSortOrderTest extends HighlightsModuleTest {

    @Test
    void shouldUpdateSortOrder() {
        allowUserToCreateHighlights("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        String highlightId = createHighlight("IMAGE_ID", 0L, agent);

        long version = updateSortOrder(highlightId, 0L, 500L, agent);

        assertThat(version).isEqualTo(1L);
        var highlight = getHighlight(highlightId, agent);
        assertThat(highlight.getSortOrder()).isEqualTo(500L);
    }

    @Test
    void shouldNotUpdateSortOrderWhenUserIsNotAllowed() {
        allowUserToCreateHighlights("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        String highlightId = createHighlight("IMAGE_ID", 0L, agent);

        var otherAgent = Agent.user(AgentId.of("OTHER_USER_ID"));
        assertThatThrownBy(() -> updateSortOrder(highlightId, 0L, 500L, otherAgent))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

}
