package de.bennyboer.kicherkrabbe.patterns;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.money.Money;
import de.bennyboer.kicherkrabbe.patterns.http.api.MoneyDTO;
import de.bennyboer.kicherkrabbe.patterns.http.api.PatternAttributionDTO;
import de.bennyboer.kicherkrabbe.patterns.http.api.PatternVariantDTO;
import de.bennyboer.kicherkrabbe.patterns.http.api.PricedSizeRangeDTO;
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

        // when: the user queries the pattern as a user
        var pattern = getPublishedPattern(patternId, agent);

        // then: the published pattern is returned
        assertThat(pattern.getId()).isEqualTo(PatternId.of(patternId));
        assertThat(pattern.getName()).isEqualTo(PatternName.of("Summerdress"));
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

        // when: querying the pattern as an anonymous user
        var pattern = getPublishedPattern(patternId, Agent.anonymous());

        // then: the published pattern is returned
        assertThat(pattern.getId()).isEqualTo(PatternId.of(patternId));
        assertThat(pattern.getName()).isEqualTo(PatternName.of("Summerdress"));
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

        // when: querying the pattern as a system user
        var pattern = getPublishedPattern(patternId, Agent.system());

        // then: the published pattern is returned
        assertThat(pattern.getId()).isEqualTo(PatternId.of(patternId));
        assertThat(pattern.getName()).isEqualTo(PatternName.of("Summerdress"));
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

        // and: the pattern is unpublished again
        unpublishPattern(patternId, 1L, agent);

        // when: querying the pattern as an anonymous user
        var pattern = getPublishedPattern(patternId, Agent.anonymous());

        // then: the published pattern is null
        assertThat(pattern).isNull();
    }

}
