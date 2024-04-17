package de.bennyboer.kicherkrabbe.auth.ports.http;

import de.bennyboer.kicherkrabbe.auth.AuthModule;
import de.bennyboer.kicherkrabbe.auth.SecurityConfig;
import de.bennyboer.kicherkrabbe.testing.security.SecurityTestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest
@ContextConfiguration(classes = {
        AuthHttpConfig.class,
        SecurityConfig.class,
        SecurityTestConfig.class
})
public class HttpHandlerTest {

    @Autowired
    WebTestClient client;

    @MockBean
    AuthModule module;

}
