package de.bennyboer.kicherkrabbe.auth.ports.http;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.HttpMethod.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class HttpConfig {

    @Bean
    public HttpHandler authHttpHandler() {
        return new HttpHandler();
    }

    @Bean
    public RouterFunction<ServerResponse> authHttpRouting(HttpHandler handler) {
        return route()
                .nest(path("/auth"), () -> route()
                        .nest(path("/credentials"), () -> route()
                                .POST("/use", handler::useCredentials)
                                .build())
                        .build())
                .build();
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .securityMatcher(new PathPatternParserServerWebExchangeMatcher("/auth/**"))
                .authorizeExchange((exchanges) -> exchanges
                        .pathMatchers(POST, "/auth/credentials/use").permitAll()
                        .anyExchange().authenticated());

        return http.build();
    }

}
