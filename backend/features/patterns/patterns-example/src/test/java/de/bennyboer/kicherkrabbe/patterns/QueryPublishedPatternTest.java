package de.bennyboer.kicherkrabbe.patterns;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.money.Money;
import de.bennyboer.kicherkrabbe.patterns.samples.SamplePatternAttribution;
import de.bennyboer.kicherkrabbe.patterns.samples.SamplePatternVariant;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class QueryPublishedPatternTest extends PatternsModuleTest {

    @Test
    void shouldQueryPublishedPatternAsUser() {
        // given: a user is allowed to create patterns
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: some categories are available
        markCategoryAsAvailable("DRESS_ID", "Dress");
        markCategoryAsAvailable("SKIRT_ID", "Skirt");

        // and: the user creates a pattern
        String patternId = createPattern(
                "Summerdress",
                "S-D-SUM-1",
                "A beautiful summer dress",
                SamplePatternAttribution.builder().build().toDTO(),
                Set.of("DRESS_ID"),
                List.of("IMAGE_ID"),
                List.of(SamplePatternVariant.builder().build().toDTO()),
                List.of(),
                agent
        );

        // and: the pattern is published
        publishPattern(patternId, 0L, agent);

        // when: the user queries the pattern as a user
        var pattern = getPublishedPattern(patternId, agent);

        // then: the published pattern is returned
        assertThat(pattern.getId()).isEqualTo(PatternId.of(patternId));
        assertThat(pattern.getName()).isEqualTo(PatternName.of("Summerdress"));
        assertThat(pattern.getDescription()).contains(PatternDescription.of("A beautiful summer dress"));
        assertThat(pattern.getAlias()).isEqualTo(PatternAlias.of("summerdress"));
        assertThat(pattern.getAttribution()).isEqualTo(PatternAttribution.of(null, null));
        assertThat(pattern.getCategories()).containsExactly(PatternCategoryId.of("DRESS_ID"));
        assertThat(pattern.getImages()).containsExactly(ImageId.of("IMAGE_ID"));
        assertThat(pattern.getVariants()).containsExactlyInAnyOrder(
                PatternVariant.of(
                        PatternVariantName.of("Normal"),
                        Set.of(
                                PricedSizeRange.of(
                                        80,
                                        86L,
                                        null,
                                        Money.euro(1000)
                                )
                        )
                )
        );
        assertThat(pattern.getExtras()).isEmpty();
    }

    @Test
    void shouldQueryPublishedPatternAsAnonymousUser() {
        // given: a user is allowed to create patterns
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: some categories are available
        markCategoryAsAvailable("DRESS_ID", "Dress");
        markCategoryAsAvailable("SKIRT_ID", "Skirt");

        // and: the user creates a pattern
        String patternId = createPattern(
                "Summerdress",
                "S-D-SUM-1",
                null,
                SamplePatternAttribution.builder().build().toDTO(),
                Set.of("DRESS_ID"),
                List.of("IMAGE_ID"),
                List.of(SamplePatternVariant.builder().build().toDTO()),
                List.of(),
                agent
        );

        // and: the pattern is published
        publishPattern(patternId, 0L, agent);

        // when: querying the pattern as an anonymous user
        var pattern = getPublishedPattern(patternId, Agent.anonymous());

        // then: the published pattern is returned
        assertThat(pattern.getId()).isEqualTo(PatternId.of(patternId));
        assertThat(pattern.getName()).isEqualTo(PatternName.of("Summerdress"));
        assertThat(pattern.getDescription()).isEmpty();
        assertThat(pattern.getAlias()).isEqualTo(PatternAlias.of("summerdress"));
        assertThat(pattern.getAttribution()).isEqualTo(PatternAttribution.of(null, null));
        assertThat(pattern.getCategories()).containsExactly(PatternCategoryId.of("DRESS_ID"));
        assertThat(pattern.getImages()).containsExactly(ImageId.of("IMAGE_ID"));
        assertThat(pattern.getVariants()).containsExactlyInAnyOrder(
                PatternVariant.of(
                        PatternVariantName.of("Normal"),
                        Set.of(
                                PricedSizeRange.of(
                                        80,
                                        86L,
                                        null,
                                        Money.euro(1000)
                                )
                        )
                )
        );
        assertThat(pattern.getExtras()).isEmpty();
    }

    @Test
    void shouldQueryPublishedPatternAsSystemUser() {
        // given: a user is allowed to create patterns
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: some categories are available
        markCategoryAsAvailable("DRESS_ID", "Dress");
        markCategoryAsAvailable("SKIRT_ID", "Skirt");

        // and: the user creates a pattern
        String patternId = createPattern(
                "Summerdress",
                "S-D-SUM-1",
                null,
                SamplePatternAttribution.builder().build().toDTO(),
                Set.of("DRESS_ID"),
                List.of("IMAGE_ID"),
                List.of(SamplePatternVariant.builder().build().toDTO()),
                List.of(),
                agent
        );

        // and: the pattern is published
        publishPattern(patternId, 0L, agent);

        // when: querying the pattern as a system user
        var pattern = getPublishedPattern(patternId, Agent.system());

        // then: the published pattern is returned
        assertThat(pattern.getId()).isEqualTo(PatternId.of(patternId));
        assertThat(pattern.getName()).isEqualTo(PatternName.of("Summerdress"));
        assertThat(pattern.getDescription()).isEmpty();
        assertThat(pattern.getAlias()).isEqualTo(PatternAlias.of("summerdress"));
        assertThat(pattern.getAttribution()).isEqualTo(PatternAttribution.of(null, null));
        assertThat(pattern.getCategories()).containsExactly(PatternCategoryId.of("DRESS_ID"));
        assertThat(pattern.getImages()).containsExactly(ImageId.of("IMAGE_ID"));
        assertThat(pattern.getVariants()).containsExactlyInAnyOrder(
                PatternVariant.of(
                        PatternVariantName.of("Normal"),
                        Set.of(
                                PricedSizeRange.of(
                                        80,
                                        86L,
                                        null,
                                        Money.euro(1000)
                                )
                        )
                )
        );
        assertThat(pattern.getExtras()).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenPublishedPatternDoesNotExist() {
        // when: querying a pattern that does not exist
        var pattern = getPublishedPattern("UNKNOWN_PATTERN_ID", Agent.anonymous());

        // then: the published pattern is null
        assertThat(pattern).isNull();
    }

    @Test
    void shouldReturnEmptyWhenPatternIsNotPublished() {
        // given: a user is allowed to create patterns
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: some categories are available
        markCategoryAsAvailable("DRESS_ID", "Dress");
        markCategoryAsAvailable("SKIRT_ID", "Skirt");

        // and: the user creates a pattern but does not publish it
        String patternId = createPattern(
                "Summerdress",
                "S-D-SUM-1",
                null,
                SamplePatternAttribution.builder().build().toDTO(),
                Set.of("DRESS_ID"),
                List.of("IMAGE_ID"),
                List.of(SamplePatternVariant.builder().build().toDTO()),
                List.of(),
                agent
        );

        // when: querying the pattern as an anonymous user
        var pattern = getPublishedPattern(patternId, Agent.anonymous());

        // then: the published pattern is null
        assertThat(pattern).isNull();
    }

    @Test
    void shouldReturnEmptyIfThePatternIsUnpublished() {
        // given: a user is allowed to create patterns
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: some categories are available
        markCategoryAsAvailable("DRESS_ID", "Dress");
        markCategoryAsAvailable("SKIRT_ID", "Skirt");

        // and: the user creates a pattern
        String patternId = createPattern(
                "Summerdress",
                "S-D-SUM-1",
                null,
                SamplePatternAttribution.builder().build().toDTO(),
                Set.of("DRESS_ID"),
                List.of("IMAGE_ID"),
                List.of(SamplePatternVariant.builder().build().toDTO()),
                List.of(),
                agent
        );

        // and: the pattern is published
        publishPattern(patternId, 0L, agent);

        // and: the pattern is unpublished again
        unpublishPattern(patternId, 1L, agent);

        // when: querying the pattern as an anonymous user
        var pattern = getPublishedPattern(patternId, Agent.anonymous());

        // then: the published pattern is null
        assertThat(pattern).isNull();
    }

    @Test
    void shouldQueryPublishedPatternAsUserWithAlias() {
        // given: a user is allowed to create patterns
        allowUserToCreatePatterns("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: some categories are available
        markCategoryAsAvailable("DRESS_ID", "Dress");
        markCategoryAsAvailable("SKIRT_ID", "Skirt");

        // and: the user creates a pattern
        String patternId = createPattern(
                "Summerdress",
                "S-D-SUM-1",
                null,
                SamplePatternAttribution.builder().build().toDTO(),
                Set.of("DRESS_ID"),
                List.of("IMAGE_ID"),
                List.of(SamplePatternVariant.builder().build().toDTO()),
                List.of(),
                agent
        );

        // and: the pattern is published
        publishPattern(patternId, 0L, agent);

        // when: the user queries the pattern with its alias
        var pattern = getPublishedPattern("summerdress", agent);

        // then: the published pattern is returned
        assertThat(pattern.getId()).isEqualTo(PatternId.of(patternId));

        // when: the user queries the pattern with an invalid alias
        pattern = getPublishedPattern("fwejfiwefwe", agent);

        // then: the published pattern is null
        assertThat(pattern).isNull();
    }

}
