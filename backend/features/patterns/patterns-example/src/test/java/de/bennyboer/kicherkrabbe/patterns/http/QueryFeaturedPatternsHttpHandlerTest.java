package de.bennyboer.kicherkrabbe.patterns.http;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.patterns.http.api.responses.QueryFeaturedPatternsResponse;
import de.bennyboer.kicherkrabbe.patterns.samples.SamplePublishedPattern;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class QueryFeaturedPatternsHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyQueryFeaturedPatterns() {
        var token = createTokenForUser("USER_ID");

        when(module.getFeaturedPatterns(Agent.user(AgentId.of("USER_ID")))).thenReturn(Flux.just(
                SamplePublishedPattern.builder().build().toModel()
        ));

        var exchange = client.get()
                .uri("/patterns/featured")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        exchange.expectStatus().isOk();

        var response = exchange.expectBody(QueryFeaturedPatternsResponse.class).returnResult().getResponseBody();
        assertThat(response.patterns).hasSize(1);
    }

    @Test
    void shouldAllowUnauthorizedAccess() {
        when(module.getFeaturedPatterns(Agent.anonymous())).thenReturn(Flux.just(
                SamplePublishedPattern.builder().build().toModel()
        ));

        var exchange = client.get()
                .uri("/patterns/featured")
                .exchange();

        exchange.expectStatus().isOk();

        var response = exchange.expectBody(QueryFeaturedPatternsResponse.class).returnResult().getResponseBody();
        assertThat(response.patterns).hasSize(1);
    }

    @Test
    void shouldNotAllowAccessWithInvalidToken() {
        var exchange = client.get()
                .uri("/patterns/featured")
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        exchange.expectStatus().isUnauthorized();
    }

}
