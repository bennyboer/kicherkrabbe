package de.bennyboer.kicherkrabbe.users.ports.http;

import de.bennyboer.kicherkrabbe.auth.SecurityConfig;
import de.bennyboer.kicherkrabbe.auth.tokens.Owner;
import de.bennyboer.kicherkrabbe.auth.tokens.OwnerId;
import de.bennyboer.kicherkrabbe.auth.tokens.TokenGenerator;
import de.bennyboer.kicherkrabbe.auth.tokens.TokenPayload;
import de.bennyboer.kicherkrabbe.users.UsersModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest
@ContextConfiguration(classes = {
        UsersHttpConfig.class,
        SecurityConfig.class
})
public class HttpHandlerTest {

    @Autowired
    WebTestClient client;

    @Autowired
    TokenGenerator tokenGenerator;

    @MockBean
    UsersModule module;

    public String generateTokenForUser(String userId) {
        var ownerId = OwnerId.of(userId);
        var owner = Owner.of(ownerId);
        var tokenPayload = TokenPayload.of(owner);

        return tokenGenerator.generate(tokenPayload)
                .block()
                .getValue();
    }

}
