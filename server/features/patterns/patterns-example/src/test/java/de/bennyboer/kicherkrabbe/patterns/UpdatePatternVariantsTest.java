package de.bennyboer.kicherkrabbe.patterns;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.money.Money;
import de.bennyboer.kicherkrabbe.patterns.samples.SampleMoney;
import de.bennyboer.kicherkrabbe.patterns.samples.SamplePatternVariant;
import de.bennyboer.kicherkrabbe.patterns.samples.SamplePricedSizeRange;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UpdatePatternVariantsTest extends PatternsModuleTest {

    @Test
    void shouldUpdatePatternVariantsAsUser() {
        // given: a user is allowed to create patterns
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates a pattern
        String patternId = createSamplePattern(agent);

        // when: the user updates the variants of the pattern
        var newVariant = SamplePatternVariant.builder()
                .name("New")
                .pricedSizeRange(SamplePricedSizeRange.builder()
                        .from(92)
                        .to(98L)
                        .price(SampleMoney.builder().amount(1200).build())
                        .build())
                .build()
                .toDTO();

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
        // when: a user that is not allowed to update pattern variants tries to update pattern variants; then: an error
        // is raised
        assertThatThrownBy(() -> updatePatternVariants(
                "PATTERN_ID",
                0L,
                List.of(SamplePatternVariant.builder().build().toDTO()),
                Agent.user(AgentId.of("USER_ID"))
        )).matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldNotUpdatePatternVariantsGivenAnOutdatedVersion() {
        // given: a user that is allowed to create patterns
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates a pattern
        String patternId = createSamplePattern(agent);

        // and: the pattern is published
        publishPattern(patternId, 0L, agent);

        // when: the user tries to update the pattern variants with an outdated version
        assertThatThrownBy(() -> updatePatternVariants(
                patternId,
                0L,
                List.of(SamplePatternVariant.builder().build().toDTO()),
                agent
        )).matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

}
