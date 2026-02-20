package de.bennyboer.kicherkrabbe.offers.http;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.offers.Actions;
import de.bennyboer.kicherkrabbe.offers.OffersPage;
import de.bennyboer.kicherkrabbe.offers.api.requests.QueryOffersRequest;
import de.bennyboer.kicherkrabbe.offers.api.responses.QueryOffersResponse;
import de.bennyboer.kicherkrabbe.offers.samples.SampleOfferDetails;
import de.bennyboer.kicherkrabbe.permissions.*;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class QueryOffersHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyQueryOffers() {
        var token = createTokenForUser("USER_ID");

        var request = new QueryOffersRequest();
        request.searchTerm = "";
        request.skip = 0;
        request.limit = 10;

        var offerDetails = SampleOfferDetails.builder().build().toModel();

        when(module.getOffers(
                "",
                0L,
                10L,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.just(OffersPage.of(0, 10, 1, List.of(offerDetails))));

        var exchange = client.post()
                .uri("/offers/")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        exchange.expectStatus().isOk();
        var response = exchange.expectBody(QueryOffersResponse.class)
                .returnResult()
                .getResponseBody();
        assertThat(response.total).isEqualTo(1);
        assertThat(response.offers).hasSize(1);
        assertThat(response.offers.getFirst().id).isEqualTo("OFFER_ID");
    }

    @Test
    void shouldNotAllowUnauthorizedAccess() {
        var request = new QueryOffersRequest();
        request.searchTerm = "";
        request.skip = 0;
        request.limit = 10;

        var exchange = client.post()
                .uri("/offers/")
                .bodyValue(request)
                .exchange();

        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithForbiddenForMissingPermission() {
        var request = new QueryOffersRequest();
        request.searchTerm = "";
        request.skip = 0;
        request.limit = 10;

        var token = createTokenForUser("USER_ID");

        when(module.getOffers(
                "",
                0L,
                10L,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.error(new MissingPermissionError(
                Permission.builder()
                        .holder(Holder.user(HolderId.of("USER_ID")))
                        .isAllowedTo(Actions.READ)
                        .on(Resource.ofType(ResourceType.of("OFFER")))
        )));

        var exchange = client.post()
                .uri("/offers/")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        exchange.expectStatus().isForbidden();
    }
}
