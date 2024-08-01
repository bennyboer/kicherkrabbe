package de.bennyboer.kicherkrabbe.categories;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import org.junit.jupiter.api.Test;

import static de.bennyboer.kicherkrabbe.categories.CategoryGroup.CLOTHING;
import static de.bennyboer.kicherkrabbe.categories.CategoryGroup.NONE;
import static org.assertj.core.api.Assertions.assertThat;

public class QueryCategoriesTest extends CategoriesModuleTest {

    @Test
    void shouldGetAllAccessibleCategories() {
        // given: some categories for different users
        allowUserToCreateCategories("USER_ID_1");
        var agent1 = Agent.user(AgentId.of("USER_ID_1"));
        allowUserToCreateCategories("USER_ID_2");
        var agent2 = Agent.user(AgentId.of("USER_ID_2"));

        var categoryId1 = createCategory("Dress", CLOTHING, agent1);
        var categoryId2 = createCategory("Skirt", CLOTHING, agent2);
        var categoryId3 = createCategory("Top", CLOTHING, agent1);
        var categoryId4 = createCategory("Shoes", NONE, agent1);

        // when: getting all categories for the first user
        var categories1 = getCategories(agent1);

        // then: the categories for the first user are returned
        assertThat(categories1).hasSize(3);
        var categoryIds1 = categories1.stream()
                .map(CategoryDetails::getId)
                .map(CategoryId::getValue)
                .toList();
        assertThat(categoryIds1).containsExactlyInAnyOrder(categoryId1, categoryId3, categoryId4);

        // when: getting all categories for the second user
        var categories2 = getCategories(agent2);

        // then: the categories for the second user are returned
        assertThat(categories2).hasSize(1);
        var categoryIds2 = categories2.stream()
                .map(CategoryDetails::getId)
                .map(CategoryId::getValue)
                .toList();
        assertThat(categoryIds2).containsExactlyInAnyOrder(categoryId2);
    }

    @Test
    void shouldGetAccessibleCategoriesFilteredByGroup() {
        // given: some categories with different groups
        allowUserToCreateCategories("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        var categoryId1 = createCategory("Dress", CLOTHING, agent);
        var categoryId2 = createCategory("Skirt", CLOTHING, agent);
        var categoryId3 = createCategory("Top", CLOTHING, agent);
        var categoryId4 = createCategory("Shoes", NONE, agent);

        // when: getting all clothing categories
        var categories = getCategoriesByGroup(CLOTHING, agent);

        // then: the clothing categories are returned
        assertThat(categories).hasSize(3);
        var categoryIds = categories.stream()
                .map(CategoryDetails::getId)
                .map(CategoryId::getValue)
                .toList();
        assertThat(categoryIds).containsExactlyInAnyOrder(categoryId1, categoryId2, categoryId3);

        // when: getting all categories without a group
        categories = getCategoriesByGroup(NONE, agent);

        // then: the categories without a group are returned
        assertThat(categories).hasSize(1);
        categoryIds = categories.stream()
                .map(CategoryDetails::getId)
                .map(CategoryId::getValue)
                .toList();
        assertThat(categoryIds).containsExactlyInAnyOrder(categoryId4);
    }

    @Test
    void shouldGetASpecificCategory() {
        // given: a category
        allowUserToCreateCategories("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));
        var categoryId = createCategory("Dress", CLOTHING, agent);

        // when: getting the category
        var category = getCategory(categoryId, agent);

        // then: the category is returned
        assertThat(category.getId()).isEqualTo(CategoryId.of(categoryId));
        assertThat(category.getName()).isEqualTo(CategoryName.of("Dress"));
        assertThat(category.getGroup()).isEqualTo(CLOTHING);
    }

}
