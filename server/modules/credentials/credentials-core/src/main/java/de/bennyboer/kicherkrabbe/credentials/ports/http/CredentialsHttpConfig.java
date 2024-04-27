package de.bennyboer.kicherkrabbe.credentials.ports.http;

import de.bennyboer.kicherkrabbe.credentials.CredentialsModule;
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
public class CredentialsHttpConfig {

    @Bean
    public CredentialsHttpHandler authHttpHandler(CredentialsModule module) {
        return new CredentialsHttpHandler(module);
    }

    @Bean
    public RouterFunction<ServerResponse> authHttpRouting(CredentialsHttpHandler handler) {
        return route()
                .nest(path("/auth"), () -> route()
                        .nest(path("/credentials"), () -> route()
                                .POST("/use", handler::useCredentials)
                                .build())
                        .build())
                .build();
    }

    @Bean
    public Customizer<ServerHttpSecurity.AuthorizeExchangeSpec> authAuthorizeExchangeSpecCustomizer() {
        return exchanges -> exchanges
                .pathMatchers(POST, "/auth/credentials/use").permitAll();
    }

}
