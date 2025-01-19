package de.bennyboer.kicherkrabbe.products.http;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.*;
import de.bennyboer.kicherkrabbe.products.Actions;
import de.bennyboer.kicherkrabbe.products.api.*;
import de.bennyboer.kicherkrabbe.products.api.responses.QueryProductsResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class QueryProductsHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyQueryProducts() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a successful response
        var response = new QueryProductsResponse();
        response.total = 102;
        response.products = new ArrayList<>();

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

        var product1 = new ProductDTO();
        product1.id = "PRODUCT_ID_1";
        product1.version = 3L;
        product1.number = "0000000001";
        product1.images = List.of("IMAGE_ID_1", "IMAGE_ID_2");
        product1.links = List.of(link1, link2);
        product1.fabricComposition = fabricComposition;
        product1.notes = new NotesDTO();
        product1.notes.contains = "Some contents";
        product1.notes.care = "Some care";
        product1.notes.safety = "Some safety";
        product1.producedAt = Instant.parse("2024-12-03T12:00:00.000Z");
        product1.createdAt = Instant.parse("2024-12-03T12:30:00.000Z");
        response.products.add(product1);

        var product2 = new ProductDTO();
        product2.id = "PRODUCT_ID_2";
        product2.version = 2L;
        product2.number = "0000000002";
        product2.images = List.of("IMAGE_ID_3");
        product2.links = List.of();
        product2.fabricComposition = fabricComposition;
        product2.notes = new NotesDTO();
        product2.notes.contains = "";
        product2.notes.care = "";
        product2.notes.safety = "";
        product2.producedAt = Instant.parse("2024-12-08T12:00:00.000Z");
        product2.createdAt = Instant.parse("2024-12-08T12:30:00.000Z");
        response.products.add(product2);

        when(module.getProducts(
                eq("Search term"),
                eq(Instant.parse("2024-12-01T00:00:00.000Z")),
                eq(Instant.parse("2024-12-31T23:59:59.999Z")),
                eq(100L),
                eq(300L),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.just(response));

        // when: posting the request
        var exchange = client.get()
                .uri(builder -> builder.path("/api/products")
                        .queryParam("searchTerm", "Search term")
                        .queryParam("from", "2024-12-01T00:00:00.000Z")
                        .queryParam("to", "2024-12-31T23:59:59.999Z")
                        .queryParam("skip", "100")
                        .queryParam("limit", "300")
                        .build())
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the expected products
        var result = exchange.expectBody(QueryProductsResponse.class).returnResult().getResponseBody();
        assertThat(result.total).isEqualTo(102);
        assertThat(result.products).hasSize(2);
        assertThat(result.products.get(0)).isEqualTo(product1);
        assertThat(result.products.get(1)).isEqualTo(product2);
    }

    @Test
    void shouldQueryProductsSuccessfullyWithoutQueryParams() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a successful response
        var response = new QueryProductsResponse();
        response.total = 2;
        response.products = new ArrayList<>();

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

        var product1 = new ProductDTO();
        product1.id = "PRODUCT_ID_1";
        product1.version = 3L;
        product1.number = "0000000001";
        product1.images = List.of("IMAGE_ID_1", "IMAGE_ID_2");
        product1.links = List.of(link1, link2);
        product1.fabricComposition = fabricComposition;
        product1.notes = new NotesDTO();
        product1.notes.contains = "Some contents";
        product1.notes.care = "Some care";
        product1.notes.safety = "Some safety";
        product1.producedAt = Instant.parse("2024-12-03T12:00:00.000Z");
        product1.createdAt = Instant.parse("2024-12-03T12:30:00.000Z");
        response.products.add(product1);

        var product2 = new ProductDTO();
        product2.id = "PRODUCT_ID_2";
        product2.version = 2L;
        product2.number = "0000000002";
        product2.images = List.of("IMAGE_ID_3");
        product2.links = List.of();
        product2.fabricComposition = fabricComposition;
        product2.notes = new NotesDTO();
        product2.notes.contains = "";
        product2.notes.care = "";
        product2.notes.safety = "";
        product2.producedAt = Instant.parse("2024-12-08T12:00:00.000Z");
        product2.createdAt = Instant.parse("2024-12-08T12:30:00.000Z");
        response.products.add(product2);

        when(module.getProducts(
                eq(""),
                eq(null),
                eq(null),
                eq(0L),
                eq(100L),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.just(response));

        // when: posting the request
        var exchange = client.get()
                .uri(builder -> builder.path("/api/products").build())
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the expected products
        var result = exchange.expectBody(QueryProductsResponse.class).returnResult().getResponseBody();
        assertThat(result.total).isEqualTo(2);
        assertThat(result.products).hasSize(2);
        assertThat(result.products.get(0)).isEqualTo(product1);
        assertThat(result.products.get(1)).isEqualTo(product2);
    }

    @Test
    void shouldCorrectNegativeSkipAndLimitQueryParams() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: a valid response
        var response = new QueryProductsResponse();
        response.total = 0;
        response.products = List.of();

        // and: the module is configured to return an error
        when(module.getProducts(
                eq(""),
                eq(null),
                eq(null),
                eq(0L),
                eq(100L),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.just(response));

        // when: posting the request
        var exchange = client.get()
                .uri(builder -> builder.path("/api/products")
                        .queryParam("skip", -100)
                        .queryParam("limit", -100)
                        .build())
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is ok
        exchange.expectStatus().isOk();

        // and: the response contains the expected products
        var result = exchange.expectBody(QueryProductsResponse.class).returnResult().getResponseBody();
        assertThat(result.total).isEqualTo(0);
        assertThat(result.products).isEmpty();
    }

    @Test
    void shouldRespondWithUnauthorizedForAnonymousAccess() {
        // when: posting the request
        var exchange = client.get()
                .uri("/api/products")
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithUnauthorizedForInvalidToken() {
        // when: posting the request
        var exchange = client.get()
                .uri("/api/products")
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
        when(module.getProducts(
                eq(""),
                eq(null),
                eq(null),
                eq(0L),
                eq(100L),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.error(new MissingPermissionError(
                Permission.builder()
                        .holder(Holder.user(HolderId.of("USER_ID")))
                        .isAllowedTo(Actions.READ)
                        .onType(ResourceType.of("PRODUCT"))
        )));

        // when: posting the request
        var exchange = client.get()
                .uri("/api/products")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is forbidden
        exchange.expectStatus().isForbidden();
    }

}
