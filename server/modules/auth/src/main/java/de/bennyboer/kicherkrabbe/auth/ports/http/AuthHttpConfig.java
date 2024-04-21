package de.bennyboer.kicherkrabbe.auth.ports.http;

import de.bennyboer.kicherkrabbe.auth.AuthModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.HttpMethod.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class AuthHttpConfig {

    @Bean
    public AuthHttpHandler authHttpHandler(AuthModule module) {
        return new AuthHttpHandler(module);
    }

    @Bean
    public RouterFunction<ServerResponse> authHttpRouting(AuthHttpHandler handler) {
        return route()
                .nest(path("/auth"), () -> route()
                        .nest(path("/credentials"), () -> route()
                                .POST("/use", handler::useCredentials)
                                .POST("/test", handler::test) // TODO Remove
                                .build())
                        .build())
                .build();
    }

    @Bean
    public Customizer<ServerHttpSecurity.AuthorizeExchangeSpec> authAuthorizeExchangeSpecCustomizer() {
        return exchanges -> exchanges
                .pathMatchers(POST, "/auth/credentials/use").permitAll()
                .pathMatchers(POST, "/auth/credentials/test").permitAll(); // TODO Remove
    }

}
