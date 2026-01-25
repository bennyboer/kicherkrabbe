package de.bennyboer.kicherkrabbe.patterns;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.money.Money;
import de.bennyboer.kicherkrabbe.patterns.samples.SampleMoney;
import de.bennyboer.kicherkrabbe.patterns.samples.SamplePatternExtra;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UpdatePatternExtrasTest extends PatternsModuleTest {

    @Test
    void shouldUpdatePatternExtrasAsUser() {
        // given: a user is allowed to create patterns
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates a pattern
        String patternId = createSamplePattern(agent);

        // when: the user updates the extras of the pattern
        var newExtra = SamplePatternExtra.builder()
                .name("New")
                .price(SampleMoney.builder().amount(300).build())
                .build()
                .toDTO();
        var existingExtra = SamplePatternExtra.builder().build().toDTO();

        updatePatternExtras(patternId, 0L, List.of(newExtra, existingExtra), agent);

        // then: the pattern has the updated extras
        var patterns = getPatterns(agent);
        assertThat(patterns).hasSize(1);
        var pattern = patterns.getFirst();
        assertThat(pattern.getId()).isEqualTo(PatternId.of(patternId));
        assertThat(pattern.getVersion()).isEqualTo(Version.of(1));
        assertThat(pattern.getExtras()).hasSize(2);
        var extra1 = pattern.getExtras().getFirst();
        assertThat(extra1.getName()).isEqualTo(PatternExtraName.of("New"));
        assertThat(extra1.getPrice()).isEqualTo(Money.euro(300));
        var extra2 = pattern.getExtras().getLast();
        assertThat(extra2.getName()).isEqualTo(PatternExtraName.of("Extra"));
        assertThat(extra2.getPrice()).isEqualTo(Money.euro(200));
    }

    @Test
    void shouldNotUpdatePatternExtrasWhenUserIsNotAllowed() {
        // when: a user that is not allowed to update pattern extras tries to update pattern extras; then: an error
        // is raised
        assertThatThrownBy(() -> updatePatternExtras(
                "PATTERN_ID",
                0L,
                List.of(SamplePatternExtra.builder().build().toDTO()),
                Agent.user(AgentId.of("USER_ID"))
        )).matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldNotUpdatePatternExtrasGivenAnOutdatedVersion() {
        // given: a user that is allowed to create patterns
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates a pattern
        String patternId = createSamplePattern(agent);

        // and: the pattern is published
        publishPattern(patternId, 0L, agent);

        // when: the user tries to update the pattern extras with an outdated version
        assertThatThrownBy(() -> updatePatternExtras(
                patternId,
                0L,
                List.of(SamplePatternExtra.builder().build().toDTO()),
                agent
        )).matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

}
