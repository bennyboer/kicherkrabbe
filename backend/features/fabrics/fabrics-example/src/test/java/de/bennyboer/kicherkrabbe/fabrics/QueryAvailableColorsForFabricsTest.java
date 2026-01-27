package de.bennyboer.kicherkrabbe.fabrics;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.fabrics.persistence.colors.Color;
import de.bennyboer.kicherkrabbe.fabrics.persistence.colors.ColorName;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class QueryAvailableColorsForFabricsTest extends FabricsModuleTest {

    @Test
    void shouldQueryAvailableColorsForFabrics() {
        // given: a user is allowed to create fabrics
        allowUserToCreateFabrics("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: some colors are available
        markColorAsAvailable("BLUE_ID", "Blue", 0, 0, 255);
        markColorAsAvailable("WHITE_ID", "White", 255, 255, 255);
        markColorAsAvailable("BLACK_ID", "Black", 0, 0, 0);

        // when: querying the colors available for fabrics with the user agent
        var colors = getAvailableColorsForFabrics(agent);

        // then: the colors are returned
        assertThat(colors).containsExactlyInAnyOrder(
                Color.of(ColorId.of("BLUE_ID"), ColorName.of("Blue"), 0, 0, 255),
                Color.of(ColorId.of("WHITE_ID"), ColorName.of("White"), 255, 255, 255),
                Color.of(ColorId.of("BLACK_ID"), ColorName.of("Black"), 0, 0, 0)
        );

        // when: querying the colors used in fabrics with an anonymous agent; then: an error is raised
        assertThatThrownBy(() -> getAvailableColorsForFabrics(Agent.anonymous()))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

}
