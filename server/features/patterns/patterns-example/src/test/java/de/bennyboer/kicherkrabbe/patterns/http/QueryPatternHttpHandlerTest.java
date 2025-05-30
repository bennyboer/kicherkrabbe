package de.bennyboer.kicherkrabbe.patterns.http;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateNotFoundError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.money.Money;
import de.bennyboer.kicherkrabbe.patterns.*;
import de.bennyboer.kicherkrabbe.patterns.http.api.responses.QueryPatternResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class QueryPatternHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyQueryPattern() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a successful response
        when(module.getPattern(
                "PATTERN_ID",
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.just(PatternDetails.of(
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
        )));

        // when: posting the request
        var exchange = client.get()
                .uri("/api/patterns/PATTERN_ID")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the pattern
        var response = exchange.expectBody(QueryPatternResponse.class).returnResult().getResponseBody();
        assertThat(response.pattern.id).isEqualTo("PATTERN_ID");
        assertThat(response.pattern.version).isEqualTo(0L);
        assertThat(response.pattern.published).isTrue();
        assertThat(response.pattern.name).isEqualTo("Summerdress");
        assertThat(response.pattern.description).isEqualTo("A beautiful summer dress");
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

        assertThat(response.pattern.createdAt).isEqualTo("2024-05-12T12:30:00.00Z");
    }

    @Test
    void shouldNotAllowUnauthorizedAccess() {
        // when: posting the request without a token
        var exchange = client.get()
                .uri("/api/patterns/PATTERN_ID")
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldNotAllowAccessWithInvalidToken() {
        // when: posting the request with an invalid token
        var exchange = client.get()
                .uri("/api/patterns/PATTERN_ID")
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldReturnWith404WhenPatternNotFound() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return n not found error
        when(module.getPattern(
                "PATTERN_ID",
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.error(new AggregateNotFoundError(
                AggregateType.of("PATTERN"),
                AggregateId.of("PATTERN_ID")
        )));

        // when: posting the request
        var exchange = client.get()
                .uri("/api/patterns/PATTERN_ID")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is not found
        exchange.expectStatus().isNotFound();
    }

}
