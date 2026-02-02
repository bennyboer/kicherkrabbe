package de.bennyboer.kicherkrabbe.patterns.http;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.patterns.http.api.MoneyDTO;
import de.bennyboer.kicherkrabbe.patterns.http.api.PatternExtraDTO;
import de.bennyboer.kicherkrabbe.patterns.http.api.requests.UpdatePatternExtrasRequest;
import de.bennyboer.kicherkrabbe.patterns.http.api.responses.UpdatePatternExtrasResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class UpdatePatternExtrasHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyUpdatePatternExtras() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: a request to update the extras of a pattern
        var extra1 = new PatternExtraDTO();
        extra1.name = "Sewing instructions";
        extra1.price = new MoneyDTO();
        extra1.price.amount = 200;
        extra1.price.currency = "EUR";

        var extra2 = new PatternExtraDTO();
        extra2.name = "Satin ribbon";
        extra2.price = new MoneyDTO();
        extra2.price.amount = 100;
        extra2.price.currency = "EUR";


        var request = new UpdatePatternExtrasRequest();
        request.version = 3L;
        request.extras = List.of(extra1, extra2);

        // and: the module is configured to return a successful response
        when(module.updatePatternExtras(
                "PATTERN_ID",
                3L,
                List.of(extra1, extra2),
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.just(4L));

        // when: posting the request
        var exchange = client.post()
                .uri("/patterns/PATTERN_ID/update/extras")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the new version of the pattern
        exchange.expectBody(UpdatePatternExtrasResponse.class)
                .value(response -> assertThat(response.version).isEqualTo(4L));
    }

    @Test
    void shouldRespondWith409OnAggregateVersionOutdatedError() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: a request to update the extras of a pattern
        var extra1 = new PatternExtraDTO();
        extra1.name = "Sewing instructions";
        extra1.price = new MoneyDTO();
        extra1.price.amount = 200;
        extra1.price.currency = "EUR";

        var extra2 = new PatternExtraDTO();
        extra2.name = "Satin ribbon";
        extra2.price = new MoneyDTO();
        extra2.price.amount = 100;
        extra2.price.currency = "EUR";

        var request = new UpdatePatternExtrasRequest();
        request.version = 3L;
        request.extras = List.of(extra1, extra2);

        // and: the module is configured to return a conflict response
        when(module.updatePatternExtras(
                "PATTERN_ID",
                3L,
                List.of(extra1, extra2),
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.error(new AggregateVersionOutdatedError(
                AggregateType.of("TEST"),
                AggregateId.of("TEST_ID"),
                Version.zero()
        )));

        // when: posting the request
        var exchange = client.post()
                .uri("/patterns/PATTERN_ID/update/extras")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is conflict
        exchange.expectStatus().isEqualTo(409);
    }

    @Test
    void shouldNotAllowUnauthorizedAccess() {
        // when: posting the request without a token
        var exchange = client.post()
                .uri("/patterns/PATTERN_ID/update/extras")
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldNotAllowAccessWithInvalidToken() {
        // when: posting the request with an invalid token
        var exchange = client.post()
                .uri("/patterns/PATTERN_ID/update/extras")
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithBadRequestOnInvalidRequest() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: a request to update the extras of a pattern
        var request = new UpdatePatternExtrasRequest();
        request.version = 3L;

        // and: the module is configured to return an illegal argument exception
        when(module.updatePatternExtras(
                "PATTERN_ID",
                3L,
                null,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.error(new IllegalArgumentException("Invalid request")));

        // when: posting the request
        var exchange = client.post()
                .uri("/patterns/PATTERN_ID/update/extras")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is bad request
        exchange.expectStatus().isBadRequest();
    }

}
