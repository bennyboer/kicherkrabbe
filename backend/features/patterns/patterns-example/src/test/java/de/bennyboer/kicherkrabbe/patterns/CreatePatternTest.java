package de.bennyboer.kicherkrabbe.patterns;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.money.Money;
import de.bennyboer.kicherkrabbe.patterns.http.api.*;
import de.bennyboer.kicherkrabbe.patterns.samples.SampleMoney;
import de.bennyboer.kicherkrabbe.patterns.samples.SamplePattern;
import de.bennyboer.kicherkrabbe.patterns.samples.SamplePatternAttribution;
import de.bennyboer.kicherkrabbe.patterns.samples.SamplePatternExtra;
import de.bennyboer.kicherkrabbe.patterns.samples.SamplePatternVariant;
import de.bennyboer.kicherkrabbe.patterns.samples.SamplePricedSizeRange;
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
        var attribution = SamplePatternAttribution.builder()
                .originalPatternName("Summerdress EXTREME")
                .designer("EXTREME PATTERNS")
                .build()
                .toDTO();

        var pricedSizeRange1 = SamplePricedSizeRange.builder()
                .from(80)
                .to(86L)
                .price(SampleMoney.builder().amount(2900).build())
                .build();
        var pricedSizeRange2 = SamplePricedSizeRange.builder()
                .from(92)
                .to(98L)
                .price(SampleMoney.builder().amount(3200).build())
                .build();

        var shortVariant = SamplePatternVariant.builder()
                .name("Short")
                .pricedSizeRange(pricedSizeRange1)
                .pricedSizeRange(pricedSizeRange2)
                .build()
                .toDTO();
        var longVariant = SamplePatternVariant.builder()
                .name("Long")
                .pricedSizeRange(pricedSizeRange1)
                .pricedSizeRange(pricedSizeRange2)
                .build()
                .toDTO();

        var extra1 = SamplePatternExtra.builder()
                .name("Extra 1")
                .price(SampleMoney.builder().amount(1000).build())
                .build()
                .toDTO();
        var extra2 = SamplePatternExtra.builder()
                .name("Extra 2")
                .price(SampleMoney.builder().amount(2000).build())
                .build()
                .toDTO();

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
                PricedSizeRange.of(80, 86L, null, Money.euro(2900)),
                PricedSizeRange.of(92, 98L, null, Money.euro(3200))
        );
        assertThat(pattern.getVariants().get(1).getName()).isEqualTo(PatternVariantName.of("Long"));
        assertThat(pattern.getVariants().get(1).getPricedSizeRanges()).containsExactlyInAnyOrder(
                PricedSizeRange.of(80, 86L, null, Money.euro(2900)),
                PricedSizeRange.of(92, 98L, null, Money.euro(3200))
        );
        assertThat(pattern.getExtras()).containsExactlyInAnyOrder(
                PatternExtra.of(PatternExtraName.of("Extra 1"), Money.euro(1000)),
                PatternExtra.of(PatternExtraName.of("Extra 2"), Money.euro(2000))
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

        var validAttribution = SamplePatternAttribution.builder().build().toDTO();
        PatternAttributionDTO invalidAttribution = null;

        Set<String> validCategories = Set.of("DRESS_ID", "SKIRT_ID");
        Set<String> invalidCategories1 = Set.of("", "SKIRT_ID");
        Set<String> invalidCategories2 = null;

        List<String> validImages = List.of("IMAGE_ID_1", "IMAGE_ID_2");
        List<String> invalidImages1 = List.of("IMAGE_ID_1", "");
        List<String> invalidImages2 = List.of();
        List<String> invalidImages3 = null;

        var validVariant = SamplePatternVariant.builder().build().toDTO();
        var pricedSizeRange = SamplePricedSizeRange.builder().build().toDTO();
        var invalidVariant1 = new PatternVariantDTO();
        invalidVariant1.name = "";
        invalidVariant1.pricedSizeRanges = Set.of(pricedSizeRange);
        var invalidVariant2 = new PatternVariantDTO();
        invalidVariant2.name = "Short";
        invalidVariant2.pricedSizeRanges = null;
        var invalidVariant3 = new PatternVariantDTO();
        invalidVariant3.name = "Short";
        invalidVariant3.pricedSizeRanges = Set.of();

        var validExtra = SamplePatternExtra.builder().build().toDTO();
        var invalidExtra1 = new PatternExtraDTO();
        invalidExtra1.name = "";
        invalidExtra1.price = SampleMoney.builder().build().toDTO();
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
                validAttribution,
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
                validAttribution,
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
                validAttribution,
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
                validAttribution,
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
                validAttribution,
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
                validAttribution,
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
                validAttribution,
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
                validAttribution,
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
                validAttribution,
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
                validAttribution,
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
                validAttribution,
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
                validAttribution,
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
                validAttribution,
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
                validAttribution,
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
                validAttribution,
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
        var variant = SamplePatternVariant.builder().build().toDTO();

        assertThatThrownBy(() -> createPattern(
                "Partydress",
                "S-D-PAR-1",
                null,
                SamplePatternAttribution.builder().build().toDTO(),
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
                SamplePatternAttribution.builder().build().toDTO(),
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
                SamplePatternAttribution.builder().build().toDTO(),
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
        // when: a user that is not allowed to create a pattern tries to create a pattern; then: an error is raised
        assertThatThrownBy(() -> createPattern(
                "Test",
                "S-D-TEST-1",
                null,
                SamplePatternAttribution.builder().build().toDTO(),
                Set.of(),
                List.of("IMAGE_ID"),
                List.of(SamplePatternVariant.builder().build().toDTO()),
                List.of(),
                Agent.user(AgentId.of("USER_ID"))
        )).matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldCreateMultiplePatterns() {
        // given: a user is allowed to create patterns
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // when: the user creates multiple patterns with different names and numbers
        createPattern(SamplePattern.builder().name("Party Dress").number("S-D-PAR-1").build(), agent);
        createPattern(SamplePattern.builder().name("Summer Dress").number("S-D-SUM-1").build(), agent);
        createPattern(SamplePattern.builder().name("Winter Coat").number("S-D-WIN-1").build(), agent);

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
        String patternId = createPattern(
                SamplePattern.builder().name("Summer Dress").number("S-D-SUM-1").description(null).build(),
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
                SamplePattern.builder().name("Next Pattern").number("S-D-NXT-1").description("").build(),
                agent
        );

        // then: the pattern is created
        patterns = getPatterns(agent);
        assertThat(patterns).hasSize(2);

        pattern = getPattern(patternId, agent);
        assertThat(pattern.getDescription()).isEmpty();
    }

    @Test
    void shouldNotCreatePatternNumberWhenTheNumberIsAlreadyInUse() {
        // given: a user is allowed to create patterns
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates a pattern
        String patternId = createPattern(SamplePattern.builder().number("S-D-SUM-1").build(), agent);

        // when: the user tries to create a pattern with the same number; then: an error is raised
        assertThatThrownBy(() -> createPattern(
                SamplePattern.builder().number("S-D-SUM-1").build(),
                agent
        )).matches(e -> {
            if (e.getCause() instanceof NumberAlreadyInUseError numberAlreadyInUseError) {
                return numberAlreadyInUseError.getConflictingPatternId().equals(PatternId.of(patternId))
                        && numberAlreadyInUseError.getNumber().equals(PatternNumber.of("S-D-SUM-1"));
            }

            return false;
        });
    }

    @Test
    void shouldNotCreatePatternWhenAliasIsAlreadyInUse() {
        // given: a user is allowed to create patterns
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates a pattern
        String patternId = createPattern(
                SamplePattern.builder()
                        .name("Summer Dress")
                        .number("S-D-SUM-1")
                        .build(),
                agent
        );

        // when: another pattern with the same name (and thus alias) is created; then: an error is raised
        assertThatThrownBy(() -> createPattern(
                SamplePattern.builder()
                        .name("Summer Dress")
                        .number("S-D-SUM-2")
                        .build(),
                agent
        )).matches(e -> e.getCause() instanceof AliasAlreadyInUseError
                && ((AliasAlreadyInUseError) e.getCause()).getConflictingPatternId().equals(PatternId.of(patternId))
                && ((AliasAlreadyInUseError) e.getCause()).getAlias().equals(PatternAlias.of("summer-dress")));

        // when: a pattern with a different name that slugifies to the same alias is created; then: an error is raised
        assertThatThrownBy(() -> createPattern(
                SamplePattern.builder()
                        .name("Summer-Dress")
                        .number("S-D-SUM-3")
                        .build(),
                agent
        )).matches(e -> e.getCause() instanceof AliasAlreadyInUseError);

        // when: a pattern with a different name is created; then: no error is raised
        createPattern(
                SamplePattern.builder()
                        .name("Winter Coat")
                        .number("S-C-WIN-1")
                        .build(),
                agent
        );

        // then: two patterns exist
        var patterns = getPatterns(agent);
        assertThat(patterns).hasSize(2);
    }

}
