package de.bennyboer.kicherkrabbe.colors.http;

import de.bennyboer.kicherkrabbe.colors.http.requests.UpdateColorRequest;
import de.bennyboer.kicherkrabbe.colors.http.responses.UpdateColorResponse;
import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class UpdateColorHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyUpdateColor() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: a request to update a color
        var request = new UpdateColorRequest();
        request.name = "Green";
        request.version = 2;
        request.red = 0;
        request.green = 255;
        request.blue = 0;

        // and: the module is configured to return a successful response
        when(module.updateColor(
                "COLOR_ID",
                2L,
                request.name,
                request.red,
                request.green,
                request.blue,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.just(3L));

        // when: posting the request
        var exchange = client.post()
                .uri("/api/colors/COLOR_ID/update")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the version of the updated color
        var response = exchange.expectBody(UpdateColorResponse.class)
                .returnResult()
                .getResponseBody();
        assertThat(response.version).isEqualTo(3L);
    }

    @Test
    void shouldNotAllowUnauthorizedAccess() {
        // given: a request to update a color
        var request = new UpdateColorRequest();
        request.name = "Green";
        request.version = 2;
        request.red = 0;
        request.green = 255;
        request.blue = 0;

        // when: posting the request without a token
        var exchange = client.post()
                .uri("/api/colors/COLOR_ID/update")
                .bodyValue(request)
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldNotAllowAccessWithInvalidToken() {
        // when: posting the request with an invalid token
        var exchange = client.delete()
                .uri("/api/colors/COLOR_ID/update")
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldReturn409OnAggregateVersionOutdatedError() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: a request to update a color
        var request = new UpdateColorRequest();
        request.name = "Green";
        request.version = 2;
        request.red = 0;
        request.green = 255;
        request.blue = 0;

        // and: the module is configured to return an error
        when(module.updateColor(
                "COLOR_ID",
                2L,
                request.name,
                request.red,
                request.green,
                request.blue,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.error(new AggregateVersionOutdatedError(
                AggregateType.of("TEST"),
                AggregateId.of("TEST_ID"),
                Version.zero()
        )));

        // when: posting the request
        var exchange = client.post()
                .uri("/api/colors/COLOR_ID/update")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is conflict
        exchange.expectStatus().isEqualTo(409);
    }

    @Test
    void shouldRespondWithBadRequestOnInvalidRequest() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: a request to update a color
        var request = new UpdateColorRequest();
        request.name = "Green";
        request.version = 2;
        request.red = 0;
        request.green = 255;
        request.blue = 0;

        // and: the module is configured to return an illegal argument exception
        when(module.updateColor(
                "COLOR_ID",
                2L,
                request.name,
                request.red,
                request.green,
                request.blue,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.error(new IllegalArgumentException("Invalid request")));

        // when: posting the request
        var exchange = client.post()
                .uri("/api/colors/COLOR_ID/update")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is bad request
        exchange.expectStatus().isBadRequest();
    }

}
