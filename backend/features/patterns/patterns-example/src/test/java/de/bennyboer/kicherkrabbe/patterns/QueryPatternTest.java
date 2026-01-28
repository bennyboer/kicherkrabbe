package de.bennyboer.kicherkrabbe.patterns;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.money.Money;
import de.bennyboer.kicherkrabbe.patterns.samples.SamplePatternAttribution;
import de.bennyboer.kicherkrabbe.patterns.samples.SamplePatternVariant;
import de.bennyboer.kicherkrabbe.patterns.samples.SamplePricedSizeRange;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class QueryPatternTest extends PatternsModuleTest {

    @Test
    void shouldQueryPatternAsUser() {
        // given: a user is allowed to create patterns
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: some categories are available
        markCategoryAsAvailable("DRESS_ID", "Dress");

        // and: the user creates a pattern
        String patternId = createPattern(
                "Summerdress",
                "S-D-SUM-1",
                null,
                SamplePatternAttribution.builder().designer("Designer").build().toDTO(),
                Set.of("DRESS_ID"),
                List.of("IMAGE_ID"),
                List.of(SamplePatternVariant.builder()
                        .pricedSizeRange(SamplePricedSizeRange.builder().build())
                        .build()
                        .toDTO()),
                List.of(),
                agent
        );

        // when: the user queries the pattern
        var pattern = getPattern(patternId, agent);

        // then: the pattern is returned
        assertThat(pattern.getId()).isEqualTo(PatternId.of(patternId));
        assertThat(pattern.getVersion()).isEqualTo(Version.zero());
        assertThat(pattern.isPublished()).isFalse();
        assertThat(pattern.getName()).isEqualTo(PatternName.of("Summerdress"));
        assertThat(pattern.getCategories()).containsExactlyInAnyOrder(
                PatternCategoryId.of("DRESS_ID")
        );
        assertThat(pattern.getImages()).containsExactlyInAnyOrder(
                ImageId.of("IMAGE_ID")
        );
        assertThat(pattern.getVariants()).hasSize(1);
        var queriedVariant = pattern.getVariants().get(0);
        assertThat(queriedVariant.getName()).isEqualTo(PatternVariantName.of("Normal"));
        assertThat(queriedVariant.getPricedSizeRanges()).hasSize(1);
        var queriedPricedSizeRange = queriedVariant.getPricedSizeRanges().iterator().next();
        assertThat(queriedPricedSizeRange.getFrom()).isEqualTo(80);
        assertThat(queriedPricedSizeRange.getTo()).isEqualTo(Optional.of(86L));
        assertThat(queriedPricedSizeRange.getPrice()).isEqualTo(Money.euro(1000));
        assertThat(pattern.getAttribution()).isEqualTo(PatternAttribution.of(null, PatternDesigner.of("Designer")));
        assertThat(pattern.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldNotQueryPatternWhenUserIsNotAllowed() {
        // when: a user that is not allowed to query a pattern tries to query a pattern; then: an error is raised
        assertThatThrownBy(() -> getPattern(
                "PATTERN_ID",
                Agent.user(AgentId.of("USER_ID"))
        )).matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldRaiseNotFoundErrorWhenPatternDoesNotExist() {
        // given: a user has permissions to read a pattern that does not exist anymore
        allowUserToReadPattern("USER_ID", "PATTERN_ID");

        // when: querying a pattern that does not exist; then: an error is raised
        assertThatThrownBy(() -> getPattern(
                "PATTERN_ID",
                Agent.user(AgentId.of("USER_ID"))
        )).matches(e -> e.getCause() instanceof PatternNotFoundError);
    }

}
