package de.bennyboer.kicherkrabbe.offers;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class QueryAvailableCategoriesForOffersTest extends OffersModuleTest {

    @Test
    void shouldGetAvailableCategories() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        markCategoryAsAvailable("CAT_1", "T-Shirts");
        markCategoryAsAvailable("CAT_2", "Hoodies");

        var categories = getAvailableCategoriesForOffers(agent);
        assertThat(categories).hasSize(2);
        assertThat(categories).extracting(c -> c.getId().getValue()).containsExactlyInAnyOrder("CAT_1", "CAT_2");
        assertThat(categories).extracting(c -> c.getName().getValue()).containsExactlyInAnyOrder("T-Shirts", "Hoodies");
    }

    @Test
    void shouldRenameAvailableCategory() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        markCategoryAsAvailable("CAT_1", "T-Shirts");
        renameCategoryIfAvailable("CAT_1", "Tops");

        var categories = getAvailableCategoriesForOffers(agent);
        assertThat(categories).hasSize(1);
        assertThat(categories.getFirst().getId().getValue()).isEqualTo("CAT_1");
        assertThat(categories.getFirst().getName().getValue()).isEqualTo("Tops");
    }

    @Test
    void shouldNotAddCategoryWhenRenamingNonExistentCategory() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        renameCategoryIfAvailable("CAT_1", "T-Shirts");

        var categories = getAvailableCategoriesForOffers(agent);
        assertThat(categories).isEmpty();
    }

    @Test
    void shouldRemoveUnavailableCategory() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        markCategoryAsAvailable("CAT_1", "T-Shirts");
        markCategoryAsAvailable("CAT_2", "Hoodies");
        markCategoryAsUnavailable("CAT_1");

        var categories = getAvailableCategoriesForOffers(agent);
        assertThat(categories).hasSize(1);
        assertThat(categories.getFirst().getId().getValue()).isEqualTo("CAT_2");
    }

}
