package de.bennyboer.kicherkrabbe.assets.http;

import de.bennyboer.kicherkrabbe.assets.AssetContent;
import de.bennyboer.kicherkrabbe.assets.ContentType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class QueryAssetContentHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyQueryAssetContent() {
        // given: having a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: some data buffers to return
        var data = "Hello, World!";
        Flux<DataBuffer> buffers$ = Flux.just(DefaultDataBufferFactory.sharedInstance.wrap(data.getBytes(UTF_8)));

        // and: the module is configured to return a successful response
        when(module.getAssetContent(
                eq("ASSET_ID"),
                eq(null),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.just(AssetContent.of(ContentType.of("image/jpeg"), buffers$)));

        // when: posting the request
        var exchange = client.get()
                .uri("/assets/ASSET_ID/content")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the content type is image/jpeg
        exchange.expectHeader().contentType(MediaType.IMAGE_JPEG);

        // and: the response contains the content
        exchange.expectBody(String.class)
                .isEqualTo(data);
    }

    @Test
    void shouldAllowUnauthorizedAccess() {
        // given: some data buffers to return
        var data = "Hello, World!";
        Flux<DataBuffer> buffers$ = Flux.just(DefaultDataBufferFactory.sharedInstance.wrap(data.getBytes(UTF_8)));

        // and: the module is configured to return a successful response
        when(module.getAssetContent(
                eq("ASSET_ID"),
                eq(null),
                eq(Agent.anonymous())
        )).thenReturn(Mono.just(AssetContent.of(ContentType.of("image/jpeg"), buffers$)));

        // when: posting the request without token
        var exchange = client.get()
                .uri("/assets/ASSET_ID/content")
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the content type is image/jpeg
        exchange.expectHeader().contentType(MediaType.IMAGE_JPEG);

        // and: the response contains the content
        exchange.expectBody(String.class)
                .isEqualTo(data);
    }

    @Test
    void shouldNotAllowAccessWithInvalidToken() {
        // when: posting the request with invalid token
        var exchange = client.get()
                .uri("/assets/ASSET_ID/content")
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldReturnWith401IfAssetIsNotFound() {
        // given: having a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a successful response
        when(module.getAssetContent(
                eq("ASSET_ID"),
                eq(null),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.empty());

        // when: posting the request
        var exchange = client.get()
                .uri("/assets/ASSET_ID/content")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldPassWidthParameterToModule() {
        // given: having a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: some data buffers to return
        var data = "Hello, World!";
        Flux<DataBuffer> buffers$ = Flux.just(DefaultDataBufferFactory.sharedInstance.wrap(data.getBytes(UTF_8)));

        // and: the module is configured to return a successful response
        when(module.getAssetContent(
                eq("ASSET_ID"),
                eq(500),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.just(AssetContent.of(ContentType.of("image/webp"), buffers$)));

        // when: posting the request with width parameter
        var exchange = client.get()
                .uri("/assets/ASSET_ID/content?width=500")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the content type is image/webp
        exchange.expectHeader().contentType("image/webp");
    }

    @Test
    void shouldRejectNegativeWidthParameter() {
        // given: having a valid token for a user
        var token = createTokenForUser("USER_ID");

        // when: posting the request with negative width
        var exchange = client.get()
                .uri("/assets/ASSET_ID/content?width=-100")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is bad request
        exchange.expectStatus().isBadRequest();
    }

    @Test
    void shouldRejectZeroWidthParameter() {
        // given: having a valid token for a user
        var token = createTokenForUser("USER_ID");

        // when: posting the request with zero width
        var exchange = client.get()
                .uri("/assets/ASSET_ID/content?width=0")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is bad request
        exchange.expectStatus().isBadRequest();
    }

    @Test
    void shouldRejectNonNumericWidthParameter() {
        // given: having a valid token for a user
        var token = createTokenForUser("USER_ID");

        // when: posting the request with non-numeric width
        var exchange = client.get()
                .uri("/assets/ASSET_ID/content?width=abc")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is bad request
        exchange.expectStatus().isBadRequest();
    }

}
