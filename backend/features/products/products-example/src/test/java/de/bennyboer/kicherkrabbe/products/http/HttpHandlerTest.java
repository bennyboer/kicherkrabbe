package de.bennyboer.kicherkrabbe.products.http;

import de.bennyboer.kicherkrabbe.auth.SecurityConfig;
import de.bennyboer.kicherkrabbe.auth.tokens.Owner;
import de.bennyboer.kicherkrabbe.auth.tokens.OwnerId;
import de.bennyboer.kicherkrabbe.auth.tokens.TokenGenerator;
import de.bennyboer.kicherkrabbe.auth.tokens.TokenPayload;
import de.bennyboer.kicherkrabbe.products.ProductsModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest
@ContextConfiguration(classes = {
        ProductsHttpConfig.class,
        SecurityConfig.class,
        HttpTestConfig.class
})
public class HttpHandlerTest {

    @Autowired
    WebTestClient client;

    @Autowired
    TokenGenerator tokenGenerator;

    @MockitoBean
    ProductsModule module;

    public String createTokenForUser(String userId) {
        return tokenGenerator.generate(TokenPayload.of(Owner.of(OwnerId.of(userId)))).block().getValue();
    }

}
