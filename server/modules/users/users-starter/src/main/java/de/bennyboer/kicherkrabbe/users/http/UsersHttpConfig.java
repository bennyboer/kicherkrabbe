package de.bennyboer.kicherkrabbe.users.http;

import de.bennyboer.kicherkrabbe.users.UsersModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class UsersHttpConfig {

    @Bean
    public UsersHttpHandler usersHttpHandler(UsersModule module) {
        return new UsersHttpHandler(module);
    }

    @Bean
    public RouterFunction<ServerResponse> usersHttpRouting(UsersHttpHandler handler) {
        return route()
                .nest(path("/api"), () -> route()
                        .nest(path("/users"), () -> route()
                                .GET("/me", handler::getLoggedInUserDetails)
                                .build())
                        .build()
                ).build();
    }

    @Bean
    public Customizer<ServerHttpSecurity.AuthorizeExchangeSpec> usersAuthorizeExchangeSpecCustomizer() {
        return exchanges -> {
        };
    }

}
