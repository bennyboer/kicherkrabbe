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

public class UpdatePatternNumberTest extends PatternsModuleTest {

    @Test
    void shouldUpdatePatternNumberAsUser() {
        // given: a user is allowed to create patterns
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: some categories are available
        markCategoryAsAvailable("DRESS_ID", "Dress");
        markCategoryAsAvailable("TROUSERS_ID", "Trousers");

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
                "S-D-SUM-1",
                null,
                new PatternAttributionDTO(),
                Set.of("DRESS_ID"),
                List.of("IMAGE_ID"),
                List.of(variant),
                List.of(),
                agent
        );

        // when: the user updates the pattern number
        updatePatternNumber(patternId, 0L, "S-D-SUM-2", agent);

        // then: the pattern number is updated
        var patterns = getPatterns(agent);
        assertThat(patterns).hasSize(1);
        var pattern = patterns.getFirst();
        assertThat(pattern.getId()).isEqualTo(PatternId.of(patternId));
        assertThat(pattern.getVersion()).isEqualTo(Version.of(1));
        assertThat(pattern.getNumber()).isEqualTo(PatternNumber.of("S-D-SUM-2"));
    }

    @Test
    void shouldNotBeAbleToUpdatePatternNumberGivenAnInvalidName() {
        String invalidNumber1 = "";
        String invalidNumber2 = null;
        String invalidNumber3 = "P-383";

        // given: a user is allowed to create patterns
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: some categories are available
        markCategoryAsAvailable("SKIRT_ID", "Skirt");
        markCategoryAsAvailable("TROUSERS_ID", "Trousers");

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
                "S-D-SUM-1",
                null,
                new PatternAttributionDTO(),
                Set.of("SKIRT_ID"),
                List.of("IMAGE_ID"),
                List.of(variant),
                List.of(),
                agent
        );

        // when: the user tries to update the pattern with an invalid number; then: an error is raised
        assertThatThrownBy(() -> updatePatternNumber(
                patternId,
                0L,
                invalidNumber1,
                agent
        )).isInstanceOf(IllegalArgumentException.class);

        // when: the user tries to update the pattern with an invalid number; then: an error is raised
        assertThatThrownBy(() -> updatePatternNumber(
                patternId,
                0L,
                invalidNumber2,
                agent
        )).isInstanceOf(IllegalArgumentException.class);

        // when: the user tries to update the pattern with an invalid number; then: an error is raised
        assertThatThrownBy(() -> updatePatternNumber(
                patternId,
                0L,
                invalidNumber3,
                agent
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldNotUpdatePatternNumberWhenUserIsNotAllowed() {
        // when: a user that is not allowed to update a patterns number tries to update a pattern number; then: an
        // error is raised
        assertThatThrownBy(() -> updatePatternNumber(
                "PATTERN_ID",
                0L,
                "S-D-SUM-2",
                Agent.user(AgentId.of("USER_ID"))
        )).matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldNotUpdatePatternNumberGivenAnOutdatedVersion() {
        // given: a user that is allowed to create patterns
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: some categories are available
        markCategoryAsAvailable("SKIRT_ID", "Skirt");
        markCategoryAsAvailable("TROUSERS_ID", "Trousers");

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
                "S-D-SUM-1",
                null,
                new PatternAttributionDTO(),
                Set.of("SKIRT_ID"),
                List.of("IMAGE_ID"),
                List.of(variant),
                List.of(),
                agent
        );

        // and: the pattern number is updated
        updatePatternNumber(patternId, 0L, "S-D-SUM-2", agent);

        // when: the user tries to update the pattern number with an outdated version
        assertThatThrownBy(() -> updatePatternNumber(
                patternId,
                0L,
                "S-D-SUM-3",
                agent
        )).matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldNotUpdatePatternNumberWhenTheNumberIsAlreadyInUse() {
        // given: a user that is allowed to create patterns
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: some categories are available
        markCategoryAsAvailable("SKIRT_ID", "Skirt");
        markCategoryAsAvailable("TROUSERS_ID", "Trousers");

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

        String patternId1 = createPattern(
                "Summerdress 1",
                "S-D-SUM-1",
                null,
                new PatternAttributionDTO(),
                Set.of("SKIRT_ID"),
                List.of("IMAGE_ID"),
                List.of(variant),
                List.of(),
                agent
        );

        // and: the user creates another pattern
        String patternId2 = createPattern(
                "Summerdress 2",
                "S-D-SUM-2",
                null,
                new PatternAttributionDTO(),
                Set.of("SKIRT_ID"),
                List.of("IMAGE_ID"),
                List.of(variant),
                List.of(),
                agent
        );

        // when: the user tries to update the pattern number with a number that is already in use
        assertThatThrownBy(() -> updatePatternNumber(
                patternId1,
                0L,
                "S-D-SUM-2",
                agent
        )).matches(e -> {
            if (e.getCause() instanceof NumberAlreadyInUseError numberAlreadyInUseError) {
                return numberAlreadyInUseError.getConflictingPatternId().equals(PatternId.of(patternId2))
                        && numberAlreadyInUseError.getNumber().equals(PatternNumber.of("S-D-SUM-2"));
            }

            return false;
        });
    }

}
