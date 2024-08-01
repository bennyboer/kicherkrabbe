package de.bennyboer.kicherkrabbe.categories;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static de.bennyboer.kicherkrabbe.categories.CategoryGroup.CLOTHING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DeleteCategoryTest extends CategoriesModuleTest {

    @Test
    void shouldDeleteCategory() {
        // given: a category
        allowUserToCreateCategories("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));
        var categoryId1 = createCategory("Dress", CLOTHING, agent);
        var categoryId2 = createCategory("Skirt", CLOTHING, agent);

        // when: the user deletes the first category
        deleteCategory(categoryId1, 0L, agent);

        // then: the first category is deleted
        var categories = getCategories(agent);
        assertThat(categories).hasSize(1);
        var category = categories.getFirst();
        assertThat(category.getId()).isEqualTo(CategoryId.of(categoryId2));
    }

    @Test
    void shouldNotDeleteCategoryIfNotHavingPermission() {
        // given: a category
        allowUserToCreateCategories("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));
        var categoryId = createCategory("Top", CLOTHING, agent);

        // when: another user tries to delete the category; then: an error is raised
        assertThatThrownBy(() -> deleteCategory(categoryId, 0L, Agent.user(AgentId.of("OTHER_USER_ID"))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

}
