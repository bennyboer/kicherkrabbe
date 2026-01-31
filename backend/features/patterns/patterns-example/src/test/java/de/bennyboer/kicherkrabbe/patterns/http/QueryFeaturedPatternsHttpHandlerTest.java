package de.bennyboer.kicherkrabbe.patterns.http;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.money.Money;
import de.bennyboer.kicherkrabbe.patterns.*;
import de.bennyboer.kicherkrabbe.patterns.http.api.responses.QueryFeaturedPatternsResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class QueryFeaturedPatternsHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyQueryFeaturedPatterns() {
        var token = createTokenForUser("USER_ID");

        when(module.getFeaturedPatterns(Agent.user(AgentId.of("USER_ID")))).thenReturn(Flux.just(
                PublishedPattern.of(
                        PatternId.of("PATTERN_ID"),
                        PatternName.of("Summerdress"),
                        PatternNumber.of("S-D-SUM-1"),
                        PatternDescription.of("A beautiful summer dress"),
                        PatternAlias.of("summerdress"),
                        PatternAttribution.of(
                                OriginalPatternName.of("Summerdress EXTREME"),
                                PatternDesigner.of("EXTREME PATTERNS inc.")
                        ),
                        Set.of(PatternCategoryId.of("DRESS_ID")),
                        List.of(ImageId.of("IMAGE_ID")),
                        List.of(PatternVariant.of(
                                PatternVariantName.of("Normal"),
                                Set.of(PricedSizeRange.of(
                                        80,
                                        86L,
                                        null,
                                        Money.euro(2000)
                                ))
                        )),
                        List.of(PatternExtra.of(
                                PatternExtraName.of("Sewing instructions"),
                                Money.euro(200)
                        ))
                )
        ));

        var exchange = client.get()
                .uri("/api/patterns/featured")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        exchange.expectStatus().isOk();

        var response = exchange.expectBody(QueryFeaturedPatternsResponse.class).returnResult().getResponseBody();
        assertThat(response.patterns).hasSize(1);

        var pattern = response.patterns.get(0);
        assertThat(pattern.id).isEqualTo("PATTERN_ID");
        assertThat(pattern.name).isEqualTo("Summerdress");
        assertThat(pattern.number).isEqualTo("S-D-SUM-1");
        assertThat(pattern.description).isEqualTo("A beautiful summer dress");
        assertThat(pattern.alias).isEqualTo("summerdress");
        assertThat(pattern.attribution.originalPatternName).isEqualTo("Summerdress EXTREME");
        assertThat(pattern.attribution.designer).isEqualTo("EXTREME PATTERNS inc.");
        assertThat(pattern.categories).containsExactly("DRESS_ID");
        assertThat(pattern.images).containsExactly("IMAGE_ID");
        assertThat(pattern.variants).hasSize(1);

        var variant = pattern.variants.get(0);
        assertThat(variant.name).isEqualTo("Normal");
        assertThat(variant.pricedSizeRanges).hasSize(1);

        var pricedSizeRange = variant.pricedSizeRanges.stream().findFirst().get();
        assertThat(pricedSizeRange.from).isEqualTo(80);
        assertThat(pricedSizeRange.to).isEqualTo(86);
        assertThat(pricedSizeRange.price.amount).isEqualTo(2000);
        assertThat(pricedSizeRange.price.currency).isEqualTo("EUR");

        assertThat(pattern.extras).hasSize(1);
        var extra = pattern.extras.get(0);
        assertThat(extra.name).isEqualTo("Sewing instructions");
        assertThat(extra.price.amount).isEqualTo(200);
        assertThat(extra.price.currency).isEqualTo("EUR");
    }

    @Test
    void shouldAllowUnauthorizedAccess() {
        when(module.getFeaturedPatterns(Agent.anonymous())).thenReturn(Flux.just(
                PublishedPattern.of(
                        PatternId.of("PATTERN_ID"),
                        PatternName.of("Summerdress"),
                        PatternNumber.of("S-D-SUM-1"),
                        null,
                        PatternAlias.of("summerdress"),
                        PatternAttribution.of(
                                OriginalPatternName.of("Summerdress EXTREME"),
                                PatternDesigner.of("EXTREME PATTERNS inc.")
                        ),
                        Set.of(PatternCategoryId.of("DRESS_ID")),
                        List.of(ImageId.of("IMAGE_ID")),
                        List.of(PatternVariant.of(
                                PatternVariantName.of("Normal"),
                                Set.of(PricedSizeRange.of(
                                        80,
                                        86L,
                                        null,
                                        Money.euro(2000)
                                ))
                        )),
                        List.of()
                )
        ));

        var exchange = client.get()
                .uri("/api/patterns/featured")
                .exchange();

        exchange.expectStatus().isOk();

        var response = exchange.expectBody(QueryFeaturedPatternsResponse.class).returnResult().getResponseBody();
        assertThat(response.patterns).hasSize(1);

        var pattern = response.patterns.get(0);
        assertThat(pattern.id).isEqualTo("PATTERN_ID");
        assertThat(pattern.name).isEqualTo("Summerdress");
        assertThat(pattern.alias).isEqualTo("summerdress");
    }

    @Test
    void shouldNotAllowAccessWithInvalidToken() {
        var exchange = client.get()
                .uri("/api/patterns/featured")
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        exchange.expectStatus().isUnauthorized();
    }

}
