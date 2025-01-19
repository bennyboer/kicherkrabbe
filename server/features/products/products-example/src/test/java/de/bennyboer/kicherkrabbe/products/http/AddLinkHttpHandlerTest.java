package de.bennyboer.kicherkrabbe.products.http;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.*;
import de.bennyboer.kicherkrabbe.products.Actions;
import de.bennyboer.kicherkrabbe.products.api.LinkDTO;
import de.bennyboer.kicherkrabbe.products.api.LinkTypeDTO;
import de.bennyboer.kicherkrabbe.products.api.requests.AddLinkToProductRequest;
import de.bennyboer.kicherkrabbe.products.api.responses.AddLinkToProductResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class AddLinkHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyAddLink() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: a request to add a link
        var request = new AddLinkToProductRequest();
        request.version = 2L;
        request.link = new LinkDTO();
        request.link.type = LinkTypeDTO.PATTERN;
        request.link.id = "PATTERN_ID";
        request.link.name = "Pattern Name";

        // and: the module is configured to return a successful response
        var response = new AddLinkToProductResponse();
        response.version = 3L;

        when(module.addLinkToProduct(
                eq("SOME_PRODUCT_ID"),
                eq(request),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.just(response));

        // when: posting the request
        var exchange = client.post()
                .uri(builder -> builder.path("/api/products/{productId}/links/add")
                        .build("SOME_PRODUCT_ID"))
                .headers(headers -> headers.setBearerAuth(token))
                .bodyValue(request)
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the new version of the product
        var result = exchange.expectBody(AddLinkToProductResponse.class).returnResult().getResponseBody();
        assertThat(result.version).isEqualTo(3L);
    }

    @Test
    void shouldRespondWith404WhenProductDoesNotExist() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: a request to add a link
        var request = new AddLinkToProductRequest();
        request.version = 2L;
        request.link = new LinkDTO();
        request.link.type = LinkTypeDTO.PATTERN;
        request.link.id = "PATTERN_ID";
        request.link.name = "Pattern Name";

        // and: the module is configured to return an empty response
        var response = new AddLinkToProductResponse();
        response.version = 3L;

        when(module.addLinkToProduct(
                eq("SOME_PRODUCT_ID"),
                eq(request),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.empty());

        // when: posting the request
        var exchange = client.post()
                .uri(builder -> builder.path("/api/products/{productId}/links/add")
                        .build("SOME_PRODUCT_ID"))
                .headers(headers -> headers.setBearerAuth(token))
                .bodyValue(request)
                .exchange();

        // then: the response is not found
        exchange.expectStatus().isNotFound();

        // and: the response contains no body
        exchange.expectBody().isEmpty();
    }

    @Test
    void shouldRespondWithUnauthorizedForAnonymousAccess() {
        // when: posting the request
        var exchange = client.post()
                .uri("/api/products/SOME_PRODUCT_ID/links/add")
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithUnauthorizedForInvalidToken() {
        // when: posting the request
        var exchange = client.post()
                .uri("/api/products/SOME_PRODUCT_ID/links/add")
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithForbiddenForMissingPermission() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: a request to add a link
        var request = new AddLinkToProductRequest();
        request.version = 2L;
        request.link = new LinkDTO();
        request.link.type = LinkTypeDTO.PATTERN;
        request.link.id = "PATTERN_ID";
        request.link.name = "Pattern Name";

        // and: the module is configured to return an error
        when(module.addLinkToProduct(
                eq("SOME_PRODUCT_ID"),
                eq(request),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.error(new MissingPermissionError(
                Permission.builder()
                        .holder(Holder.user(HolderId.of("USER_ID")))
                        .isAllowedTo(Actions.ADD_LINKS)
                        .on(Resource.of(ResourceType.of("PRODUCT"), ResourceId.of("SOME_PRODUCT_ID")))
        )));

        // when: posting the request
        var exchange = client.post()
                .uri("/api/products/SOME_PRODUCT_ID/links/add")
                .headers(headers -> headers.setBearerAuth(token))
                .bodyValue(request)
                .exchange();

        // then: the response is forbidden
        exchange.expectStatus().isForbidden();
    }

    @Test
    void shouldRespondWith409WhenVersionMismatch() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: a request to add a link
        var request = new AddLinkToProductRequest();
        request.version = 2L;
        request.link = new LinkDTO();
        request.link.type = LinkTypeDTO.PATTERN;
        request.link.id = "PATTERN_ID";
        request.link.name = "Pattern Name";

        // and: the module is configured to return an error
        when(module.addLinkToProduct(
                eq("SOME_PRODUCT_ID"),
                eq(request),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.error(new AggregateVersionOutdatedError(
                AggregateType.of("PRODUCT"),
                AggregateId.of("SOME_PRODUCT_ID"),
                Version.of(3L)
        )));

        // when: posting the request
        var exchange = client.post()
                .uri("/api/products/SOME_PRODUCT_ID/links/add")
                .headers(headers -> headers.setBearerAuth(token))
                .bodyValue(request)
                .exchange();

        // then: the response is 409 Conflict
        exchange.expectStatus().isEqualTo(409);
    }

    @Test
    void shouldRespondWithBadRequestIfVersionIsNegativeInRequest() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: a request to add a link
        var request = new AddLinkToProductRequest();
        request.version = -1L;
        request.link = new LinkDTO();
        request.link.type = LinkTypeDTO.PATTERN;
        request.link.id = "PATTERN_ID";
        request.link.name = "Pattern Name";

        // when: posting the request
        var exchange = client.post()
                .uri("/api/products/SOME_PRODUCT_ID/links/add")
                .headers(headers -> headers.setBearerAuth(token))
                .bodyValue(request)
                .exchange();

        // then: the response is bad request
        exchange.expectStatus().isBadRequest();
    }

}
