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

public class UpdatePatternAttributionTest extends PatternsModuleTest {

    @Test
    void shouldUpdatePatternAttributionAsUser() {
        // given: a user is allowed to create patterns
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: some categories are available
        markCategoryAsAvailable("DRESS_ID", "Dress");

        // and: the user creates a pattern
        var attribution = new PatternAttributionDTO();

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
                attribution,
                Set.of("DRESS_ID"),
                List.of("IMAGE_ID"),
                List.of(variant),
                List.of(),
                agent
        );

        // when: the user updates the attribution of the pattern
        var newAttribution = new PatternAttributionDTO();
        newAttribution.originalPatternName = "Summerdress EXTREME";
        newAttribution.designer = "EXTREME PATTERNS inc.";
        updatePatternAttribution(patternId, 0L, newAttribution, agent);

        // then: the pattern has the new attribution
        var patterns = getPatterns(agent);
        assertThat(patterns).hasSize(1);
        var pattern = patterns.getFirst();
        assertThat(pattern.getId()).isEqualTo(PatternId.of(patternId));
        assertThat(pattern.getVersion()).isEqualTo(Version.of(1));
        assertThat(pattern.getAttribution()).isEqualTo(PatternAttribution.of(
                OriginalPatternName.of("Summerdress EXTREME"),
                PatternDesigner.of("EXTREME PATTERNS inc.")
        ));
    }

    @Test
    void shouldNotUpdatePatternAttributionWhenUserIsNotAllowed() {
        // when: a user that is not allowed to update pattern attribution tries to update pattern attribution; then:
        // an error
        // is raised
        assertThatThrownBy(() -> updatePatternAttribution(
                "PATTERN_ID",
                0L,
                new PatternAttributionDTO(),
                Agent.user(AgentId.of("USER_ID"))
        )).matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldNotUpdatePatternAttributionGivenAnOutdatedVersion() {
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
                new PatternAttributionDTO(),
                Set.of("DRESS_ID"),
                List.of("IMAGE_ID"),
                List.of(variant),
                List.of(),
                agent
        );

        // and: the pattern is published
        publishPattern(patternId, 0L, agent);

        // when: the user tries to update the pattern attribution with an outdated version
        assertThatThrownBy(() -> updatePatternAttribution(
                patternId,
                0L,
                new PatternAttributionDTO(),
                agent
        )).matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

}
