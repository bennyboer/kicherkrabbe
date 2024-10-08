package de.bennyboer.kicherkrabbe.inquiries.http;

import de.bennyboer.kicherkrabbe.auth.SecurityConfig;
import de.bennyboer.kicherkrabbe.auth.tokens.Owner;
import de.bennyboer.kicherkrabbe.auth.tokens.OwnerId;
import de.bennyboer.kicherkrabbe.auth.tokens.TokenGenerator;
import de.bennyboer.kicherkrabbe.auth.tokens.TokenPayload;
import de.bennyboer.kicherkrabbe.inquiries.InquiriesModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest
@ContextConfiguration(classes = {
        InquiriesHttpConfig.class,
        SecurityConfig.class,
        HttpTestConfig.class
})
public class HttpHandlerTest {

    @Autowired
    WebTestClient client;

    @Autowired
    TokenGenerator tokenGenerator;

    @MockBean
    InquiriesModule module;

    public String createTokenForUser(String userId) {
        return tokenGenerator.generate(TokenPayload.of(Owner.of(OwnerId.of(userId)))).block().getValue();
    }

}
