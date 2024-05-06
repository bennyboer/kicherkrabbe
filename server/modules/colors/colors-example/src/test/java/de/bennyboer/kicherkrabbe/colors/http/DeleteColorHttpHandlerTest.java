package de.bennyboer.kicherkrabbe.colors.http;

import de.bennyboer.kicherkrabbe.colors.http.responses.DeleteColorResponse;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class DeleteColorHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyDeleteColor() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a successful response
        when(module.deleteColor(
                "COLOR_ID",
                3L,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.just(4L));

        // when: posting the request
        var exchange = client.delete()
                .uri("/api/colors/COLOR_ID/?version=3")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the version of the deleted color
        var response = exchange.expectBody(DeleteColorResponse.class)
                .returnResult()
                .getResponseBody();
        assertThat(response.version).isEqualTo(4L);
    }

    @Test
    void shouldNotAllowUnauthorizedAccess() {
        // when: posting the request without a token
        var exchange = client.delete()
                .uri("/api/colors/COLOR_ID?version=3")
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldNotAllowAccessWithInvalidToken() {
        // when: posting the request with an invalid token
        var exchange = client.delete()
                .uri("/api/colors/COLOR_ID?version=3")
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

}
