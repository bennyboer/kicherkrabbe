package de.bennyboer.kicherkrabbe.assets.http;

import de.bennyboer.kicherkrabbe.assets.AssetTooLargeError;
import de.bennyboer.kicherkrabbe.assets.http.responses.UploadAssetResponse;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class UploadAssetHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyUploadAsset() {
        // given: a file to upload
        var multipartBodyBuilder = new MultipartBodyBuilder();
        multipartBodyBuilder.part("file", "Hello, World!".getBytes())
                .contentType(MediaType.IMAGE_JPEG);

        // and: having a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a successful response
        when(module.uploadAsset(
                eq("image/jpeg"),
                any(),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.just("ASSET_ID"));

        // when: posting the request
        var exchange = client.post()
                .uri("/api/assets/upload")
                .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the ID of the new asset
        var response = exchange.expectBody(UploadAssetResponse.class)
                .returnResult()
                .getResponseBody();
        assertThat(response.assetId).isEqualTo("ASSET_ID");
    }

    @Test
    void shouldNotAllowUnauthorizedAccess() {
        // given: a file to upload
        var multipartBodyBuilder = new MultipartBodyBuilder();
        multipartBodyBuilder.part("file", "Hello, World!".getBytes())
                .contentType(MediaType.MULTIPART_FORM_DATA);

        // when: posting the request without a token
        var exchange = client.post()
                .uri("/api/assets/upload")
                .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldNotAllowAccessWithInvalidToken() {
        // given: a file to upload
        var multipartBodyBuilder = new MultipartBodyBuilder();
        multipartBodyBuilder.part("file", "Hello, World!".getBytes())
                .contentType(MediaType.MULTIPART_FORM_DATA);

        // when: posting the request with an invalid token
        var exchange = client.post()
                .uri("/api/assets/upload")
                .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithBadRequestIfContentTypeIsMissing() {
        // given: a file to upload
        var multipartBodyBuilder = new MultipartBodyBuilder();
        multipartBodyBuilder.part("file", "Hello, World!".getBytes());

        // and: having a valid token for a user
        var token = createTokenForUser("USER_ID");

        // when: posting the request
        var exchange = client.post()
                .uri("/api/assets/upload")
                .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is bad request
        exchange.expectStatus().isBadRequest();
    }

    @Test
    void shouldRespondWith413WhenTheAssetIsTooLarge() {
        // given: a file to upload
        var multipartBodyBuilder = new MultipartBodyBuilder();
        var tooLargeContent = new byte[1024 * 1024 * 17];
        multipartBodyBuilder.part("file", tooLargeContent)
                .contentType(MediaType.IMAGE_JPEG);

        // and: having a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a successful response
        when(module.uploadAsset(
                eq("image/jpeg"),
                any(),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.error(new AssetTooLargeError()));

        // when: posting the request
        var exchange = client.post()
                .uri("/api/assets/upload")
                .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is 413 Payload Too Large
        exchange.expectStatus().isEqualTo(org.springframework.http.HttpStatus.PAYLOAD_TOO_LARGE);
    }

}
