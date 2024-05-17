package de.bennyboer.kicherkrabbe.fabrics;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.fabrics.persistence.colors.Color;
import de.bennyboer.kicherkrabbe.fabrics.persistence.colors.ColorName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class QueryColorsUsedInFabricsTest extends FabricsModuleTest {

    @Test
    void shouldQueryColorIdUsedInFabrics() {
        // given: a user is allowed to create fabrics
        allowUserToCreateFabrics("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: some topics are available
        markTopicAsAvailable("WINTER_ID", "Winter");
        markTopicAsAvailable("ANIMALS_ID", "Animals");
        markTopicAsAvailable("BIRDS_ID", "Birds");

        // and: some colors are available
        markColorAsAvailable("BLUE_ID", "Blue", 0, 0, 255);
        markColorAsAvailable("WHITE_ID", "White", 255, 255, 255);
        markColorAsAvailable("BLACK_ID", "Black", 0, 0, 0);

        // and: some fabric types are available
        markFabricTypeAsAvailable("JERSEY_ID", "Jersey");
        markFabricTypeAsAvailable("COTTON_ID", "Cotton");

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
                Color.of(ColorId.of("BLUE_ID"), ColorName.of("Blue"), 0, 0, 255),
                Color.of(ColorId.of("WHITE_ID"), ColorName.of("White"), 255, 255, 255),
                Color.of(ColorId.of("BLACK_ID"), ColorName.of("Black"), 0, 0, 0)
        );

        // when: querying the colors used in fabrics with an anonymous agent
        colors = getColorsUsedInFabrics(Agent.anonymous());

        // then: the colors are returned
        assertThat(colors).containsExactlyInAnyOrder(
                Color.of(ColorId.of("BLUE_ID"), ColorName.of("Blue"), 0, 0, 255),
                Color.of(ColorId.of("WHITE_ID"), ColorName.of("White"), 255, 255, 255),
                Color.of(ColorId.of("BLACK_ID"), ColorName.of("Black"), 0, 0, 0)
        );

        // when: querying the colors used in fabrics with a system agent
        colors = getColorsUsedInFabrics(Agent.system());

        // then: the colors are returned
        assertThat(colors).containsExactlyInAnyOrder(
                Color.of(ColorId.of("BLUE_ID"), ColorName.of("Blue"), 0, 0, 255),
                Color.of(ColorId.of("WHITE_ID"), ColorName.of("White"), 255, 255, 255),
                Color.of(ColorId.of("BLACK_ID"), ColorName.of("Black"), 0, 0, 0)
        );
    }

}
