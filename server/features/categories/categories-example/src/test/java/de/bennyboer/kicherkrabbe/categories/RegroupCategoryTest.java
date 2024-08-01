package de.bennyboer.kicherkrabbe.categories;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static de.bennyboer.kicherkrabbe.categories.CategoryGroup.CLOTHING;
import static de.bennyboer.kicherkrabbe.categories.CategoryGroup.NONE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RegroupCategoryTest extends CategoriesModuleTest {

    @Test
    void shouldRegroupCategory() {
        // given: a category
        allowUserToCreateCategories("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));
        var categoryId = createCategory("Trousers", CLOTHING, agent);

        // when: the user regroups the category
        regroupCategory(categoryId, 0L, NONE, agent);

        // then: the category is regrouped
        var categories = getCategories(agent);
        assertThat(categories).hasSize(1);
        var category = categories.getFirst();
        assertThat(category.getId()).isEqualTo(CategoryId.of(categoryId));
        assertThat(category.getGroup()).isEqualTo(NONE);
    }

    @Test
    void shouldNotRegroupCategoryIfNotHavingPermission() {
        // given: a category
        allowUserToCreateCategories("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));
        var categoryId = createCategory("Dress", CLOTHING, agent);

        // when: another user tries to regroup the category; then: an error is raised
        assertThatThrownBy(() -> regroupCategory(
                categoryId,
                0L,
                NONE,
                Agent.user(AgentId.of("OTHER_USER_ID"))
        )).matches(e -> e.getCause() instanceof MissingPermissionError);
    }

}
