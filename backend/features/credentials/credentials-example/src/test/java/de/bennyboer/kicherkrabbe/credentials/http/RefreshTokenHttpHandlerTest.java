package de.bennyboer.kicherkrabbe.credentials.http;

import de.bennyboer.kicherkrabbe.auth.tokens.RefreshResult;
import de.bennyboer.kicherkrabbe.credentials.http.api.requests.RefreshTokenRequest;
import de.bennyboer.kicherkrabbe.credentials.http.api.responses.RefreshTokenResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class RefreshTokenHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyRefreshToken() {
        // given: a request to refresh a token
        var request = new RefreshTokenRequest();
        request.refreshToken = "OldRefreshToken";

        // and: the module is configured to return a successful response
        when(module.refreshTokens("OldRefreshToken"))
                .thenReturn(Mono.just(RefreshResult.of("NewAccessToken", "NewRefreshToken")));

        // when: refreshing the token
        var exchange = client.post()
                .uri("/credentials/refresh")
                .bodyValue(request)
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains new tokens
        var response = exchange.expectBody(RefreshTokenResponse.class)
                .returnResult()
                .getResponseBody();
        assertThat(response.token).isEqualTo("NewAccessToken");
        assertThat(response.refreshToken).isEqualTo("NewRefreshToken");
    }

    @Test
    void shouldReturnUnauthorizedOnInvalidRefreshToken() {
        // given: a request with an invalid refresh token
        var request = new RefreshTokenRequest();
        request.refreshToken = "InvalidToken";

        // and: the module returns an error
        when(module.refreshTokens("InvalidToken"))
                .thenReturn(Mono.error(new IllegalArgumentException("Invalid refresh token")));

        // when: refreshing the token
        var exchange = client.post()
                .uri("/credentials/refresh")
                .bodyValue(request)
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();

        // and: the response is empty
        exchange.expectBody().isEmpty();
    }

}
