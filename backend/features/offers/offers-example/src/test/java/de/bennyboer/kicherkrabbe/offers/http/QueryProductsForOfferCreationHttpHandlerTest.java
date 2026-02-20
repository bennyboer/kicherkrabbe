package de.bennyboer.kicherkrabbe.offers.http;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.offers.Actions;
import de.bennyboer.kicherkrabbe.offers.ProductsPage;
import de.bennyboer.kicherkrabbe.offers.api.requests.QueryProductsForOfferCreationRequest;
import de.bennyboer.kicherkrabbe.offers.api.responses.QueryProductsForOfferCreationResponse;
import de.bennyboer.kicherkrabbe.offers.samples.SampleLookupProduct;
import de.bennyboer.kicherkrabbe.permissions.*;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class QueryProductsForOfferCreationHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyQueryProducts() {
        var token = createTokenForUser("USER_ID");

        var request = new QueryProductsForOfferCreationRequest();
        request.searchTerm = "";
        request.skip = 0;
        request.limit = 10;

        var lookupProduct = SampleLookupProduct.builder().build().toModel();

        when(module.getProductsForOfferCreation(
                "",
                0L,
                10L,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.just(ProductsPage.of(0, 10, 1, List.of(lookupProduct))));

        var exchange = client.post()
                .uri("/offers/products")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        exchange.expectStatus().isOk();
        var response = exchange.expectBody(QueryProductsForOfferCreationResponse.class)
                .returnResult()
                .getResponseBody();
        assertThat(response.total).isEqualTo(1);
        assertThat(response.products).hasSize(1);
        assertThat(response.products.getFirst().id).isEqualTo("PRODUCT_ID");
        assertThat(response.products.getFirst().number).isEqualTo("P-001");
    }

    @Test
    void shouldNotAllowUnauthorizedAccess() {
        var request = new QueryProductsForOfferCreationRequest();
        request.searchTerm = "";
        request.skip = 0;
        request.limit = 10;

        var exchange = client.post()
                .uri("/offers/products")
                .bodyValue(request)
                .exchange();

        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithForbiddenForMissingPermission() {
        var token = createTokenForUser("USER_ID");

        var request = new QueryProductsForOfferCreationRequest();
        request.searchTerm = "";
        request.skip = 0;
        request.limit = 10;

        when(module.getProductsForOfferCreation(
                "",
                0L,
                10L,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.error(new MissingPermissionError(
                Permission.builder()
                        .holder(Holder.user(HolderId.of("USER_ID")))
                        .isAllowedTo(Actions.CREATE)
                        .onType(ResourceType.of("OFFER"))
        )));

        var exchange = client.post()
                .uri("/offers/products")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        exchange.expectStatus().isForbidden();
    }

}
