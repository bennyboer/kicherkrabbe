package de.bennyboer.kicherkrabbe.fabrics.http;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.fabrics.ColorId;
import de.bennyboer.kicherkrabbe.fabrics.http.api.responses.QueryColorsResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class QueryColorsHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyQueryColorsAsUser() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a successful response
        when(module.getColorsUsedInFabrics(Agent.user(AgentId.of("USER_ID")))).thenReturn(Flux.just(
                ColorId.of("COLOR_ID_1"),
                ColorId.of("COLOR_ID_2")
        ));

        // when: posting the request
        var exchange = client.get()
                .uri("/api/fabrics/colors")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the colors
        var response = exchange.expectBody(QueryColorsResponse.class).returnResult().getResponseBody();
        assertThat(response.colorIds).containsExactlyInAnyOrder("COLOR_ID_1", "COLOR_ID_2");
    }

    @Test
    void shouldAllowUnauthorizedAccess() {
        // given: the module is configured to return a successful response
        when(module.getColorsUsedInFabrics(Agent.anonymous())).thenReturn(Flux.just(
                ColorId.of("COLOR_ID_1"),
                ColorId.of("COLOR_ID_2")
        ));

        // when: posting the request without a token
        var exchange = client.get()
                .uri("/api/fabrics/colors")
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the colors
        var response = exchange.expectBody(QueryColorsResponse.class).returnResult().getResponseBody();
        assertThat(response.colorIds).containsExactlyInAnyOrder("COLOR_ID_1", "COLOR_ID_2");
    }

    @Test
    void shouldNotAllowAccessWithInvalidToken() {
        // when: posting the request with an invalid token
        var exchange = client.get()
                .uri("/api/fabrics/colors")
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

}
