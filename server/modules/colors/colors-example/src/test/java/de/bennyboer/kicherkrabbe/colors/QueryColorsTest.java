package de.bennyboer.kicherkrabbe.colors;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class QueryColorsTest extends ColorsModuleTest {

    @Test
    void shouldGetAllAccessibleColors() {
        // given: some colors for different users
        allowUserToCreateAndReadColors("USER_ID_1");
        var agent1 = Agent.user(AgentId.of("USER_ID_1"));
        allowUserToCreateAndReadColors("USER_ID_2");
        var agent2 = Agent.user(AgentId.of("USER_ID_2"));

        var colorId1 = createColor("Red", 255, 0, 0, agent1);
        var colorId2 = createColor("Green", 0, 255, 0, agent2);
        var colorId3 = createColor("Blue", 0, 0, 255, agent1);

        // when: getting all colors for the first user
        var colors1 = getColors(agent1);

        // then: the colors for the first user are returned
        assertThat(colors1).hasSize(2);
        var colorIds1 = colors1.stream()
                .map(ColorDetails::getId)
                .map(ColorId::getValue)
                .toList();
        assertThat(colorIds1).containsExactlyInAnyOrder(colorId1, colorId3);

        // when: getting all colors for the second user
        var colors2 = getColors(agent2);

        // then: the colors for the second user are returned
        assertThat(colors2).hasSize(1);
        var colorIds2 = colors2.stream()
                .map(ColorDetails::getId)
                .map(ColorId::getValue)
                .toList();
        assertThat(colorIds2).containsExactlyInAnyOrder(colorId2);
    }

}
