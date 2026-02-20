package de.bennyboer.kicherkrabbe.offers.http;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.offers.Actions;
import de.bennyboer.kicherkrabbe.offers.api.responses.QueryOfferResponse;
import de.bennyboer.kicherkrabbe.offers.samples.SampleOfferDetails;
import de.bennyboer.kicherkrabbe.permissions.*;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class QueryOfferHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyQueryOffer() {
        var token = createTokenForUser("USER_ID");

        var offerDetails = SampleOfferDetails.builder().build().toModel();

        when(module.getOffer(
                "OFFER_ID",
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.just(offerDetails));

        var exchange = client.get()
                .uri("/offers/OFFER_ID/")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        exchange.expectStatus().isOk();
        var response = exchange.expectBody(QueryOfferResponse.class)
                .returnResult()
                .getResponseBody();
        assertThat(response.offer.id).isEqualTo("OFFER_ID");
        assertThat(response.offer.product.id).isEqualTo("PRODUCT_ID");
        assertThat(response.offer.product.number).isEqualTo("P-001");
        assertThat(response.offer.imageIds).containsExactly("IMAGE_ID");
    }

    @Test
    void shouldNotAllowUnauthorizedAccess() {
        var exchange = client.get()
                .uri("/offers/OFFER_ID/")
                .exchange();

        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithForbiddenForMissingPermission() {
        var token = createTokenForUser("USER_ID");

        when(module.getOffer(
                "OFFER_ID",
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.error(new MissingPermissionError(
                Permission.builder()
                        .holder(Holder.user(HolderId.of("USER_ID")))
                        .isAllowedTo(Actions.READ)
                        .on(Resource.of(ResourceType.of("OFFER"), ResourceId.of("OFFER_ID")))
        )));

        var exchange = client.get()
                .uri("/offers/OFFER_ID/")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        exchange.expectStatus().isForbidden();
    }

}
