package de.bennyboer.kicherkrabbe.colors;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DeleteColorTest extends ColorsModuleTest {

    @Test
    void shouldDeleteColor() {
        // given: a color
        allowUserToCreateAndReadColors("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));
        var colorId1 = createColor("Red", 255, 0, 0, agent);
        var colorId2 = createColor("Green", 0, 255, 0, agent);

        // when: the user deletes the first color
        deleteColor(colorId1, 0L, agent);

        // then: the first color is deleted
        var colors = getColors(agent);
        assertThat(colors).hasSize(1);
        var color = colors.getFirst();
        assertThat(color.getId()).isEqualTo(ColorId.of(colorId2));
    }

    @Test
    void shouldNotDeleteColorIfNotHavingPermission() {
        // given: a color
        allowUserToCreateAndReadColors("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));
        var colorId = createColor("Red", 255, 0, 0, agent);

        // when: another user tries to delete the color; then: an error is raised
        assertThatThrownBy(() -> deleteColor(colorId, 0L, Agent.user(AgentId.of("OTHER_USER_ID"))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

}
