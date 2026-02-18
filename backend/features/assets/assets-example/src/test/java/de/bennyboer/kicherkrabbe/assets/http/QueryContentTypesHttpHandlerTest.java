package de.bennyboer.kicherkrabbe.assets.http;

import de.bennyboer.kicherkrabbe.assets.http.api.responses.QueryContentTypesResponse;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class QueryContentTypesHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyQueryContentTypes() {
        var token = createTokenForUser("USER_ID");

        when(module.getContentTypes(
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.just(List.of("image/jpeg", "image/png")));

        var exchange = client.get()
                .uri("/assets/content-types")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        exchange.expectStatus().isOk();

        var response = exchange.expectBody(QueryContentTypesResponse.class)
                .returnResult()
                .getResponseBody();
        assertThat(response.contentTypes).containsExactly("image/jpeg", "image/png");
    }

    @Test
    void shouldReturnEmptyContentTypesWhenNoAssetsExist() {
        var token = createTokenForUser("USER_ID");

        when(module.getContentTypes(
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.just(List.of()));

        var exchange = client.get()
                .uri("/assets/content-types")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        exchange.expectStatus().isOk();

        var response = exchange.expectBody(QueryContentTypesResponse.class)
                .returnResult()
                .getResponseBody();
        assertThat(response.contentTypes).isEmpty();
    }

    @Test
    void shouldNotAllowUnauthorizedAccessToContentTypes() {
        client.get()
                .uri("/assets/content-types")
                .exchange()
                .expectStatus().isUnauthorized();
    }

}
