package de.bennyboer.kicherkrabbe.fabrics;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class QueryColorsUsedInFabricsTest extends FabricsModuleTest {

    @Test
    void shouldQueryColorIdUsedInFabrics() {
        // given: a user is allowed to create fabrics
        allowUserToCreateFabrics("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates some fabrics
        createFabric(
                "Ice bear party",
                "ICE_BEAR_IMAGE_ID",
                Set.of("BLUE_ID", "WHITE_ID"),
                Set.of("WINTER_ID", "ANIMALS_ID"),
                Set.of(jerseyAvailability, cottonAvailability),
                agent
        );
        createFabric(
                "Penguin party",
                "PENGUIN_IMAGE_ID",
                Set.of("BLACK_ID", "WHITE_ID"),
                Set.of("WINTER_ID", "ANIMALS_ID", "BIRDS_ID"),
                Set.of(jerseyAvailability, cottonAvailability),
                agent
        );

        // when: querying the colors used in fabrics with the user agent
        var colors = getColorsUsedInFabrics(agent);

        // then: the colors are returned
        assertThat(colors).containsExactlyInAnyOrder(
                ColorId.of("BLUE_ID"),
                ColorId.of("WHITE_ID"),
                ColorId.of("BLACK_ID")
        );

        // when: querying the colors used in fabrics with an anonymous agent
        colors = getColorsUsedInFabrics(Agent.anonymous());

        // then: the colors are returned
        assertThat(colors).containsExactlyInAnyOrder(
                ColorId.of("BLUE_ID"),
                ColorId.of("WHITE_ID"),
                ColorId.of("BLACK_ID")
        );

        // when: querying the colors used in fabrics with a system agent
        colors = getColorsUsedInFabrics(Agent.system());

        // then: the colors are returned
        assertThat(colors).containsExactlyInAnyOrder(
                ColorId.of("BLUE_ID"),
                ColorId.of("WHITE_ID"),
                ColorId.of("BLACK_ID")
        );
    }

}
