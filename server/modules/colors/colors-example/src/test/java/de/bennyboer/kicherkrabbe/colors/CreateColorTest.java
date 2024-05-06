package de.bennyboer.kicherkrabbe.colors;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CreateColorTest extends ColorsModuleTest {

    @Test
    void shouldCreateColorAsUser() {
        // given: a user is allowed to create colors
        allowUserToCreateAndReadColors("USER_ID");
        Agent agent = Agent.user(AgentId.of("USER_ID"));

        // when: the user creates a color
        String colorId = createColor("Red", 255, 0, 0, agent);

        // then: the color is created
        var colors = getColors(agent);
        assertThat(colors).hasSize(1);
        var color = colors.getFirst();
        assertThat(color.getId()).isEqualTo(ColorId.of(colorId));
        assertThat(color.getName()).isEqualTo(ColorName.of("Red"));
        assertThat(color.getRed()).isEqualTo(255);
        assertThat(color.getGreen()).isEqualTo(0);
        assertThat(color.getBlue()).isEqualTo(0);
    }

    @Test
    void shouldNotBeAbleToCreateColorGivenAnInvalidColor() {
        // given: a user is allowed to create colors
        allowUserToCreateAndReadColors("USER_ID");
        Agent agent = Agent.user(AgentId.of("USER_ID"));

        // when: the user creates a color with an invalid color; then: an error is raised
        assertThatThrownBy(() -> createColor("Red", 256, 0, 0, agent))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Red must be between 0 and 255");

        // when: the user creates a color with an invalid color; then: an error is raised
        assertThatThrownBy(() -> createColor("Red", 0, -5, 0, agent))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Green must be between 0 and 255");
    }

    @Test
    void shouldNotCreateColorWhenUserIsNotAllowed() {
        // when: a user that is not allowed to create a color tries to create a color; then: an error is raised
        assertThatThrownBy(() -> createColor("Red", 255, 0, 0, Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldCreateMultipleColors() {
        // given: a user is allowed to create colors
        allowUserToCreateAndReadColors("USER_ID");
        Agent agent = Agent.user(AgentId.of("USER_ID"));

        // when: the user creates multiple colors
        createColor("Red", 255, 0, 0, agent);
        createColor("Green", 0, 255, 0, agent);
        createColor("Blue", 0, 0, 255, agent);

        // then: the colors are created
        var colors = getColors(agent);
        assertThat(colors).hasSize(3);
    }

}
