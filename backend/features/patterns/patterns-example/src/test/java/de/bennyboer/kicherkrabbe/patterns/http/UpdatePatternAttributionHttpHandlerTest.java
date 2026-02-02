package de.bennyboer.kicherkrabbe.patterns.http;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.patterns.http.api.PatternAttributionDTO;
import de.bennyboer.kicherkrabbe.patterns.http.api.requests.UpdatePatternAttributionRequest;
import de.bennyboer.kicherkrabbe.patterns.http.api.responses.UpdatePatternAttributionResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class UpdatePatternAttributionHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyUpdatePatternAttribution() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: a request to update the attribution of a pattern
        var request = new UpdatePatternAttributionRequest();
        request.version = 3L;
        request.attribution = new PatternAttributionDTO();
        request.attribution.originalPatternName = "Original pattern name";
        request.attribution.designer = "Designer";

        // and: the module is configured to return a successful response
        when(module.updatePatternAttribution(
                "PATTERN_ID",
                3L,
                request.attribution,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.just(4L));

        // when: posting the request
        var exchange = client.post()
                .uri("/patterns/PATTERN_ID/update/attribution")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the new version of the pattern
        exchange.expectBody(UpdatePatternAttributionResponse.class)
                .value(response -> assertThat(response.version).isEqualTo(4L));
    }

    @Test
    void shouldRespondWith409OnAggregateVersionOutdatedError() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: a request to update the attribution of a pattern
        var request = new UpdatePatternAttributionRequest();
        request.version = 3L;
        request.attribution = new PatternAttributionDTO();
        request.attribution.originalPatternName = "Original pattern name";
        request.attribution.designer = "Designer";

        // and: the module is configured to return a conflict response
        when(module.updatePatternAttribution(
                "PATTERN_ID",
                3L,
                request.attribution,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.error(new AggregateVersionOutdatedError(
                AggregateType.of("TEST"),
                AggregateId.of("TEST_ID"),
                Version.zero()
        )));

        // when: posting the request
        var exchange = client.post()
                .uri("/patterns/PATTERN_ID/update/attribution")
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
                .uri("/patterns/PATTERN_ID/update/attribution")
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldNotAllowAccessWithInvalidToken() {
        // when: posting the request with an invalid token
        var exchange = client.post()
                .uri("/patterns/PATTERN_ID/update/attribution")
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithBadRequestOnInvalidRequest() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: a request to update the attribution of a pattern
        var request = new UpdatePatternAttributionRequest();
        request.version = 3L;

        // and: the module is configured to return an illegal argument exception
        when(module.updatePatternAttribution(
                "PATTERN_ID",
                3L,
                null,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.error(new IllegalArgumentException("Invalid request")));

        // when: posting the request
        var exchange = client.post()
                .uri("/patterns/PATTERN_ID/update/attribution")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is bad request
        exchange.expectStatus().isBadRequest();
    }

}
