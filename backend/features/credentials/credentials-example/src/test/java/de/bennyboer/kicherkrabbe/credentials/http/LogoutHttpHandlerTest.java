package de.bennyboer.kicherkrabbe.credentials.http;

import de.bennyboer.kicherkrabbe.credentials.http.api.requests.LogoutRequest;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.when;

public class LogoutHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyLogout() {
        // given: a request to logout
        var request = new LogoutRequest();
        request.refreshToken = "SomeRefreshToken";

        // and: the module is configured to accept the logout
        when(module.logout("SomeRefreshToken"))
                .thenReturn(Mono.empty());

        // when: logging out
        var exchange = client.post()
                .uri("/credentials/logout")
                .bodyValue(request)
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();
    }

    @Test
    void shouldReturnOkEvenOnLogoutError() {
        // given: a request to logout with an invalid token
        var request = new LogoutRequest();
        request.refreshToken = "InvalidToken";

        // and: the module returns an error
        when(module.logout("InvalidToken"))
                .thenReturn(Mono.error(new IllegalArgumentException("Token not found")));

        // when: logging out
        var exchange = client.post()
                .uri("/credentials/logout")
                .bodyValue(request)
                .exchange();

        // then: the response is still successful (graceful degradation)
        exchange.expectStatus().isOk();
    }

}
