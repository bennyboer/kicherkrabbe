package de.bennyboer.kicherkrabbe.products.http;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.*;
import de.bennyboer.kicherkrabbe.products.Actions;
import de.bennyboer.kicherkrabbe.products.api.requests.UpdateImagesRequest;
import de.bennyboer.kicherkrabbe.products.api.responses.UpdateImagesResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class UpdateImagesHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyUpdateImages() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: a request to update the images
        var request = new UpdateImagesRequest();
        request.version = 2L;
        request.images = List.of("IMAGE_ID_1", "IMAGE_ID_2");

        // and: the module is configured to return a successful response
        var response = new UpdateImagesResponse();
        response.version = 3L;

        when(module.updateImages(
                eq("SOME_PRODUCT_ID"),
                eq(request),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.just(response));

        // when: posting the request
        var exchange = client.post()
                .uri(builder -> builder.path("/products/{productId}/images/update")
                        .build("SOME_PRODUCT_ID"))
                .headers(headers -> headers.setBearerAuth(token))
                .bodyValue(request)
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the new version of the product
        var result = exchange.expectBody(UpdateImagesResponse.class).returnResult().getResponseBody();
        assertThat(result.version).isEqualTo(3L);
    }

    @Test
    void shouldRespondWith404WhenProductDoesNotExist() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: a request to update the images
        var request = new UpdateImagesRequest();
        request.version = 2L;
        request.images = List.of("IMAGE_ID_1", "IMAGE_ID_2");

        // and: the module is configured to return an empty response
        var response = new UpdateImagesResponse();
        response.version = 3L;

        when(module.updateImages(
                eq("SOME_PRODUCT_ID"),
                eq(request),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.empty());

        // when: posting the request
        var exchange = client.post()
                .uri(builder -> builder.path("/products/{productId}/images/update")
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
                .uri("/products/SOME_PRODUCT_ID/images/update")
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithUnauthorizedForInvalidToken() {
        // when: posting the request
        var exchange = client.post()
                .uri("/products/SOME_PRODUCT_ID/images/update")
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithForbiddenForMissingPermission() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: a request to update the images
        var request = new UpdateImagesRequest();
        request.version = 2L;
        request.images = List.of("IMAGE_ID_1", "IMAGE_ID_2");

        // and: the module is configured to return an error
        when(module.updateImages(
                eq("SOME_PRODUCT_ID"),
                eq(request),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.error(new MissingPermissionError(
                Permission.builder()
                        .holder(Holder.user(HolderId.of("USER_ID")))
                        .isAllowedTo(Actions.UPDATE_IMAGES)
                        .on(Resource.of(ResourceType.of("PRODUCT"), ResourceId.of("SOME_PRODUCT_ID")))
        )));

        // when: posting the request
        var exchange = client.post()
                .uri("/products/SOME_PRODUCT_ID/images/update")
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

        // and: a request to update the images
        var request = new UpdateImagesRequest();
        request.version = 2L;
        request.images = List.of("IMAGE_ID_1", "IMAGE_ID_2");

        // and: the module is configured to return an error
        when(module.updateImages(
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
                .uri("/products/SOME_PRODUCT_ID/images/update")
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

        // and: a request to update the images
        var request = new UpdateImagesRequest();
        request.version = -1L;
        request.images = List.of("IMAGE_ID_1", "IMAGE_ID_2");

        // when: posting the request
        var exchange = client.post()
                .uri("/products/SOME_PRODUCT_ID/images/update")
                .headers(headers -> headers.setBearerAuth(token))
                .bodyValue(request)
                .exchange();

        // then: the response is bad request
        exchange.expectStatus().isBadRequest();
    }

}
