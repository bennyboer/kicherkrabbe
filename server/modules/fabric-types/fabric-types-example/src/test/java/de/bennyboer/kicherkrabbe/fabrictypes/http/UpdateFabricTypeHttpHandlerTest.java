package de.bennyboer.kicherkrabbe.fabrictypes.http;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.fabrictypes.http.requests.UpdateFabricTypeRequest;
import de.bennyboer.kicherkrabbe.fabrictypes.http.responses.UpdateFabricTypeResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class UpdateFabricTypeHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyUpdateFabricType() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: a request to update a fabric type
        var request = new UpdateFabricTypeRequest();
        request.name = "French-Terry";
        request.version = 2;

        // and: the module is configured to return a successful response
        when(module.updateFabricType(
                "FABRIC_TYPE_ID",
                2L,
                request.name,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.just(3L));

        // when: posting the request
        var exchange = client.post()
                .uri("/api/fabric-types/FABRIC_TYPE_ID/update")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the version of the updated fabric type
        var response = exchange.expectBody(UpdateFabricTypeResponse.class)
                .returnResult()
                .getResponseBody();
        assertThat(response.version).isEqualTo(3L);
    }

    @Test
    void shouldNotAllowUnauthorizedAccess() {
        // given: a request to update a fabric type
        var request = new UpdateFabricTypeRequest();
        request.name = "French-Terry";
        request.version = 2;

        // when: posting the request without a token
        var exchange = client.post()
                .uri("/api/fabric-types/FABRIC_TYPE_ID/update")
                .bodyValue(request)
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldNotAllowAccessWithInvalidToken() {
        // when: posting the request with an invalid token
        var exchange = client.delete()
                .uri("/api/fabric-types/FABRIC_TYPE_ID/update")
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

}
