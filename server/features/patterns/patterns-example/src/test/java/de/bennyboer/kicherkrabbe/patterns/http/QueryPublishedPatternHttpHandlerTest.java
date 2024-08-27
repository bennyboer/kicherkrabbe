package de.bennyboer.kicherkrabbe.patterns.http;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.money.Money;
import de.bennyboer.kicherkrabbe.patterns.*;
import de.bennyboer.kicherkrabbe.patterns.http.api.responses.QueryPublishedPatternResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class QueryPublishedPatternHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyQueryPublishedPattern() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a successful response
        when(module.getPublishedPattern(
                "PATTERN_ID",
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.just(PublishedPattern.of(
                PatternId.of("PATTERN_ID"),
                PatternName.of("Summerdress"),
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
        )));

        // when: posting the request
        var exchange = client.get()
                .uri("/api/patterns/PATTERN_ID/published")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the published pattern
        var response = exchange.expectBody(QueryPublishedPatternResponse.class).returnResult().getResponseBody();
        assertThat(response.pattern.id).isEqualTo("PATTERN_ID");
        assertThat(response.pattern.name).isEqualTo("Summerdress");
        assertThat(response.pattern.description).isEqualTo("A beautiful summer dress");
        assertThat(response.pattern.alias).isEqualTo("summerdress");
        assertThat(response.pattern.attribution.originalPatternName).isEqualTo("Summerdress EXTREME");
        assertThat(response.pattern.attribution.designer).isEqualTo("EXTREME PATTERNS inc.");
        assertThat(response.pattern.categories).containsExactly("DRESS_ID");
        assertThat(response.pattern.images).containsExactly("IMAGE_ID");
        assertThat(response.pattern.variants).hasSize(1);

        var variant = response.pattern.variants.get(0);
        assertThat(variant.name).isEqualTo("Normal");
        assertThat(variant.pricedSizeRanges).hasSize(1);

        var pricedSizeRange = variant.pricedSizeRanges.stream().findFirst().get();
        assertThat(pricedSizeRange.from).isEqualTo(80);
        assertThat(pricedSizeRange.to).isEqualTo(86);
        assertThat(pricedSizeRange.price.amount).isEqualTo(2000);
        assertThat(pricedSizeRange.price.currency).isEqualTo("EUR");

        assertThat(response.pattern.extras).hasSize(1);
        var extra = response.pattern.extras.get(0);
        assertThat(extra.name).isEqualTo("Sewing instructions");
        assertThat(extra.price.amount).isEqualTo(200);
        assertThat(extra.price.currency).isEqualTo("EUR");
    }

    @Test
    void shouldAllowUnauthorizedAccess() {
        // given: the module is configured to return a successful response
        when(module.getPublishedPattern(
                "PATTERN_ID",
                Agent.anonymous()
        )).thenReturn(Mono.just(PublishedPattern.of(
                PatternId.of("PATTERN_ID"),
                PatternName.of("Summerdress"),
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
                List.of(PatternExtra.of(
                        PatternExtraName.of("Sewing instructions"),
                        Money.euro(200)
                ))
        )));

        // when: posting the request without a token
        var exchange = client.get()
                .uri("/api/patterns/PATTERN_ID/published")
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the published pattern
        var response = exchange.expectBody(QueryPublishedPatternResponse.class).returnResult().getResponseBody();
        assertThat(response.pattern.id).isEqualTo("PATTERN_ID");
        assertThat(response.pattern.name).isEqualTo("Summerdress");
        assertThat(response.pattern.alias).isEqualTo("summerdress");
        assertThat(response.pattern.attribution.originalPatternName).isEqualTo("Summerdress EXTREME");
        assertThat(response.pattern.attribution.designer).isEqualTo("EXTREME PATTERNS inc.");
        assertThat(response.pattern.categories).containsExactly("DRESS_ID");
        assertThat(response.pattern.images).containsExactly("IMAGE_ID");
        assertThat(response.pattern.variants).hasSize(1);

        var variant = response.pattern.variants.get(0);
        assertThat(variant.name).isEqualTo("Normal");
        assertThat(variant.pricedSizeRanges).hasSize(1);

        var pricedSizeRange = variant.pricedSizeRanges.stream().findFirst().get();
        assertThat(pricedSizeRange.from).isEqualTo(80);
        assertThat(pricedSizeRange.to).isEqualTo(86);
        assertThat(pricedSizeRange.price.amount).isEqualTo(2000);
        assertThat(pricedSizeRange.price.currency).isEqualTo("EUR");

        assertThat(response.pattern.extras).hasSize(1);
        var extra = response.pattern.extras.get(0);
        assertThat(extra.name).isEqualTo("Sewing instructions");
        assertThat(extra.price.amount).isEqualTo(200);
        assertThat(extra.price.currency).isEqualTo("EUR");
    }

    @Test
    void shouldNotAllowAccessWithInvalidToken() {
        // when: posting the request with an invalid token
        var exchange = client.get()
                .uri("/api/patterns/PATTERN_ID/published")
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldReturnWith404WhenPublishedPatternNotFound() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return an empty response
        when(module.getPublishedPattern(
                "PATTERN_ID",
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.empty());

        // when: posting the request
        var exchange = client.get()
                .uri("/api/patterns/PATTERN_ID/published")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is not found
        exchange.expectStatus().isNotFound();
    }

}
