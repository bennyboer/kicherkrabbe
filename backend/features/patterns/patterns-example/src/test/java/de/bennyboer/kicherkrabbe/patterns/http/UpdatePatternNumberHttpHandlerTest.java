package de.bennyboer.kicherkrabbe.patterns.http;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.patterns.NumberAlreadyInUseError;
import de.bennyboer.kicherkrabbe.patterns.PatternId;
import de.bennyboer.kicherkrabbe.patterns.PatternNumber;
import de.bennyboer.kicherkrabbe.patterns.http.api.requests.UpdatePatternNumberRequest;
import de.bennyboer.kicherkrabbe.patterns.http.api.responses.UpdatePatternNumberResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class UpdatePatternNumberHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyUpdatePatternNumber() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: a request to update the number of a pattern
        var request = new UpdatePatternNumberRequest();
        request.version = 3L;
        request.number = "S-D-SUM-2";

        // and: the module is configured to return a successful response
        when(module.updatePatternNumber(
                "PATTERN_ID",
                3L,
                "S-D-SUM-2",
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.just(4L));

        // when: posting the request
        var exchange = client.post()
                .uri("/patterns/PATTERN_ID/update/number")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the new version of the pattern
        exchange.expectBody(UpdatePatternNumberResponse.class)
                .value(response -> assertThat(response.version).isEqualTo(4L));
    }

    @Test
    void shouldRespondWith409OnAggregateVersionOutdatedError() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: a request to update the number of a pattern
        var request = new UpdatePatternNumberRequest();
        request.version = 3L;
        request.number = "S-D-SUM-2";

        // and: the module is configured to return a conflict response
        when(module.updatePatternNumber(
                "PATTERN_ID",
                3L,
                "S-D-SUM-2",
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.error(new AggregateVersionOutdatedError(
                AggregateType.of("TEST"),
                AggregateId.of("TEST_ID"),
                Version.zero()
        )));

        // when: posting the request
        var exchange = client.post()
                .uri("/patterns/PATTERN_ID/update/number")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is conflict
        exchange.expectStatus().isEqualTo(409);
    }

    @Test
    void shouldRespondWith412WhenTheNumberIsAlreadyInUse() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: a request to update the number of a pattern
        var request = new UpdatePatternNumberRequest();
        request.version = 3L;
        request.number = "S-D-SUM-2";

        // and: the module is configured to return a conflict response
        when(module.updatePatternNumber(
                "PATTERN_ID",
                3L,
                "S-D-SUM-2",
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.error(new NumberAlreadyInUseError(
                PatternId.of("CONFLICTING_PATTERN_ID"),
                PatternNumber.of("S-D-SUM-2")
        )));

        // when: posting the request
        var exchange = client.post()
                .uri("/patterns/PATTERN_ID/update/number")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is conflict
        exchange.expectStatus().isEqualTo(412);

        // and: the response contains the reason
        exchange.expectBody().jsonPath("$.reason").isEqualTo("NUMBER_ALREADY_IN_USE");

        // and: the response contains the conflicting pattern id
        exchange.expectBody().jsonPath("$.patternId").isEqualTo("CONFLICTING_PATTERN_ID");

        // and: the response contains the conflicting number
        exchange.expectBody().jsonPath("$.number").isEqualTo("S-D-SUM-2");
    }

    @Test
    void shouldNotAllowUnauthorizedAccess() {
        // when: posting the request without a token
        var exchange = client.post()
                .uri("/patterns/PATTERN_ID/update/number")
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldNotAllowAccessWithInvalidToken() {
        // when: posting the request with an invalid token
        var exchange = client.post()
                .uri("/patterns/PATTERN_ID/update/number")
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

}
