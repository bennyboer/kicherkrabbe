package de.bennyboer.kicherkrabbe.patterns.http;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.money.Money;
import de.bennyboer.kicherkrabbe.patterns.*;
import de.bennyboer.kicherkrabbe.patterns.http.api.PatternsSortDTO;
import de.bennyboer.kicherkrabbe.patterns.http.api.requests.QueryPublishedPatternsRequest;
import de.bennyboer.kicherkrabbe.patterns.http.api.responses.QueryPublishedPatternsResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

import static de.bennyboer.kicherkrabbe.patterns.http.api.PatternsSortDirectionDTO.ASCENDING;
import static de.bennyboer.kicherkrabbe.patterns.http.api.PatternsSortPropertyDTO.ALPHABETICAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class QueryPublishedPatternsHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyQueryPublishedPatterns() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: a request to query published patterns
        var request = new QueryPublishedPatternsRequest();
        request.searchTerm = "test";
        request.skip = 3;
        request.limit = 5;
        request.categories = Set.of("CATEGORY_ID");
        request.sort = new PatternsSortDTO();
        request.sort.property = ALPHABETICAL;
        request.sort.direction = ASCENDING;

        // and: the module is configured to return a successful response
        when(module.getPublishedPatterns(
                request.searchTerm,
                request.categories,
                request.sort,
                request.skip,
                request.limit,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.just(PublishedPatternsPage.of(
                3,
                5,
                4,
                List.of(PublishedPattern.of(
                        PatternId.of("PATTERN_ID"),
                        PatternName.of("Summerdress"),
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
                ))
        )));

        // when: posting the request
        var exchange = client.post()
                .uri("/api/patterns/published")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the published patterns
        var response = exchange.expectBody(QueryPublishedPatternsResponse.class).returnResult().getResponseBody();
        assertThat(response.skip).isEqualTo(3);
        assertThat(response.limit).isEqualTo(5);
        assertThat(response.total).isEqualTo(4);
        assertThat(response.patterns).hasSize(1);

        var pattern = response.patterns.get(0);
        assertThat(pattern.id).isEqualTo("PATTERN_ID");
        assertThat(pattern.name).isEqualTo("Summerdress");
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
        // given: a request to query published patterns
        var request = new QueryPublishedPatternsRequest();
        request.searchTerm = "test";
        request.skip = 3;
        request.limit = 5;
        request.categories = Set.of("CATEGORY_ID");
        request.sort = new PatternsSortDTO();
        request.sort.property = ALPHABETICAL;
        request.sort.direction = ASCENDING;

        // and: the module is configured to return a successful response
        when(module.getPublishedPatterns(
                request.searchTerm,
                request.categories,
                request.sort,
                request.skip,
                request.limit,
                Agent.anonymous()
        )).thenReturn(Mono.just(PublishedPatternsPage.of(
                3,
                5,
                4,
                List.of(PublishedPattern.of(
                        PatternId.of("PATTERN_ID"),
                        PatternName.of("Summerdress"),
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
                ))
        )));

        // when: posting the request without a token
        var exchange = client.post()
                .uri("/api/patterns/published")
                .bodyValue(request)
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the published patterns
        var response = exchange.expectBody(QueryPublishedPatternsResponse.class).returnResult().getResponseBody();
        assertThat(response.skip).isEqualTo(3);
        assertThat(response.limit).isEqualTo(5);
        assertThat(response.total).isEqualTo(4);
        assertThat(response.patterns).hasSize(1);

        var pattern = response.patterns.get(0);
        assertThat(pattern.id).isEqualTo("PATTERN_ID");
        assertThat(pattern.name).isEqualTo("Summerdress");
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
    void shouldNotAllowAccessWithInvalidToken() {
        // given: a request to query published patterns
        var request = new QueryPublishedPatternsRequest();
        request.searchTerm = "test";
        request.skip = 3;
        request.limit = 5;
        request.categories = Set.of("CATEGORY_ID");
        request.sort = new PatternsSortDTO();
        request.sort.property = ALPHABETICAL;
        request.sort.direction = ASCENDING;

        // when: posting the request with an invalid token
        var exchange = client.post()
                .uri("/api/patterns/published")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

}
