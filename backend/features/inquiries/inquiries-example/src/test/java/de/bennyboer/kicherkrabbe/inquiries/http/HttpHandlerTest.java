package de.bennyboer.kicherkrabbe.inquiries.http;

import de.bennyboer.kicherkrabbe.auth.SecurityConfig;
import de.bennyboer.kicherkrabbe.auth.tokens.Owner;
import de.bennyboer.kicherkrabbe.auth.tokens.OwnerId;
import de.bennyboer.kicherkrabbe.auth.tokens.TokenGenerator;
import de.bennyboer.kicherkrabbe.auth.tokens.TokenPayload;
import de.bennyboer.kicherkrabbe.inquiries.InquiriesModule;
import de.bennyboer.kicherkrabbe.testing.time.TestClock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Clock;
import java.time.Instant;

@WebFluxTest
@ContextConfiguration(classes = {
        InquiriesHttpConfig.class,
        SecurityConfig.class,
        HttpTestConfig.class,
        HttpHandlerTest.TimeTestConfig.class
})
public class HttpHandlerTest {

    @Autowired
    WebTestClient client;

    @Autowired
    TokenGenerator tokenGenerator;

    @MockitoBean
    InquiriesModule module;

    public String createTokenForUser(String userId) {
        return tokenGenerator.generate(TokenPayload.of(Owner.of(OwnerId.of(userId)))).block().getValue();
    }

    public void setTime(Instant instant) {
        TimeTestConfig.CLOCK.setNow(instant);
    }

    @Configuration
    public static class TimeTestConfig {

        public static TestClock CLOCK = new TestClock();

        @Bean
        public Clock getClock() {
            return CLOCK;
        }

    }

}
