package de.bennyboer.kicherkrabbe.colors.http;

import de.bennyboer.kicherkrabbe.colors.ColorsModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class ColorsHttpConfig {

    @Bean
    public ColorsHttpHandler colorsHttpHandler(ColorsModule module, ReactiveTransactionManager transactionManager) {
        return new ColorsHttpHandler(module, transactionManager);
    }

    @Bean
    public RouterFunction<ServerResponse> colorsHttpRouting(ColorsHttpHandler handler) {
        return nest(
                path("/api/colors"),
                route(GET("/"), handler::getColors)
                        .andRoute(GET("/changes"), handler::getColorChanges)
                        .andRoute(POST("/create"), handler::createColor)
                        .andNest(
                                path("/{colorId}"),
                                route(POST("/update"), handler::updateColor)
                                        .andRoute(DELETE("/"), handler::deleteColor)
                        )
        );
    }

    @Bean
    public Customizer<ServerHttpSecurity.AuthorizeExchangeSpec> colorsAuthorizeExchangeSpecCustomizer() {
        return exchanges -> {
        };
    }

}
