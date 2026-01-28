package de.bennyboer.kicherkrabbe.categories;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static de.bennyboer.kicherkrabbe.categories.CategoryGroup.CLOTHING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RenameCategoryTest extends CategoriesModuleTest {

    @Test
    void shouldRenameCategory() {
        // given: a category
        allowUserToCreateCategories("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));
        var categoryId = createCategory("Trousers", CLOTHING, agent);

        // when: the user renames the category
        renameCategory(categoryId, 0L, "Top", agent);

        // then: the category is renamed
        var categories = getCategories(agent);
        assertThat(categories).hasSize(1);
        var category = categories.getFirst();
        assertThat(category.getId()).isEqualTo(CategoryId.of(categoryId));
        assertThat(category.getName()).isEqualTo(CategoryName.of("Top"));
    }

    @Test
    void shouldNotRenameCategoryIfNotHavingPermission() {
        // given: a category
        allowUserToCreateCategories("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));
        var categoryId = createCategory("Dress", CLOTHING, agent);

        // when: another user tries to rename the category; then: an error is raised
        assertThatThrownBy(() -> renameCategory(
                categoryId,
                0L,
                "Sandals",
                Agent.user(AgentId.of("OTHER_USER_ID"))
        )).matches(e -> e.getCause() instanceof MissingPermissionError);
    }

}
