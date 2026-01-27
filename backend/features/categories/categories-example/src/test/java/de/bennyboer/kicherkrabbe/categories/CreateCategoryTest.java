package de.bennyboer.kicherkrabbe.categories;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static de.bennyboer.kicherkrabbe.categories.CategoryGroup.CLOTHING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CreateCategoryTest extends CategoriesModuleTest {

    @Test
    void shouldCreateCategoryAsUser() {
        // given: a user is allowed to create categories
        allowUserToCreateCategories("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // when: the user creates a category
        String categoryId = createCategory("Dress", CLOTHING, agent);

        // then: the category is created
        var categories = getCategories(agent);
        assertThat(categories).hasSize(1);
        var category = categories.getFirst();
        assertThat(category.getId()).isEqualTo(CategoryId.of(categoryId));
        assertThat(category.getName()).isEqualTo(CategoryName.of("Dress"));
        assertThat(category.getGroup()).isEqualTo(CLOTHING);
    }

    @Test
    void shouldNotBeAbleToCreateCategoryGivenAnInvalidName() {
        // given: a user is allowed to create categories
        allowUserToCreateCategories("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // when: the user creates a category with an invalid name; then: an error is raised
        assertThatThrownBy(() -> createCategory("", CLOTHING, agent))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldNotCreateCategoryWhenUserIsNotAllowed() {
        // when: a user that is not allowed to create a category tries to create a category; then: an error is raised
        assertThatThrownBy(() -> createCategory("Skirt", CLOTHING, Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldCreateMultipleCategories() {
        // given: a user is allowed to create categories
        allowUserToCreateCategories("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // when: the user creates multiple categories
        createCategory("Dress", CLOTHING, agent);
        createCategory("Top", CLOTHING, agent);
        createCategory("Skirt", CLOTHING, agent);

        // then: the categories are created
        var categories = getCategories(agent);
        assertThat(categories).hasSize(3);
    }

}
