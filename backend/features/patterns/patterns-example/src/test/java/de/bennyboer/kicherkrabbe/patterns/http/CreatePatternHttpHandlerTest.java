package de.bennyboer.kicherkrabbe.patterns.http;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.patterns.*;
import de.bennyboer.kicherkrabbe.patterns.http.api.*;
import de.bennyboer.kicherkrabbe.patterns.http.api.requests.CreatePatternRequest;
import de.bennyboer.kicherkrabbe.patterns.http.api.responses.CreatePatternResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class CreatePatternHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyCreatePattern() {
        // given: a request to create a pattern
        var attribution = new PatternAttributionDTO();
        attribution.originalPatternName = "Summerdress EXTREME";
        attribution.designer = "EXTREME PATTERNS inc.";

        var pricedSizeRange = new PricedSizeRangeDTO();
        pricedSizeRange.from = 80;
        pricedSizeRange.to = 86L;
        pricedSizeRange.price = new MoneyDTO();
        pricedSizeRange.price.amount = 1000;
        pricedSizeRange.price.currency = "EUR";

        var variant1 = new PatternVariantDTO();
        variant1.name = "Short";
        variant1.pricedSizeRanges = Set.of(pricedSizeRange);

        var variant2 = new PatternVariantDTO();
        variant2.name = "Long";
        variant2.pricedSizeRanges = Set.of(pricedSizeRange);

        var extra = new PatternExtraDTO();
        extra.name = "Pocket";
        extra.price = new MoneyDTO();
        extra.price.amount = 200;
        extra.price.currency = "EUR";

        var request = new CreatePatternRequest();
        request.name = "Ice bear party";
        request.number = "S-M-ICE-1";
        request.description = "A party dress for ice bears";
        request.attribution = attribution;
        request.categories = Set.of("SKIRT_ID", "DRESS_ID");
        request.images = List.of("IMAGE_ID_1", "IMAGE_ID_2");
        request.variants = List.of(variant1, variant2);
        request.extras = List.of(extra);

        // and: having a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a successful response
        when(module.createPattern(
                request.name,
                request.number,
                request.description,
                request.attribution,
                request.categories,
                request.images,
                request.variants,
                request.extras,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.just("PATTERN_ID"));

        // when: posting the request
        var exchange = client.post()
                .uri("/patterns/create")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the ID of the new pattern
        var response = exchange.expectBody(CreatePatternResponse.class)
                .returnResult()
                .getResponseBody();
        assertThat(response.id).isEqualTo("PATTERN_ID");
    }

    @Test
    void shouldRespondWithBadRequestOnInvalidRequest() {
        // given: a request to create a pattern with an invalid request
        var attribution = new PatternAttributionDTO();
        attribution.originalPatternName = "Summerdress EXTREME";
        attribution.designer = "EXTREME PATTERNS inc.";

        var pricedSizeRange = new PricedSizeRangeDTO();
        pricedSizeRange.from = 80;
        pricedSizeRange.to = 86L;
        pricedSizeRange.price = new MoneyDTO();
        pricedSizeRange.price.amount = 1000;
        pricedSizeRange.price.currency = "EUR";

        var variant1 = new PatternVariantDTO();
        variant1.name = "Short";
        variant1.pricedSizeRanges = Set.of(pricedSizeRange);

        var variant2 = new PatternVariantDTO();
        variant2.name = "Long";
        variant2.pricedSizeRanges = Set.of(pricedSizeRange);

        var extra = new PatternExtraDTO();
        extra.name = "Pocket";
        extra.price = new MoneyDTO();
        extra.price.amount = 200;
        extra.price.currency = "EUR";

        var request = new CreatePatternRequest();
        request.name = "Ice bear party";
        request.number = "S-M-ICE-1";
        request.attribution = attribution;
        request.categories = Set.of("SKIRT_ID", "DRESS_ID");
        request.images = List.of();
        request.variants = List.of(variant1, variant2);
        request.extras = List.of(extra);

        // and: having a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return an illegal argument exception
        when(module.createPattern(
                request.name,
                request.number,
                request.description,
                request.attribution,
                request.categories,
                request.images,
                request.variants,
                request.extras,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.error(new IllegalArgumentException("Invalid request")));

        // when: posting the request
        var exchange = client.post()
                .uri("/patterns/create")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is bad request
        exchange.expectStatus().isBadRequest();
    }

    @Test
    void shouldNotAllowUnauthorizedAccess() {
        // given: a request to create a pattern
        var request = new CreatePatternRequest();

        // when: posting the request without a token
        var exchange = client.post()
                .uri("/patterns/create")
                .bodyValue(request)
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldNotAllowAccessWithInvalidToken() {
        // given: a request to create a pattern
        var request = new CreatePatternRequest();

        // when: posting the request with an invalid token
        var exchange = client.post()
                .uri("/patterns/create")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithPreconditionFailedOnMissingCategoriesError() {
        // given: a request to create a pattern with a missing category
        var attribution = new PatternAttributionDTO();
        attribution.originalPatternName = "Summerdress EXTREME";
        attribution.designer = "EXTREME PATTERNS inc.";

        var pricedSizeRange = new PricedSizeRangeDTO();
        pricedSizeRange.from = 80;
        pricedSizeRange.to = 86L;
        pricedSizeRange.price = new MoneyDTO();
        pricedSizeRange.price.amount = 1000;
        pricedSizeRange.price.currency = "EUR";

        var variant1 = new PatternVariantDTO();
        variant1.name = "Short";
        variant1.pricedSizeRanges = Set.of(pricedSizeRange);

        var variant2 = new PatternVariantDTO();
        variant2.name = "Long";
        variant2.pricedSizeRanges = Set.of(pricedSizeRange);

        var extra = new PatternExtraDTO();
        extra.name = "Pocket";
        extra.price = new MoneyDTO();
        extra.price.amount = 200;
        extra.price.currency = "EUR";

        var request = new CreatePatternRequest();
        request.name = "Ice bear party";
        request.number = "S-M-ICE-1";
        request.attribution = attribution;
        request.categories = Set.of("SKIRT_ID", "DRESS_ID");
        request.images = List.of();
        request.variants = List.of(variant1, variant2);
        request.extras = List.of(extra);

        // and: having a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a categories missing error
        when(module.createPattern(
                request.name,
                request.number,
                request.description,
                request.attribution,
                request.categories,
                request.images,
                request.variants,
                request.extras,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.error(new CategoriesMissingError(Set.of(PatternCategoryId.of("SKIRT_ID")))));

        // when: posting the request
        var exchange = client.post()
                .uri("/patterns/create")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is precondition failed
        exchange.expectStatus().isEqualTo(412);
    }

    @Test
    void shouldRespondWithPreconditionFailedOnNumberAlreadyInUseError() {
        // given: a request to create a pattern with a number that is already in use
        var attribution = new PatternAttributionDTO();
        attribution.originalPatternName = "Summerdress EXTREME";
        attribution.designer = "EXTREME PATTERNS inc.";

        var pricedSizeRange = new PricedSizeRangeDTO();
        pricedSizeRange.from = 80;
        pricedSizeRange.to = 86L;
        pricedSizeRange.price = new MoneyDTO();
        pricedSizeRange.price.amount = 1000;
        pricedSizeRange.price.currency = "EUR";

        var variant1 = new PatternVariantDTO();
        variant1.name = "Short";
        variant1.pricedSizeRanges = Set.of(pricedSizeRange);

        var variant2 = new PatternVariantDTO();
        variant2.name = "Long";
        variant2.pricedSizeRanges = Set.of(pricedSizeRange);

        var extra = new PatternExtraDTO();
        extra.name = "Pocket";
        extra.price = new MoneyDTO();
        extra.price.amount = 200;
        extra.price.currency = "EUR";

        var request = new CreatePatternRequest();
        request.name = "Ice bear party";
        request.number = "S-M-ICE-1";
        request.attribution = attribution;
        request.categories = Set.of("SKIRT_ID", "DRESS_ID");
        request.images = List.of();
        request.variants = List.of(variant1, variant2);
        request.extras = List.of(extra);

        // and: having a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a categories missing error
        when(module.createPattern(
                request.name,
                request.number,
                request.description,
                request.attribution,
                request.categories,
                request.images,
                request.variants,
                request.extras,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.error(new NumberAlreadyInUseError(
                PatternId.of("PATTERN_ID"),
                PatternNumber.of("S-M-ICE-1")
        )));

        // when: posting the request
        var exchange = client.post()
                .uri("/patterns/create")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is precondition failed
        exchange.expectStatus().isEqualTo(412);

        // and: the response contains the reason for the error
        exchange.expectBody().jsonPath("$.reason").isEqualTo("NUMBER_ALREADY_IN_USE");

        // and: the response contains the number that is already in use
        exchange.expectBody().jsonPath("$.number").isEqualTo("S-M-ICE-1");

        // and: the response contains the ID of the pattern that uses the number
        exchange.expectBody().jsonPath("$.patternId").isEqualTo("PATTERN_ID");
    }

}
