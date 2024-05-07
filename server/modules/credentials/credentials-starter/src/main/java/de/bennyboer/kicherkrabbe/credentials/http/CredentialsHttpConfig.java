package de.bennyboer.kicherkrabbe.credentials.http;

import de.bennyboer.kicherkrabbe.credentials.CredentialsModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.HttpMethod.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class CredentialsHttpConfig {

    @Bean
    public CredentialsHttpHandler credentialsHttpHandler(
            CredentialsModule module,
            ReactiveTransactionManager transactionManager
    ) {
        return new CredentialsHttpHandler(module, transactionManager);
    }

    @Bean
    public RouterFunction<ServerResponse> credentialsHttpRouting(CredentialsHttpHandler handler) {
        return route()
                .nest(path("/api"), () -> route()
                        .nest(path("/credentials"), () -> route()
                                .POST("/use", handler::useCredentials)
                                .build())
                        .build()
                ).build();
    }

    @Bean
    public Customizer<ServerHttpSecurity.AuthorizeExchangeSpec> credentialsAuthorizeExchangeSpecCustomizer() {
        return exchanges -> exchanges
                .pathMatchers(POST, "/api/credentials/use").permitAll();
    }

}
