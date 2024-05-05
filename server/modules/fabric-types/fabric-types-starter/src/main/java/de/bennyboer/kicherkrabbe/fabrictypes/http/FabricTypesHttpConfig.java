package de.bennyboer.kicherkrabbe.fabrictypes.http;

import de.bennyboer.kicherkrabbe.fabrictypes.FabricTypesModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class FabricTypesHttpConfig {

    @Bean
    public FabricTypesHttpHandler fabricTypesHttpHandler(FabricTypesModule module) {
        return new FabricTypesHttpHandler(module);
    }

    @Bean
    public RouterFunction<ServerResponse> fabricTypesHttpRouting(FabricTypesHttpHandler handler) {
        return nest(
                path("/api/fabric-types"),
                route(GET("/"), handler::getFabricTypes)
                        .andRoute(POST("/create"), handler::createFabricType)
                        .andNest(
                                path("/{typeId}"),
                                route(POST("/update"), handler::updateFabricType)
                                        .andRoute(DELETE("/"), handler::deleteFabricType)
                        )
        );
    }

    @Bean
    public Customizer<ServerHttpSecurity.AuthorizeExchangeSpec> fabricTypesAuthorizeExchangeSpecCustomizer() {
        return exchanges -> {
        };
    }

}
