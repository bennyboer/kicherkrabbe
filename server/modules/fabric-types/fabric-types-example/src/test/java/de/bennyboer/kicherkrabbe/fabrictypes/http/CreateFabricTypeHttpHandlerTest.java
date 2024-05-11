package de.bennyboer.kicherkrabbe.fabrictypes.http;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.fabrictypes.http.requests.CreateFabricTypeRequest;
import de.bennyboer.kicherkrabbe.fabrictypes.http.responses.CreateFabricTypeResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class CreateFabricTypeHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyCreateFabricType() {
        // given: a request to create a fabric type
        var request = new CreateFabricTypeRequest();
        request.name = "Jersey";

        // and: having a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a successful response
        when(module.createFabricType(
                request.name,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.just("FABRIC_TYPE_ID"));

        // when: posting the request
        var exchange = client.post()
                .uri("/api/fabric-types/create")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the ID of the new fabric type
        var response = exchange.expectBody(CreateFabricTypeResponse.class)
                .returnResult()
                .getResponseBody();
        assertThat(response.id).isEqualTo("FABRIC_TYPE_ID");
    }

    @Test
    void shouldNotAllowUnauthorizedAccess() {
        // given: a request to create a fabric type
        var request = new CreateFabricTypeRequest();
        request.name = "Jersey";

        // when: posting the request without a token
        var exchange = client.post()
                .uri("/api/fabric-types/create")
                .bodyValue(request)
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldNotAllowAccessWithInvalidToken() {
        // given: a request to create a fabric type
        var request = new CreateFabricTypeRequest();
        request.name = "Jersey";

        // when: posting the request with an invalid token
        var exchange = client.post()
                .uri("/api/fabric-types/create")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithBadRequestOnIllegalRequest() {
        // given: a request to create a fabric type
        var request = new CreateFabricTypeRequest();
        request.name = "";

        // and: having a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return an illegal argument exception
        when(module.createFabricType(
                request.name,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.error(new IllegalArgumentException("Name must not be empty")));

        // when: posting the request
        var exchange = client.post()
                .uri("/api/fabric-types/create")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is bad request
        exchange.expectStatus().isBadRequest();
    }

}
