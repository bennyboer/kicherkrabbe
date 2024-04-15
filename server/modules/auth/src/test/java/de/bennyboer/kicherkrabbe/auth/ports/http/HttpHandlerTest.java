package de.bennyboer.kicherkrabbe.auth.ports.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest
@Import({
        HttpConfig.class
})
public class HttpHandlerTest {

    private final WebTestClient client;

    @Autowired
    public HttpHandlerTest(WebTestClient client) {
        this.client = client;
    }

}
