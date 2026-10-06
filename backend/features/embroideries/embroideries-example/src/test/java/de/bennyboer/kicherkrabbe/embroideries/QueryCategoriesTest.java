package de.bennyboer.kicherkrabbe.embroideries;

import de.bennyboer.kicherkrabbe.embroideries.samples.SampleEmbroidery;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class QueryCategoriesTest extends EmbroideriesModuleTest {

    @Test
    void shouldQueryAvailableCategoriesForEmbroideries() {
        // given: a user is allowed to create embroideries
        allowUserToCreateEmbroideries("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: some categories are available
        markCategoryAsAvailable("ANIMALS_ID", "Animals");
        markCategoryAsAvailable("FLOWERS_ID", "Flowers");

        // when: the user queries the available categories
        var categories = getAvailableCategoriesForEmbroideries(agent);

        // then: all available categories are returned
        assertThat(categories).containsExactlyInAnyOrder(
                EmbroideryCategory.of(EmbroideryCategoryId.of("ANIMALS_ID"), EmbroideryCategoryName.of("Animals")),
                EmbroideryCategory.of(EmbroideryCategoryId.of("FLOWERS_ID"), EmbroideryCategoryName.of("Flowers"))
        );

        // when: a category is marked as unavailable
        markCategoryAsUnavailable("FLOWERS_ID");

        // then: the category is no longer returned
        assertThat(getAvailableCategoriesForEmbroideries(agent)).containsExactly(
                EmbroideryCategory.of(EmbroideryCategoryId.of("ANIMALS_ID"), EmbroideryCategoryName.of("Animals"))
        );
    }

    @Test
    void shouldUpdateAvailableCategoryName() {
        // given: a user is allowed to create embroideries
        allowUserToCreateEmbroideries("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: a category is available
        markCategoryAsAvailable("ANIMALS_ID", "Animals");

        // when: the category is marked as available again with another name
        markCategoryAsAvailable("ANIMALS_ID", "Wild animals");

        // then: the category is returned with the new name
        assertThat(getAvailableCategoriesForEmbroideries(agent)).containsExactly(
                EmbroideryCategory.of(EmbroideryCategoryId.of("ANIMALS_ID"), EmbroideryCategoryName.of("Wild animals"))
        );
    }

    @Test
    void shouldNotQueryAvailableCategoriesWhenUserIsNotAllowedToCreateEmbroideries() {
        // given: a category is available
        markCategoryAsAvailable("ANIMALS_ID", "Animals");

        // when: a user that is not allowed to create embroideries queries the available categories; then: an error is raised
        assertThatThrownBy(() -> getAvailableCategoriesForEmbroideries(Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldQueryCategoriesUsedInEmbroideries() {
        // given: a user is allowed to create embroideries
        allowUserToCreateEmbroideries("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: some categories are available
        markCategoryAsAvailable("ANIMALS_ID", "Animals");
        markCategoryAsAvailable("FLOWERS_ID", "Flowers");
        markCategoryAsAvailable("SEA_ID", "Sea");

        // and: some embroideries using some of the categories
        createEmbroidery(SampleEmbroidery.builder().name("Crab").category("ANIMALS_ID").category("SEA_ID").build(), agent);
        createEmbroidery(SampleEmbroidery.builder().name("Fox").category("ANIMALS_ID").build(), agent);

        // when: an anonymous user queries the categories used in embroideries
        var categories = getCategoriesUsedInEmbroideries(Agent.anonymous());

        // then: only the used categories are returned
        assertThat(categories).containsExactlyInAnyOrder(
                EmbroideryCategory.of(EmbroideryCategoryId.of("ANIMALS_ID"), EmbroideryCategoryName.of("Animals")),
                EmbroideryCategory.of(EmbroideryCategoryId.of("SEA_ID"), EmbroideryCategoryName.of("Sea"))
        );
    }

}
