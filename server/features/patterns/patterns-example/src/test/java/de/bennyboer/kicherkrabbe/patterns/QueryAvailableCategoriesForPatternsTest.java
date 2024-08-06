package de.bennyboer.kicherkrabbe.patterns;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class QueryAvailableCategoriesForPatternsTest extends PatternsModuleTest {

    @Test
    void shouldQueryCategoriesUsedInPatterns() {
        // given: a user is allowed to create patterns
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: some categories are available
        markCategoryAsAvailable("DRESS_ID", "Dress");
        markCategoryAsAvailable("SKIRT_ID", "Skirt");
        markCategoryAsAvailable("TROUSERS_ID", "Trousers");

        // when: querying the available categories for patterns with the user agent
        var categories = getAvailableCategoriesForPatterns(agent);

        // then: the categories are returned
        assertThat(categories).containsExactlyInAnyOrder(
                PatternCategory.of(PatternCategoryId.of("DRESS_ID"), PatternCategoryName.of("Dress")),
                PatternCategory.of(PatternCategoryId.of("SKIRT_ID"), PatternCategoryName.of("Skirt")),
                PatternCategory.of(PatternCategoryId.of("TROUSERS_ID"), PatternCategoryName.of("Trousers"))
        );

        // when: querying the categories used in patterns with an anonymous agent; then: an error is raised
        assertThatThrownBy(() -> getAvailableCategoriesForPatterns(Agent.anonymous()))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

}
