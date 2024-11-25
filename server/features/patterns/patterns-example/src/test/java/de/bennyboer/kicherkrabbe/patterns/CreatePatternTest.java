package de.bennyboer.kicherkrabbe.patterns;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.money.Money;
import de.bennyboer.kicherkrabbe.patterns.http.api.*;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CreatePatternTest extends PatternsModuleTest {

    @Test
    void shouldCreatePatternAsUser() {
        // given: a user is allowed to create patterns
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: some categories are available
        markCategoryAsAvailable("DRESS_ID", "Dress");
        markCategoryAsAvailable("SKIRT_ID", "Skirt");

        // when: the user creates a pattern
        var attribution = new PatternAttributionDTO();
        attribution.originalPatternName = "Summerdress EXTREME";
        attribution.designer = "EXTREME PATTERNS";

        var pricedSizeRange1 = new PricedSizeRangeDTO();
        pricedSizeRange1.from = 80;
        pricedSizeRange1.to = 86L;
        pricedSizeRange1.price = new MoneyDTO();
        pricedSizeRange1.price.amount = 2900;
        pricedSizeRange1.price.currency = "EUR";
        var pricedSizeRange2 = new PricedSizeRangeDTO();
        pricedSizeRange2.from = 92;
        pricedSizeRange2.to = 98L;
        pricedSizeRange2.price = new MoneyDTO();
        pricedSizeRange2.price.amount = 3200;
        pricedSizeRange2.price.currency = "EUR";

        var shortVariant = new PatternVariantDTO();
        shortVariant.name = "Short";
        shortVariant.pricedSizeRanges = Set.of(
                pricedSizeRange1,
                pricedSizeRange2
        );
        var longVariant = new PatternVariantDTO();
        longVariant.name = "Long";
        longVariant.pricedSizeRanges = Set.of(
                pricedSizeRange1,
                pricedSizeRange2
        );

        var extra1 = new PatternExtraDTO();
        extra1.name = "Extra 1";
        extra1.price = new MoneyDTO();
        extra1.price.amount = 1000;
        extra1.price.currency = "EUR";
        var extra2 = new PatternExtraDTO();
        extra2.name = "Extra 2";
        extra2.price = new MoneyDTO();
        extra2.price.amount = 2000;
        extra2.price.currency = "EUR";

        String patternId = createPattern(
                "Summerdress",
                "S-D-SUM-1",
                "A nice dress for the summer",
                attribution,
                Set.of("DRESS_ID", "SKIRT_ID"),
                List.of("IMAGE_ID_1", "IMAGE_ID_2"),
                List.of(shortVariant, longVariant),
                List.of(extra1, extra2),
                agent
        );

        // then: the pattern is created
        var patterns = getPatterns(agent);
        assertThat(patterns).hasSize(1);
        var pattern = patterns.getFirst();
        assertThat(pattern.getId()).isEqualTo(PatternId.of(patternId));
        assertThat(pattern.getVersion()).isEqualTo(Version.zero());
        assertThat(pattern.getName()).isEqualTo(PatternName.of("Summerdress"));
        assertThat(pattern.getDescription()).contains(PatternDescription.of("A nice dress for the summer"));
        assertThat(pattern.getAttribution().getOriginalPatternName()).isEqualTo(
                Optional.of(OriginalPatternName.of("Summerdress EXTREME"))
        );
        assertThat(pattern.getAttribution().getDesigner()).isEqualTo(
                Optional.of(PatternDesigner.of("EXTREME PATTERNS"))
        );
        assertThat(pattern.getCategories()).containsExactlyInAnyOrder(
                PatternCategoryId.of("DRESS_ID"),
                PatternCategoryId.of("SKIRT_ID")
        );
        assertThat(pattern.getImages()).containsExactly(
                ImageId.of("IMAGE_ID_1"),
                ImageId.of("IMAGE_ID_2")
        );
        assertThat(pattern.getVariants()).hasSize(2);
        assertThat(pattern.getVariants().get(0).getName()).isEqualTo(PatternVariantName.of("Short"));
        assertThat(pattern.getVariants().get(0).getPricedSizeRanges()).containsExactlyInAnyOrder(
                PricedSizeRange.of(
                        80,
                        86L,
                        null,
                        Money.euro(2900)
                ),
                PricedSizeRange.of(
                        92,
                        98L,
                        null,
                        Money.euro(3200)
                )
        );
        assertThat(pattern.getVariants().get(1).getName()).isEqualTo(PatternVariantName.of("Long"));
        assertThat(pattern.getVariants().get(1).getPricedSizeRanges()).containsExactlyInAnyOrder(
                PricedSizeRange.of(
                        80,
                        86L,
                        null,
                        Money.euro(2900)
                ),
                PricedSizeRange.of(
                        92,
                        98L,
                        null,
                        Money.euro(3200)
                )
        );
        assertThat(pattern.getExtras()).containsExactlyInAnyOrder(
                PatternExtra.of(
                        PatternExtraName.of("Extra 1"),
                        Money.euro(1000)
                ),
                PatternExtra.of(
                        PatternExtraName.of("Extra 2"),
                        Money.euro(2000)
                )
        );
    }

    @Test
    void shouldNotBeAbleToCreatePatternGivenAnInvalidPattern() {
        String validName = "Summerdress";
        String invalidName1 = "";
        String invalidName2 = null;

        String validNumber = "S-D-SUM-1";
        String invalidNumber1 = "";
        String invalidNumber2 = "P-22";

        var validAttribution1 = new PatternAttributionDTO();
        validAttribution1.originalPatternName = "Summerdress EXTREME";
        validAttribution1.designer = "EXTREME PATTERNS";
        PatternAttributionDTO invalidAttribution = null;

        Set<String> validCategories = Set.of("DRESS_ID", "SKIRT_ID");
        Set<String> invalidCategories1 = Set.of("", "SKIRT_ID");
        Set<String> invalidCategories2 = null;

        List<String> validImages = List.of("IMAGE_ID_1", "IMAGE_ID_2");
        List<String> invalidImages1 = List.of("IMAGE_ID_1", "");
        List<String> invalidImages2 = List.of();
        List<String> invalidImages3 = null;

        var pricedSizeRange = new PricedSizeRangeDTO();
        pricedSizeRange.from = 80;
        pricedSizeRange.to = 86L;
        pricedSizeRange.price = new MoneyDTO();
        pricedSizeRange.price.amount = 2900;
        pricedSizeRange.price.currency = "EUR";

        var validVariant = new PatternVariantDTO();
        validVariant.name = "Short";
        validVariant.pricedSizeRanges = Set.of(pricedSizeRange);
        var invalidVariant1 = new PatternVariantDTO();
        invalidVariant1.name = "";
        invalidVariant1.pricedSizeRanges = Set.of(pricedSizeRange);
        var invalidVariant2 = new PatternVariantDTO();
        invalidVariant2.name = "Short";
        invalidVariant2.pricedSizeRanges = null;
        var invalidVariant3 = new PatternVariantDTO();
        invalidVariant3.name = "Short";
        invalidVariant3.pricedSizeRanges = Set.of();

        var validExtra = new PatternExtraDTO();
        validExtra.name = "Extra 1";
        validExtra.price = new MoneyDTO();
        validExtra.price.amount = 1000;
        validExtra.price.currency = "EUR";
        var invalidExtra1 = new PatternExtraDTO();
        invalidExtra1.name = "";
        invalidExtra1.price = new MoneyDTO();
        invalidExtra1.price.amount = 1000;
        invalidExtra1.price.currency = "EUR";
        var invalidExtra2 = new PatternExtraDTO();
        invalidExtra2.name = "Extra 1";
        invalidExtra2.price = null;

        // given: a user is allowed to create patterns
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // when: the user tries to create a pattern without name; then: an error is raised
        assertThatThrownBy(() -> createPattern(
                invalidName1,
                validNumber,
                null,
                validAttribution1,
                validCategories,
                validImages,
                List.of(validVariant),
                List.of(validExtra),
                agent
        )).isInstanceOf(IllegalArgumentException.class);

        // when: the user tries to create a pattern with null name; then: an error is raised
        assertThatThrownBy(() -> createPattern(
                invalidName2,
                validNumber,
                null,
                validAttribution1,
                validCategories,
                validImages,
                List.of(validVariant),
                List.of(validExtra),
                agent
        )).isInstanceOf(IllegalArgumentException.class);

        // when: the user tries to create a pattern without number; then: an error is raised
        assertThatThrownBy(() -> createPattern(
                validName,
                invalidNumber1,
                null,
                validAttribution1,
                validCategories,
                validImages,
                List.of(validVariant),
                List.of(validExtra),
                agent
        )).isInstanceOf(IllegalArgumentException.class);

        // when: the user tries to create a pattern with an invalid number; then: an error is raised
        assertThatThrownBy(() -> createPattern(
                validName,
                invalidNumber2,
                null,
                validAttribution1,
                validCategories,
                validImages,
                List.of(validVariant),
                List.of(validExtra),
                agent
        )).isInstanceOf(IllegalArgumentException.class);

        // when: the user tries to create a pattern without attribution; then: an error is raised
        assertThatThrownBy(() -> createPattern(
                validName,
                validNumber,
                null,
                invalidAttribution,
                validCategories,
                validImages,
                List.of(validVariant),
                List.of(validExtra),
                agent
        )).isInstanceOf(IllegalArgumentException.class);

        // when: the user tries to create a pattern with faulty categories; then: an error is raised
        assertThatThrownBy(() -> createPattern(
                validName,
                validNumber,
                "",
                validAttribution1,
                invalidCategories1,
                validImages,
                List.of(validVariant),
                List.of(validExtra),
                agent
        )).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> createPattern(
                validName,
                validNumber,
                "",
                validAttribution1,
                invalidCategories2,
                validImages,
                List.of(validVariant),
                List.of(validExtra),
                agent
        )).isInstanceOf(IllegalArgumentException.class);

        // when: the user tries to create a pattern with faulty images; then: an error is raised
        assertThatThrownBy(() -> createPattern(
                validName,
                validNumber,
                "",
                validAttribution1,
                validCategories,
                invalidImages1,
                List.of(validVariant),
                List.of(validExtra),
                agent
        )).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> createPattern(
                validName,
                validNumber,
                null,
                validAttribution1,
                validCategories,
                invalidImages2,
                List.of(validVariant),
                List.of(validExtra),
                agent
        )).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> createPattern(
                validName,
                validNumber,
                "",
                validAttribution1,
                validCategories,
                invalidImages3,
                List.of(validVariant),
                List.of(validExtra),
                agent
        )).isInstanceOf(IllegalArgumentException.class);

        // when: the user tries to create a pattern with faulty variants; then: an error is raised
        assertThatThrownBy(() -> createPattern(
                validName,
                validNumber,
                null,
                validAttribution1,
                validCategories,
                validImages,
                List.of(),
                List.of(validExtra),
                agent
        )).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> createPattern(
                validName,
                validNumber,
                null,
                validAttribution1,
                validCategories,
                validImages,
                List.of(invalidVariant1),
                List.of(validExtra),
                agent
        )).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> createPattern(
                validName,
                validNumber,
                "",
                validAttribution1,
                validCategories,
                validImages,
                List.of(invalidVariant2),
                List.of(validExtra),
                agent
        )).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> createPattern(
                validName,
                validNumber,
                "",
                validAttribution1,
                validCategories,
                validImages,
                List.of(invalidVariant3),
                List.of(validExtra),
                agent
        )).isInstanceOf(IllegalArgumentException.class);

        // when: the user tries to create a pattern with faulty extras; then: an error is raised
        assertThatThrownBy(() -> createPattern(
                validName,
                validNumber,
                null,
                validAttribution1,
                validCategories,
                validImages,
                List.of(validVariant),
                List.of(invalidExtra1),
                agent
        )).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> createPattern(
                validName,
                validNumber,
                null,
                validAttribution1,
                validCategories,
                validImages,
                List.of(validVariant),
                List.of(invalidExtra2),
                agent
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldNotCreatePatternWhenCategoriesAreMissing() {
        // given: a user is allowed to create patterns
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: some categories are available
        markCategoryAsAvailable("SKIRT_ID", "Skirt");
        markCategoryAsAvailable("DRESS_ID", "Dress");

        // when: the user creates a pattern with a missing category; then: an error is raised
        var variant = new PatternVariantDTO();
        variant.name = "Test";
        var pricedSizeRange = new PricedSizeRangeDTO();
        pricedSizeRange.from = 80;
        pricedSizeRange.price = new MoneyDTO();
        pricedSizeRange.price.amount = 2900;
        pricedSizeRange.price.currency = "EUR";
        variant.pricedSizeRanges = Set.of(pricedSizeRange);

        assertThatThrownBy(() -> createPattern(
                "Partydress",
                "S-D-PAR-1",
                null,
                new PatternAttributionDTO(),
                Set.of("SKIRT_ID", "MISSING_ID"),
                List.of("IMAGE_ID"),
                List.of(variant),
                List.of(),
                agent
        )).matches(e -> e.getCause() instanceof CategoriesMissingError
                && ((CategoriesMissingError) e.getCause()).getMissingCategories()
                .equals(Set.of(PatternCategoryId.of("MISSING_ID"))));

        // when: the category is marked as available
        markCategoryAsAvailable("MISSING_ID", "Missing");

        // and: the user creates a pattern with all categories
        createPattern(
                "Partydress",
                "S-D-PAR-1",
                null,
                new PatternAttributionDTO(),
                Set.of("SKIRT_ID", "DRESS_ID", "MISSING_ID"),
                List.of("IMAGE_ID"),
                List.of(variant),
                List.of(),
                agent
        );

        // then: the pattern is created
        var patterns = getPatterns(agent);
        assertThat(patterns).hasSize(1);

        // when: the category is marked as unavailable again
        markCategoryAsUnavailable("MISSING_ID");

        // and: the user tries to create a pattern with all categories; then: an error is raised
        assertThatThrownBy(() -> createPattern(
                "Partydress",
                "S-D-PAR-1",
                null,
                new PatternAttributionDTO(),
                Set.of("SKIRT_ID", "DRESS_ID", "MISSING_ID"),
                List.of("IMAGE_ID"),
                List.of(variant),
                List.of(),
                agent
        )).matches(e -> e.getCause() instanceof CategoriesMissingError
                && ((CategoriesMissingError) e.getCause()).getMissingCategories()
                .equals(Set.of(PatternCategoryId.of("MISSING_ID"))));
    }

    @Test
    void shouldNotCreatePatternWhenUserIsNotAllowed() {
        var variant = new PatternVariantDTO();
        variant.name = "Test";
        var pricedSizeRange = new PricedSizeRangeDTO();
        pricedSizeRange.from = 80;
        pricedSizeRange.price = new MoneyDTO();
        pricedSizeRange.price.amount = 2900;
        pricedSizeRange.price.currency = "EUR";
        variant.pricedSizeRanges = Set.of(pricedSizeRange);

        // when: a user that is not allowed to create a pattern tries to create a pattern; then: an error is raised
        assertThatThrownBy(() -> createPattern(
                "Test",
                "S-D-TEST-1",
                null,
                new PatternAttributionDTO(),
                Set.of(),
                List.of("IMAGE_ID"),
                List.of(variant),
                List.of(),
                Agent.user(AgentId.of("USER_ID"))
        )).matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldCreateMultiplePatterns() {
        // given: a user is allowed to create patterns
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: some categories are available
        markCategoryAsAvailable("DRESS_ID", "Dress");
        markCategoryAsAvailable("SKIRT_ID", "Skirt");

        // when: the user creates multiple patterns
        var variant = new PatternVariantDTO();
        variant.name = "Test";
        var pricedSizeRange = new PricedSizeRangeDTO();
        pricedSizeRange.from = 80;
        pricedSizeRange.price = new MoneyDTO();
        pricedSizeRange.price.amount = 2900;
        pricedSizeRange.price.currency = "EUR";
        variant.pricedSizeRanges = Set.of(pricedSizeRange);

        createPattern(
                "Partydress",
                "S-D-PAR-1",
                null,
                new PatternAttributionDTO(),
                Set.of("DRESS_ID"),
                List.of("IMAGE_ID"),
                List.of(variant),
                List.of(),
                agent
        );
        createPattern(
                "Summerdress",
                "S-D-SUM-1",
                null,
                new PatternAttributionDTO(),
                Set.of("SKIRT_ID"),
                List.of("IMAGE_ID"),
                List.of(variant),
                List.of(),
                agent
        );
        createPattern(
                "Winterdress",
                "S-D-WIN-1",
                null,
                new PatternAttributionDTO(),
                Set.of("DRESS_ID"),
                List.of("IMAGE_ID"),
                List.of(variant),
                List.of(),
                agent
        );

        // then: the patterns are created
        var patterns = getPatterns(agent);
        assertThat(patterns).hasSize(3);

        // and: they have different IDs
        var patternIds = patterns.stream().map(PatternDetails::getId).toList();
        assertThat(patternIds).doesNotHaveDuplicates();

        // and: they all have version zero
        var versions = patterns.stream().map(PatternDetails::getVersion).toList();
        assertThat(versions).allMatch(v -> v.equals(Version.zero()));
    }

    @Test
    void shouldBeAbleToCreatePatternGivenNoDescription() {
        // given: a user is allowed to create patterns
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // when: the user creates a pattern without description
        var attribution = new PatternAttributionDTO();
        var pricedSizeRange = new PricedSizeRangeDTO();
        pricedSizeRange.from = 80;
        pricedSizeRange.to = 86L;
        pricedSizeRange.price = new MoneyDTO();
        pricedSizeRange.price.amount = 2900;
        pricedSizeRange.price.currency = "EUR";
        var variant = new PatternVariantDTO();
        variant.name = "Short";
        variant.pricedSizeRanges = Set.of(pricedSizeRange);

        String patternId = createPattern(
                "Summerdress",
                "S-D-SUM-1",
                null,
                attribution,
                Set.of(),
                List.of("IMAGE_ID"),
                List.of(variant),
                List.of(),
                agent
        );

        // then: the pattern is created
        var patterns = getPatterns(agent);
        assertThat(patterns).hasSize(1);
        var pattern = patterns.getFirst();
        assertThat(pattern.getId()).isEqualTo(PatternId.of(patternId));
        assertThat(pattern.getVersion()).isEqualTo(Version.zero());
        assertThat(pattern.getDescription()).isEmpty();

        // when: the user creates a pattern with an empty description
        patternId = createPattern(
                "Next",
                "S-D-NXT-1",
                "",
                attribution,
                Set.of(),
                List.of("IMAGE_ID"),
                List.of(variant),
                List.of(),
                agent
        );

        // then: the pattern is created
        patterns = getPatterns(agent);
        assertThat(patterns).hasSize(2);

        pattern = getPattern(patternId, agent);
        assertThat(pattern.getDescription()).isEmpty();
    }

}
