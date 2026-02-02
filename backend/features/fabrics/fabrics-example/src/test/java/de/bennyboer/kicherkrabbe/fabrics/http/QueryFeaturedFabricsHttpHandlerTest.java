package de.bennyboer.kicherkrabbe.fabrics.http;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.fabrics.http.api.responses.QueryFeaturedFabricsResponse;
import de.bennyboer.kicherkrabbe.fabrics.samples.SamplePublishedFabric;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class QueryFeaturedFabricsHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyQueryFeaturedFabrics() {
        var token = createTokenForUser("USER_ID");

        when(module.getFeaturedFabrics(Agent.user(AgentId.of("USER_ID")))).thenReturn(Flux.just(
                SamplePublishedFabric.builder().build().toModel()
        ));

        var exchange = client.get()
                .uri("/fabrics/featured")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        exchange.expectStatus().isOk();

        var response = exchange.expectBody(QueryFeaturedFabricsResponse.class).returnResult().getResponseBody();
        assertThat(response.fabrics).hasSize(1);
    }

    @Test
    void shouldAllowUnauthorizedAccess() {
        when(module.getFeaturedFabrics(Agent.anonymous())).thenReturn(Flux.just(
                SamplePublishedFabric.builder().build().toModel()
        ));

        var exchange = client.get()
                .uri("/fabrics/featured")
                .exchange();

        exchange.expectStatus().isOk();

        var response = exchange.expectBody(QueryFeaturedFabricsResponse.class).returnResult().getResponseBody();
        assertThat(response.fabrics).hasSize(1);
    }

    @Test
    void shouldNotAllowAccessWithInvalidToken() {
        var exchange = client.get()
                .uri("/fabrics/featured")
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        exchange.expectStatus().isUnauthorized();
    }

}
