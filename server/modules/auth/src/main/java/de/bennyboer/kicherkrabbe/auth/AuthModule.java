package de.bennyboer.kicherkrabbe.auth;

import de.bennyboer.kicherkrabbe.auth.ports.http.HttpHandler;
import de.bennyboer.kicherkrabbe.auth.ports.http.HttpConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;

import static org.springframework.http.HttpMethod.POST;

@Configuration
@Import({
        HttpConfig.class,
        HttpHandler.class
})
public class AuthModule {

}
