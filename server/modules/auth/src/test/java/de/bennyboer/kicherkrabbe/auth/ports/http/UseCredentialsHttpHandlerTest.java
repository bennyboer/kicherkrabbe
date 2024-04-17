package de.bennyboer.kicherkrabbe.auth.ports.http;

import org.junit.jupiter.api.Test;

public class UseCredentialsHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldUseCredentials() {
        client.post()
                .uri("/auth/credentials/use")
                .exchange()
                .expectStatus().isOk();
    }

}
