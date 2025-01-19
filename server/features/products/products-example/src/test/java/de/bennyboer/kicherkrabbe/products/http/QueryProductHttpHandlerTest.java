package de.bennyboer.kicherkrabbe.products.http;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.*;
import de.bennyboer.kicherkrabbe.products.Actions;
import de.bennyboer.kicherkrabbe.products.api.*;
import de.bennyboer.kicherkrabbe.products.api.responses.QueryProductResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class QueryProductHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyQueryProduct() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a successful response
        var link1 = new LinkDTO();
        link1.type = LinkTypeDTO.PATTERN;
        link1.id = "PATTERN_ID_1";
        link1.name = "Pattern 1";

        var link2 = new LinkDTO();
        link2.type = LinkTypeDTO.FABRIC;
        link2.id = "FABRIC_ID_1";
        link2.name = "Fabric 1";

        var fabricComposition = new FabricCompositionDTO();
        fabricComposition.items = new ArrayList<>();
        var fabricCompositionItem1 = new FabricCompositionItemDTO();
        fabricCompositionItem1.fabricType = FabricTypeDTO.COTTON;
        fabricCompositionItem1.percentage = 8000;
        fabricComposition.items.add(fabricCompositionItem1);
        var fabricCompositionItem2 = new FabricCompositionItemDTO();
        fabricCompositionItem2.fabricType = FabricTypeDTO.POLYESTER;
        fabricCompositionItem2.percentage = 2000;
        fabricComposition.items.add(fabricCompositionItem2);

        var response = new QueryProductResponse();
        response.product = new ProductDTO();
        response.product.id = "PRODUCT_ID_1";
        response.product.version = 3L;
        response.product.number = "0000000001";
        response.product.images = List.of("IMAGE_ID_1", "IMAGE_ID_2");
        response.product.links = List.of(link1, link2);
        response.product.fabricComposition = fabricComposition;
        response.product.notes = new NotesDTO();
        response.product.notes.contains = "Some contents";
        response.product.notes.care = "Some care";
        response.product.notes.safety = "Some safety";
        response.product.producedAt = Instant.parse("2024-12-03T12:00:00.000Z");
        response.product.createdAt = Instant.parse("2024-12-03T12:30:00.000Z");

        when(module.getProduct(
                eq("SOME_PRODUCT_ID"),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.just(response));

        // when: posting the request
        var exchange = client.get()
                .uri(builder -> builder.path("/api/products/{productId}").build("SOME_PRODUCT_ID"))
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the expected product
        var result = exchange.expectBody(QueryProductResponse.class).returnResult().getResponseBody();
        assertThat(result.product).isEqualTo(response.product);
    }

    @Test
    void shouldRespondWith404WhenProductDoesNotExist() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return an empty response
        when(module.getProduct(
                eq("SOME_PRODUCT_ID"),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.empty());

        // when: posting the request
        var exchange = client.get()
                .uri(builder -> builder.path("/api/products/{productId}").build("SOME_PRODUCT_ID"))
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is not found
        exchange.expectStatus().isNotFound();

        // and: the response contains no body
        exchange.expectBody().isEmpty();
    }

    @Test
    void shouldRespondWithUnauthorizedForAnonymousAccess() {
        // when: posting the request
        var exchange = client.get()
                .uri("/api/products/SOME_PRODUCT_ID")
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithUnauthorizedForInvalidToken() {
        // when: posting the request
        var exchange = client.get()
                .uri("/api/products/SOME_PRODUCT_ID")
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithForbiddenForMissingPermission() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return an error
        when(module.getProduct(
                eq("SOME_PRODUCT_ID"),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.error(new MissingPermissionError(
                Permission.builder()
                        .holder(Holder.user(HolderId.of("USER_ID")))
                        .isAllowedTo(Actions.READ)
                        .on(Resource.of(ResourceType.of("PRODUCT"), ResourceId.of("SOME_PRODUCT_ID")))
        )));

        // when: posting the request
        var exchange = client.get()
                .uri("/api/products/SOME_PRODUCT_ID")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is forbidden
        exchange.expectStatus().isForbidden();
    }

}
