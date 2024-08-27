package de.bennyboer.kicherkrabbe.patterns;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.patterns.http.api.MoneyDTO;
import de.bennyboer.kicherkrabbe.patterns.http.api.PatternAttributionDTO;
import de.bennyboer.kicherkrabbe.patterns.http.api.PatternVariantDTO;
import de.bennyboer.kicherkrabbe.patterns.http.api.PricedSizeRangeDTO;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UpdatePatternCategoriesTest extends PatternsModuleTest {

    @Test
    void shouldUpdatePatternCategoriesAsUser() {
        // given: a user is allowed to create patterns
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: some categories are available
        markCategoryAsAvailable("DRESS_ID", "Dress");
        markCategoryAsAvailable("SKIRT_ID", "Skirt");
        markCategoryAsAvailable("NEW_CATEGORY_ID_1", "New category 1");
        markCategoryAsAvailable("NEW_CATEGORY_ID_2", "New category 2");
        markCategoryAsAvailable("NEW_CATEGORY_ID_3", "New category 3");

        // and: the user creates a pattern
        var variant = new PatternVariantDTO();
        variant.name = "Normal";
        var pricedSizeRange = new PricedSizeRangeDTO();
        pricedSizeRange.from = 80;
        pricedSizeRange.to = 86L;
        pricedSizeRange.price = new MoneyDTO();
        pricedSizeRange.price.amount = 1000;
        pricedSizeRange.price.currency = "EUR";
        variant.pricedSizeRanges = Set.of(pricedSizeRange);

        String patternId = createPattern(
                "Summerdress",
                null,
                new PatternAttributionDTO(),
                Set.of("DRESS_ID"),
                List.of("IMAGE_ID"),
                List.of(variant),
                List.of(),
                agent
        );

        // when: the user updates the categories of the pattern
        updatePatternCategories(
                patternId,
                0L,
                Set.of("NEW_CATEGORY_ID_1", "NEW_CATEGORY_ID_2", "NEW_CATEGORY_ID_3"),
                agent
        );

        // then: the pattern has the new categories
        var patterns = getPatterns(agent);
        assertThat(patterns).hasSize(1);
        var pattern = patterns.getFirst();
        assertThat(pattern.getId()).isEqualTo(PatternId.of(patternId));
        assertThat(pattern.getVersion()).isEqualTo(Version.of(1));
        assertThat(pattern.getCategories()).containsExactlyInAnyOrder(
                PatternCategoryId.of("NEW_CATEGORY_ID_1"),
                PatternCategoryId.of("NEW_CATEGORY_ID_2"),
                PatternCategoryId.of("NEW_CATEGORY_ID_3")
        );
    }

    @Test
    void shouldRaiseErrorWhenCategoriesAreMissing() {
        // given: a user is allowed to create patterns
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: some categories are available
        markCategoryAsAvailable("DRESS_ID", "Dress");
        markCategoryAsAvailable("SKIRT_ID", "Skirt");
        markCategoryAsAvailable("NEW_CATEGORY_ID_1", "New category 1");
        markCategoryAsAvailable("NEW_CATEGORY_ID_2", "New category 2");

        // and: the user creates a pattern
        var variant = new PatternVariantDTO();
        variant.name = "Normal";
        var pricedSizeRange = new PricedSizeRangeDTO();
        pricedSizeRange.from = 80;
        pricedSizeRange.to = 86L;
        pricedSizeRange.price = new MoneyDTO();
        pricedSizeRange.price.amount = 1000;
        pricedSizeRange.price.currency = "EUR";
        variant.pricedSizeRanges = Set.of(pricedSizeRange);

        String patternId = createPattern(
                "Summerdress",
                null,
                new PatternAttributionDTO(),
                Set.of("DRESS_ID"),
                List.of("IMAGE_ID"),
                List.of(variant),
                List.of(),
                agent
        );

        // when: the user updates the categories of the pattern with a missing category; then: an error is raised
        assertThatThrownBy(() -> updatePatternCategories(
                patternId,
                0L,
                Set.of("NEW_CATEGORY_ID_1", "NEW_CATEGORY_ID_2", "MISSING_CATEGORY_ID"),
                agent
        )).matches(e -> e.getCause() instanceof CategoriesMissingError
                && ((CategoriesMissingError) e.getCause()).getMissingCategories()
                .equals(Set.of(PatternCategoryId.of("MISSING_CATEGORY_ID"))));

        // when: the category is marked as available
        markCategoryAsAvailable("MISSING_CATEGORY_ID", "Missing category");

        // and: the user updates the categories of the pattern with the missing category again
        updatePatternCategories(
                patternId,
                0L,
                Set.of("NEW_CATEGORY_ID_1", "NEW_CATEGORY_ID_2", "MISSING_CATEGORY_ID"),
                agent
        );

        // then: the pattern has the new categories
        var patterns = getPatterns(agent);
        assertThat(patterns).hasSize(1);
        var pattern = patterns.getFirst();
        assertThat(pattern.getId()).isEqualTo(PatternId.of(patternId));
        assertThat(pattern.getVersion()).isEqualTo(Version.of(1));
        assertThat(pattern.getCategories()).containsExactlyInAnyOrder(
                PatternCategoryId.of("NEW_CATEGORY_ID_1"),
                PatternCategoryId.of("NEW_CATEGORY_ID_2"),
                PatternCategoryId.of("MISSING_CATEGORY_ID")
        );

        // when: the category is marked as unavailable
        markCategoryAsUnavailable("MISSING_CATEGORY_ID");

        // and: the user tries to update the categories of the pattern with the missing category again; then: an
        // error is raised
        assertThatThrownBy(() -> updatePatternCategories(
                patternId,
                2L,
                Set.of("NEW_CATEGORY_ID_1", "NEW_CATEGORY_ID_2", "MISSING_CATEGORY_ID"),
                agent
        )).matches(e -> e.getCause() instanceof CategoriesMissingError
                && ((CategoriesMissingError) e.getCause()).getMissingCategories()
                .equals(Set.of(PatternCategoryId.of("MISSING_CATEGORY_ID"))));
    }

    @Test
    void shouldNotUpdatePatternCategoriesWhenUserIsNotAllowed() {
        // when: a user that is not allowed to update a patterns categories tries to update the categories; then: an
        // error is raised
        assertThatThrownBy(() -> updatePatternCategories(
                "PATTERN_ID",
                0L,
                Set.of("NEW_CATEGORY_ID_1", "NEW_CATEGORY_ID_2", "NEW_CATEGORY_ID_3"),
                Agent.user(AgentId.of("USER_ID"))
        )).matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldNotUpdatePatternCategoriesGivenAnOutdatedVersion() {
        // given: a user that is allowed to create patterns
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: some categories are available
        markCategoryAsAvailable("DRESS_ID", "Dress");
        markCategoryAsAvailable("SKIRT_ID", "Skirt");
        markCategoryAsAvailable("NEW_CATEGORY_ID_1", "New category 1");
        markCategoryAsAvailable("NEW_CATEGORY_ID_2", "New category 2");
        markCategoryAsAvailable("NEW_CATEGORY_ID_3", "New category 3");

        // and: the user creates a pattern
        var variant = new PatternVariantDTO();
        variant.name = "Normal";
        var pricedSizeRange = new PricedSizeRangeDTO();
        pricedSizeRange.from = 80;
        pricedSizeRange.to = 86L;
        pricedSizeRange.price = new MoneyDTO();
        pricedSizeRange.price.amount = 1000;
        pricedSizeRange.price.currency = "EUR";
        variant.pricedSizeRanges = Set.of(pricedSizeRange);

        String patternId = createPattern(
                "Summerdress",
                null,
                new PatternAttributionDTO(),
                Set.of("DRESS_ID"),
                List.of("IMAGE_ID"),
                List.of(variant),
                List.of(),
                agent
        );

        // and: the pattern is published
        publishPattern(patternId, 0L, agent);

        // when: the user tries to update the pattern categories with an outdated version
        assertThatThrownBy(() -> updatePatternCategories(
                patternId,
                0L,
                Set.of("NEW_CATEGORY_ID_1", "NEW_CATEGORY_ID_2", "NEW_CATEGORY_ID_3"),
                agent
        )).matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldRaiseIllegalArgumentExceptionWhenCategoryIsBlank() {
        // given: a user that is allowed to create patterns
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: some categories are available
        markCategoryAsAvailable("DRESS_ID", "Dress");
        markCategoryAsAvailable("SKIRT_ID", "Skirt");
        markCategoryAsAvailable("NEW_CATEGORY_ID_1", "New category 1");
        markCategoryAsAvailable("NEW_CATEGORY_ID_2", "New category 2");

        // and: the user creates a pattern
        var variant = new PatternVariantDTO();
        variant.name = "Normal";
        var pricedSizeRange = new PricedSizeRangeDTO();
        pricedSizeRange.from = 80;
        pricedSizeRange.to = 86L;
        pricedSizeRange.price = new MoneyDTO();
        pricedSizeRange.price.amount = 1000;
        pricedSizeRange.price.currency = "EUR";
        variant.pricedSizeRanges = Set.of(pricedSizeRange);

        String patternId = createPattern(
                "Summerdress",
                null,
                new PatternAttributionDTO(),
                Set.of("DRESS_ID"),
                List.of("IMAGE_ID"),
                List.of(variant),
                List.of(),
                agent
        );

        // when: the user tries to update the pattern categories with an empty category
        assertThatThrownBy(() -> updatePatternCategories(
                patternId,
                0L,
                Set.of("NEW_CATEGORY_ID_1", "NEW_CATEGORY_ID_2", ""),
                agent
        )).isInstanceOf(IllegalArgumentException.class);
    }

}
