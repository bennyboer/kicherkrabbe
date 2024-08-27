package de.bennyboer.kicherkrabbe.patterns;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.money.Money;
import de.bennyboer.kicherkrabbe.patterns.http.api.MoneyDTO;
import de.bennyboer.kicherkrabbe.patterns.http.api.PatternAttributionDTO;
import de.bennyboer.kicherkrabbe.patterns.http.api.PatternVariantDTO;
import de.bennyboer.kicherkrabbe.patterns.http.api.PricedSizeRangeDTO;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UpdatePatternVariantsTest extends PatternsModuleTest {

    @Test
    void shouldUpdatePatternVariantsAsUser() {
        // given: a user is allowed to create patterns
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: some categories are available
        markCategoryAsAvailable("DRESS_ID", "Dress");

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

        // when: the user updates the variants of the pattern
        var newVariant = new PatternVariantDTO();
        newVariant.name = "New";
        var newPricedSizeRange = new PricedSizeRangeDTO();
        newPricedSizeRange.from = 92;
        newPricedSizeRange.to = 98L;
        newPricedSizeRange.price = new MoneyDTO();
        newPricedSizeRange.price.amount = 1200;
        newPricedSizeRange.price.currency = "EUR";
        newVariant.pricedSizeRanges = Set.of(newPricedSizeRange);

        updatePatternVariants(patternId, 0L, List.of(newVariant), agent);

        // then: the pattern has the new variant
        var patterns = getPatterns(agent);
        assertThat(patterns).hasSize(1);
        var pattern = patterns.getFirst();
        assertThat(pattern.getId()).isEqualTo(PatternId.of(patternId));
        assertThat(pattern.getVersion()).isEqualTo(Version.of(1));
        assertThat(pattern.getVariants()).hasSize(1);
        var newPatternVariant = pattern.getVariants().get(0);
        assertThat(newPatternVariant.getName()).isEqualTo(PatternVariantName.of("New"));
        assertThat(newPatternVariant.getPricedSizeRanges()).hasSize(1);
        var newPatternVariantPricedSizeRange = newPatternVariant.getPricedSizeRanges().iterator().next();
        assertThat(newPatternVariantPricedSizeRange.getFrom()).isEqualTo(92);
        assertThat(newPatternVariantPricedSizeRange.getTo()).isEqualTo(Optional.of(98L));
        assertThat(newPatternVariantPricedSizeRange.getPrice()).isEqualTo(Money.euro(1200));
    }

    @Test
    void shouldNotUpdatePatternVariantsWhenUserIsNotAllowed() {
        var newVariant = new PatternVariantDTO();
        newVariant.name = "New";
        var newPricedSizeRange = new PricedSizeRangeDTO();
        newPricedSizeRange.from = 92;
        newPricedSizeRange.to = 98L;
        newPricedSizeRange.price = new MoneyDTO();
        newPricedSizeRange.price.amount = 1200;
        newPricedSizeRange.price.currency = "EUR";
        newVariant.pricedSizeRanges = Set.of(newPricedSizeRange);

        // when: a user that is not allowed to update pattern variants tries to update pattern variants; then: an error
        // is raised
        assertThatThrownBy(() -> updatePatternVariants(
                "PATTERN_ID",
                0L,
                List.of(newVariant),
                Agent.user(AgentId.of("USER_ID"))
        )).matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldNotUpdatePatternVariantsGivenAnOutdatedVersion() {
        // given: a user that is allowed to create patterns
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: some categories are available
        markCategoryAsAvailable("DRESS_ID", "Dress");

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

        // when: the user tries to update the pattern variants with an outdated version
        var newVariant = new PatternVariantDTO();
        newVariant.name = "New";
        var newPricedSizeRange = new PricedSizeRangeDTO();
        newPricedSizeRange.from = 92;
        newPricedSizeRange.to = 98L;
        newPricedSizeRange.price = new MoneyDTO();
        newPricedSizeRange.price.amount = 1200;
        newPricedSizeRange.price.currency = "EUR";
        newVariant.pricedSizeRanges = Set.of(newPricedSizeRange);

        assertThatThrownBy(() -> updatePatternVariants(
                patternId,
                0L,
                List.of(newVariant),
                agent
        )).matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

}
