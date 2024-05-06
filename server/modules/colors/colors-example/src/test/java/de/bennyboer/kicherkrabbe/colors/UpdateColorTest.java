package de.bennyboer.kicherkrabbe.colors;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UpdateColorTest extends ColorsModuleTest {

    @Test
    void shouldUpdateColor() {
        // given: a color
        allowUserToCreateAndReadColors("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));
        var colorId = createColor("Red", 255, 0, 0, agent);

        // when: the user updates the color
        updateColor(colorId, 0L, "Blue", 0, 0, 255, agent);

        // then: the color is updated
        var colors = getColors(agent);
        assertThat(colors).hasSize(1);
        var color = colors.getFirst();
        assertThat(color.getId()).isEqualTo(ColorId.of(colorId));
        assertThat(color.getName()).isEqualTo(ColorName.of("Blue"));
        assertThat(color.getRed()).isEqualTo(0);
        assertThat(color.getGreen()).isEqualTo(0);
        assertThat(color.getBlue()).isEqualTo(255);
    }

    @Test
    void shouldNotUpdateColorIfNotHavingPermission() {
        // given: a color
        allowUserToCreateAndReadColors("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));
        var colorId = createColor("Red", 255, 0, 0, agent);

        // when: another user tries to update the color; then: an error is raised
        assertThatThrownBy(() -> updateColor(
                colorId,
                0L,
                "Blue",
                0,
                0,
                255,
                Agent.user(AgentId.of("OTHER_USER_ID"))
        )).matches(e -> e.getCause() instanceof MissingPermissionError);
    }

}
