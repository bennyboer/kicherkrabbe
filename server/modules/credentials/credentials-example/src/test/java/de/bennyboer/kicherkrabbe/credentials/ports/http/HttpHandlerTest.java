package de.bennyboer.kicherkrabbe.credentials.ports.http;

import de.bennyboer.kicherkrabbe.auth.SecurityConfig;
import de.bennyboer.kicherkrabbe.credentials.CredentialsModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest
@ContextConfiguration(classes = {
        CredentialsHttpConfig.class,
        SecurityConfig.class
})
public class HttpHandlerTest {

    @Autowired
    WebTestClient client;

    @MockBean
    CredentialsModule module;

}
