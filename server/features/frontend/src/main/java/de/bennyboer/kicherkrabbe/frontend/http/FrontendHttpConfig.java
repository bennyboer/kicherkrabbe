package de.bennyboer.kicherkrabbe.frontend.http;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;

import static org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher.MatchResult.match;
import static org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher.MatchResult.notMatch;

@Configuration
@Import({FrontendWebFilter.class})
public class FrontendHttpConfig {

    @Bean
    public Customizer<ServerHttpSecurity.AuthorizeExchangeSpec> frontendAuthorizeExchangeSpecCustomizer() {
        return exchanges -> exchanges.matchers(exchange -> {
            boolean startsWithApi = exchange.getRequest()
                    .getPath()
                    .value()
                    .startsWith("/api/");

            return startsWithApi ? notMatch() : match();
        }).permitAll();
    }

}
