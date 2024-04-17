package de.bennyboer.kicherkrabbe.auth.ports.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest
@ContextConfiguration(classes = {HttpConfig.class})
public class HttpHandlerTest {

    @Autowired
    WebTestClient client;

}
