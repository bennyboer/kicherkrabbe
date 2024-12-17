package de.bennyboer.kicherkrabbe.patterns.http;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.money.Money;
import de.bennyboer.kicherkrabbe.patterns.*;
import de.bennyboer.kicherkrabbe.patterns.http.api.requests.QueryPatternsRequest;
import de.bennyboer.kicherkrabbe.patterns.http.api.responses.QueryPatternsResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class QueryPatternsHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyQueryPatterns() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: a request to query patterns
        var request = new QueryPatternsRequest();
        request.searchTerm = "test";
        request.categories = Set.of("CATEGORY_ID");
        request.skip = 3;
        request.limit = 35;

        // and: the module is configured to return a successful response
        when(module.getPatterns(
                "test",
                Set.of("CATEGORY_ID"),
                3,
                35,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.just(PatternsPage.of(
                3,
                35,
                4,
                List.of(PatternDetails.of(
                        PatternId.of("PATTERN_ID"),
                        Version.zero(),
                        true,
                        PatternName.of("Summerdress"),
                        PatternNumber.of("S-D-SUM-1"),
                        PatternDescription.of("A beautiful summer dress"),
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
                        )),
                        Instant.parse("2024-05-12T12:30:00.00Z")
                ))
        )));

        // when: posting the request
        var exchange = client.post()
                .uri("/api/patterns")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the patterns page
        var response = exchange.expectBody(QueryPatternsResponse.class).returnResult().getResponseBody();
        assertThat(response).isNotNull();
        assertThat(response.skip).isEqualTo(3);
        assertThat(response.limit).isEqualTo(35);
        assertThat(response.total).isEqualTo(4);
        assertThat(response.patterns).hasSize(1);

        var responsePattern = response.patterns.get(0);
        assertThat(responsePattern.id).isEqualTo("PATTERN_ID");
        assertThat(responsePattern.version).isEqualTo(0L);
        assertThat(responsePattern.published).isTrue();
        assertThat(responsePattern.name).isEqualTo("Summerdress");
        assertThat(responsePattern.description).isEqualTo("A beautiful summer dress");
        assertThat(responsePattern.attribution.originalPatternName).isEqualTo("Summerdress EXTREME");
        assertThat(responsePattern.attribution.designer).isEqualTo("EXTREME PATTERNS inc.");
        assertThat(responsePattern.categories).containsExactly("DRESS_ID");
        assertThat(responsePattern.images).containsExactly("IMAGE_ID");
        assertThat(responsePattern.variants).hasSize(1);

        var variant = responsePattern.variants.get(0);
        assertThat(variant.name).isEqualTo("Normal");
        assertThat(variant.pricedSizeRanges).hasSize(1);

        var pricedSizeRange = variant.pricedSizeRanges.stream().findFirst().get();
        assertThat(pricedSizeRange.from).isEqualTo(80);
        assertThat(pricedSizeRange.to).isEqualTo(86);
        assertThat(pricedSizeRange.price.amount).isEqualTo(2000);
        assertThat(pricedSizeRange.price.currency).isEqualTo("EUR");

        assertThat(responsePattern.extras).hasSize(1);
        var extra = responsePattern.extras.get(0);
        assertThat(extra.name).isEqualTo("Sewing instructions");
        assertThat(extra.price.amount).isEqualTo(200);
        assertThat(extra.price.currency).isEqualTo("EUR");

        assertThat(responsePattern.createdAt).isEqualTo("2024-05-12T12:30:00.00Z");
    }

    @Test
    void shouldNotAllowUnauthorizedAccess() {
        // given: a request to query patterns
        var request = new QueryPatternsRequest();
        request.searchTerm = "test";
        request.skip = 3;
        request.limit = 35;

        // when: posting the request without a token
        var exchange = client.post()
                .uri("/api/patterns")
                .bodyValue(request)
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldNotAllowAccessWithInvalidToken() {
        // given: a request to query patterns
        var request = new QueryPatternsRequest();
        request.searchTerm = "test";
        request.categories = Set.of("CATEGORY_ID");
        request.skip = 3;
        request.limit = 35;

        // when: posting the request with an invalid token
        var exchange = client.post()
                .uri("/api/patterns")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

}
