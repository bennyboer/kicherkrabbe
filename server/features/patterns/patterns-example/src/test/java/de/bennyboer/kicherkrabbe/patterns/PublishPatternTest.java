package de.bennyboer.kicherkrabbe.patterns;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.patterns.http.api.MoneyDTO;
import de.bennyboer.kicherkrabbe.patterns.http.api.PatternAttributionDTO;
import de.bennyboer.kicherkrabbe.patterns.http.api.PatternVariantDTO;
import de.bennyboer.kicherkrabbe.patterns.http.api.PricedSizeRangeDTO;
import de.bennyboer.kicherkrabbe.patterns.publish.AlreadyPublishedError;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PublishPatternTest extends PatternsModuleTest {

    @Test
    void shouldPublishPatternAsUser() {
        // given: a user is allowed to create patterns
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: some categories are available
        markCategoryAsAvailable("UNDERWEAR_ID", "Underwear");

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
                "Underwear",
                new PatternAttributionDTO(),
                Set.of("UNDERWEAR_ID"),
                List.of("IMAGE_ID"),
                List.of(variant),
                List.of(),
                agent
        );

        // when: the user publishes the pattern
        publishPattern(patternId, 0L, agent);

        // then: the pattern is published
        var patterns = getPatterns(agent);
        assertThat(patterns).hasSize(1);
        var pattern = patterns.getFirst();
        assertThat(pattern.getId()).isEqualTo(PatternId.of(patternId));
        assertThat(pattern.getVersion()).isEqualTo(Version.of(1));
        assertThat(pattern.isPublished()).isTrue();
    }

    @Test
    void shouldNotBeAbleToPublishPatternIfItIsAlreadyPublished() {
        // given: a user is allowed to create patterns
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: some categories are available
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
                "Jeans",
                new PatternAttributionDTO(),
                Set.of("TROUSERS_ID"),
                List.of("IMAGE_ID"),
                List.of(variant),
                List.of(),
                agent
        );

        // and: the pattern is published
        publishPattern(patternId, 0L, agent);

        // when: the user tries to publish the pattern again; then: an error is raised
        assertThatThrownBy(() -> publishPattern(
                patternId,
                1L,
                agent
        )).isInstanceOf(AlreadyPublishedError.class);
    }

    @Test
    void shouldNotPublishPatternWhenUserIsNotAllowed() {
        // when: a user that is not allowed to publish a pattern tries to publish a pattern; then: an error is raised
        assertThatThrownBy(() -> publishPattern(
                "PATTERN_ID",
                0L,
                Agent.user(AgentId.of("USER_ID"))
        )).matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldNotPublishPatternGivenAnOutdatedVersion() {
        // given: a user that is allowed to create patterns
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: some categories are available
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
                "Jeans",
                new PatternAttributionDTO(),
                Set.of("TROUSERS_ID"),
                List.of("IMAGE_ID"),
                List.of(variant),
                List.of(),
                agent
        );

        // and: the pattern is published
        publishPattern(patternId, 0L, agent);

        // when: the user tries to publish the pattern with an outdated version
        assertThatThrownBy(() -> publishPattern(
                patternId,
                0L,
                agent
        )).matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

}
