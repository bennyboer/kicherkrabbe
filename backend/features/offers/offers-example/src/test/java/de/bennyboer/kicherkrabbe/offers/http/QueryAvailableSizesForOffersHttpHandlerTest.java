package de.bennyboer.kicherkrabbe.offers.http;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.offers.api.responses.QueryAvailableSizesForOffersResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class QueryAvailableSizesForOffersHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyQueryAvailableSizes() {
        when(module.getAvailableSizesForOffers(any(Agent.class))).thenReturn(Flux.just("L", "M", "S"));

        var exchange = client.get()
                .uri("/offers/sizes")
                .exchange();

        exchange.expectStatus().isOk();
        var response = exchange.expectBody(QueryAvailableSizesForOffersResponse.class)
                .returnResult()
                .getResponseBody();
        assertThat(response.sizes).containsExactly("L", "M", "S");
    }

    @Test
    void shouldAllowAnonymousAccess() {
        when(module.getAvailableSizesForOffers(any(Agent.class))).thenReturn(Flux.empty());

        var exchange = client.get()
                .uri("/offers/sizes")
                .exchange();

        exchange.expectStatus().isOk();
    }

}
