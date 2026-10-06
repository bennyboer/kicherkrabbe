package de.bennyboer.kicherkrabbe.embroideries;

import de.bennyboer.kicherkrabbe.embroideries.samples.SampleEmbroidery;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CleanupCategoriesTest extends EmbroideriesModuleTest {

    @Test
    void shouldRemoveCategoryFromEmbroideries() {
        // given: a user is allowed to create embroideries
        allowUserToCreateEmbroideries("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: some categories are available
        markCategoryAsAvailable("ANIMALS_ID", "Animals");
        markCategoryAsAvailable("SEA_ID", "Sea");

        // and: some embroideries using the categories
        String crabId = createEmbroidery(
                SampleEmbroidery.builder().name("Crab").category("ANIMALS_ID").category("SEA_ID").build(),
                agent
        );
        String foxId = createEmbroidery(SampleEmbroidery.builder().name("Fox").category("ANIMALS_ID").build(), agent);
        String shellId = createEmbroidery(SampleEmbroidery.builder().name("Shell").category("SEA_ID").build(), agent);

        // when: the animals category is removed from all embroideries
        removeCategoryFromEmbroideries("ANIMALS_ID");

        // then: the category is removed from the embroideries that used it
        var crab = getEmbroidery(crabId, agent);
        assertThat(crab.getCategories()).containsExactly(EmbroideryCategoryId.of("SEA_ID"));
        assertThat(crab.getVersion()).isEqualTo(Version.of(1));

        var fox = getEmbroidery(foxId, agent);
        assertThat(fox.getCategories()).isEmpty();
        assertThat(fox.getVersion()).isEqualTo(Version.of(1));

        // and: other embroideries are untouched
        var shell = getEmbroidery(shellId, agent);
        assertThat(shell.getCategories()).containsExactly(EmbroideryCategoryId.of("SEA_ID"));
        assertThat(shell.getVersion()).isEqualTo(Version.zero());
    }

}
