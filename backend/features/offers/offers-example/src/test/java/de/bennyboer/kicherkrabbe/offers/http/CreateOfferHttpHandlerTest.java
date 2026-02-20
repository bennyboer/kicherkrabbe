package de.bennyboer.kicherkrabbe.offers.http;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.offers.Actions;
import de.bennyboer.kicherkrabbe.offers.api.MoneyDTO;
import de.bennyboer.kicherkrabbe.permissions.*;
import de.bennyboer.kicherkrabbe.offers.api.NotesDTO;
import de.bennyboer.kicherkrabbe.offers.api.requests.CreateOfferRequest;
import de.bennyboer.kicherkrabbe.offers.api.responses.CreateOfferResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class CreateOfferHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyCreateOffer() {
        var request = new CreateOfferRequest();
        request.productId = "PRODUCT_ID";
        request.imageIds = List.of("IMAGE_1");

        var notes = new NotesDTO();
        notes.description = "Description";
        request.notes = notes;

        var price = new MoneyDTO();
        price.amount = 1999L;
        price.currency = "EUR";
        request.price = price;

        var token = createTokenForUser("USER_ID");

        when(module.createOffer(
                any(), any(), any(), any(),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.just("OFFER_ID"));

        var exchange = client.post()
                .uri("/offers/create")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        exchange.expectStatus().isOk();

        var response = exchange.expectBody(CreateOfferResponse.class)
                .returnResult()
                .getResponseBody();
        assertThat(response.id).isEqualTo("OFFER_ID");
    }

    @Test
    void shouldRespondWithBadRequestOnInvalidRequest() {
        var request = new CreateOfferRequest();
        request.productId = "";
        request.imageIds = List.of();
        var notes = new NotesDTO();
        notes.description = "";
        request.notes = notes;
        var price = new MoneyDTO();
        price.amount = 0L;
        price.currency = "EUR";
        request.price = price;

        var token = createTokenForUser("USER_ID");

        when(module.createOffer(
                any(), any(), any(), any(),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.error(new IllegalArgumentException("Invalid request")));

        var exchange = client.post()
                .uri("/offers/create")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        exchange.expectStatus().isBadRequest();
    }

    @Test
    void shouldNotAllowUnauthorizedAccess() {
        var request = new CreateOfferRequest();

        var exchange = client.post()
                .uri("/offers/create")
                .bodyValue(request)
                .exchange();

        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldNotAllowAccessWithInvalidToken() {
        var request = new CreateOfferRequest();

        var exchange = client.post()
                .uri("/offers/create")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithForbiddenForMissingPermission() {
        var request = new CreateOfferRequest();
        request.productId = "PRODUCT_ID";
        request.imageIds = List.of("IMAGE_1");
        var notes = new NotesDTO();
        notes.description = "Description";
        request.notes = notes;
        var price = new MoneyDTO();
        price.amount = 1999L;
        price.currency = "EUR";
        request.price = price;

        var token = createTokenForUser("USER_ID");

        when(module.createOffer(
                any(), any(), any(), any(),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.error(new MissingPermissionError(
                Permission.builder()
                        .holder(Holder.user(HolderId.of("USER_ID")))
                        .isAllowedTo(Actions.CREATE)
                        .on(Resource.ofType(ResourceType.of("OFFER")))
        )));

        var exchange = client.post()
                .uri("/offers/create")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        exchange.expectStatus().isForbidden();
    }
}
