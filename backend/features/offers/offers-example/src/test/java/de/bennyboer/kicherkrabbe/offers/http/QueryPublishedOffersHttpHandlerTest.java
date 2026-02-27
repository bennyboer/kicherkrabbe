package de.bennyboer.kicherkrabbe.offers.http;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.offers.PublishedOffersPage;
import de.bennyboer.kicherkrabbe.offers.api.requests.QueryPublishedOffersRequest;
import de.bennyboer.kicherkrabbe.offers.api.responses.QueryPublishedOffersResponse;
import de.bennyboer.kicherkrabbe.offers.samples.SamplePublishedOffer;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class QueryPublishedOffersHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyQueryPublishedOffers() {
        var request = new QueryPublishedOffersRequest();
        request.searchTerm = "";
        request.skip = 0;
        request.limit = 10;

        var publishedOffer = SamplePublishedOffer.builder().build().toModel();

        when(module.getPublishedOffers(
                eq(""),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                eq(0L),
                eq(10L),
                any(Agent.class)
        )).thenReturn(Mono.just(PublishedOffersPage.of(0, 10, 1, List.of(publishedOffer))));

        var exchange = client.post()
                .uri("/offers/published")
                .bodyValue(request)
                .exchange();

        exchange.expectStatus().isOk();
        var response = exchange.expectBody(QueryPublishedOffersResponse.class)
                .returnResult()
                .getResponseBody();
        assertThat(response.total).isEqualTo(1);
        assertThat(response.offers).hasSize(1);
        assertThat(response.offers.getFirst().id).isEqualTo("OFFER_ID");
        assertThat(response.offers.getFirst().alias).isEqualTo("sample-offer");
        assertThat(response.offers.getFirst().reserved).isFalse();
    }

    @Test
    void shouldAllowAnonymousAccess() {
        var request = new QueryPublishedOffersRequest();
        request.searchTerm = "";
        request.skip = 0;
        request.limit = 10;

        when(module.getPublishedOffers(
                eq(""),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                eq(0L),
                eq(10L),
                any(Agent.class)
        )).thenReturn(Mono.just(PublishedOffersPage.of(0, 10, 0, List.of())));

        var exchange = client.post()
                .uri("/offers/published")
                .bodyValue(request)
                .exchange();

        exchange.expectStatus().isOk();
    }
}
