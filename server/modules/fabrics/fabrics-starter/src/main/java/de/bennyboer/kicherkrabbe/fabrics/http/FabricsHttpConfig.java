package de.bennyboer.kicherkrabbe.fabrics.http;

import de.bennyboer.kicherkrabbe.fabrics.FabricsModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class FabricsHttpConfig {

    @Bean
    public FabricsHttpHandler fabricsHttpHandler(FabricsModule module, ReactiveTransactionManager transactionManager) {
        return new FabricsHttpHandler(module, transactionManager);
    }

    @Bean
    public RouterFunction<ServerResponse> fabricsHttpRouting(FabricsHttpHandler handler) {
        return nest(
                path("/api/fabrics"),
                route(GET("/"), handler::getFabrics)
                        .andRoute(GET("/published"), handler::getPublishedFabrics)
                        .andRoute(POST("/create"), handler::createFabric)
                        .andNest(path("/{fabricId}"), route(GET("/"), handler::getFabric)
                                .andRoute(POST("/rename"), handler::renameFabric)
                                .andRoute(POST("/publish"), handler::publishFabric)
                                .andRoute(POST("/unpublish"), handler::unpublishFabric)
                                .andRoute(DELETE("/"), handler::deleteFabric)
                                .andNest(
                                        path("/update"),
                                        route(POST("/image"), handler::updateFabricImage)
                                                .andRoute(POST("/colors"), handler::updateFabricColors)
                                                .andRoute(POST("/topics"), handler::updateFabricTopics)
                                                .andRoute(
                                                        POST("/availability"),
                                                        handler::updateFabricAvailability
                                                )
                                ))
        );
    }

    @Bean
    public Customizer<ServerHttpSecurity.AuthorizeExchangeSpec> fabricsAuthorizeExchangeSpecCustomizer() {
        return exchanges -> exchanges.pathMatchers(HttpMethod.GET, "/api/fabrics/published").permitAll();
    }

}
