package de.bennyboer.kicherkrabbe.colors.http;

import de.bennyboer.kicherkrabbe.colors.http.api.requests.CreateColorRequest;
import de.bennyboer.kicherkrabbe.colors.http.api.responses.CreateColorResponse;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class CreateColorHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyCreateColor() {
        // given: a request to create a color
        var request = new CreateColorRequest();
        request.name = "Red";
        request.red = 255;
        request.green = 0;
        request.blue = 0;

        // and: having a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a successful response
        when(module.createColor(
                request.name,
                request.red,
                request.green,
                request.blue,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.just("COLOR_ID"));

        // when: posting the request
        var exchange = client.post()
                .uri("/api/colors/create")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the ID of the new color
        var response = exchange.expectBody(CreateColorResponse.class)
                .returnResult()
                .getResponseBody();
        assertThat(response.id).isEqualTo("COLOR_ID");
    }

    @Test
    void shouldNotAllowUnauthorizedAccess() {
        // given: a request to create a color
        var request = new CreateColorRequest();
        request.name = "Red";
        request.red = 255;
        request.green = 0;
        request.blue = 0;

        // when: posting the request without a token
        var exchange = client.post()
                .uri("/api/colors/create")
                .bodyValue(request)
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldNotAllowAccessWithInvalidToken() {
        // given: a request to create a color
        var request = new CreateColorRequest();
        request.name = "Red";
        request.red = 255;
        request.green = 0;
        request.blue = 0;

        // when: posting the request with an invalid token
        var exchange = client.post()
                .uri("/api/colors/create")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithBadRequestOnInvalidColor() {
        // given: a request to create a color
        var request = new CreateColorRequest();
        request.name = "Red";
        request.red = 256;
        request.green = 0;
        request.blue = 0;

        // and: having a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return an illegal argument exception
        when(module.createColor(
                request.name,
                request.red,
                request.green,
                request.blue,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.error(new IllegalArgumentException("Invalid color")));

        // when: posting the request
        var exchange = client.post()
                .uri("/api/colors/create")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is bad request
        exchange.expectStatus().isBadRequest();
    }

}
