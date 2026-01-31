package de.bennyboer.kicherkrabbe.fabrics.http;

import de.bennyboer.kicherkrabbe.fabrics.FabricsModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
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
                route(POST("/"), handler::getFabrics)
                        .andRoute(GET("/changes"), handler::getChanges)
                        .andRoute(POST("/published"), handler::getPublishedFabrics)
                        .andRoute(GET("/featured"), handler::getFeaturedFabrics)
                        .andRoute(GET("/topics"), handler::getAvailableTopicsForFabrics)
                        .andRoute(GET("/topics/used"), handler::getTopicsUsedInFabrics)
                        .andRoute(GET("/colors"), handler::getAvailableColorsForFabrics)
                        .andRoute(GET("/colors/used"), handler::getColorsUsedInFabrics)
                        .andRoute(GET("/fabric-types"), handler::getAvailableFabricTypesForFabrics)
                        .andRoute(GET("/fabric-types/used"), handler::getFabricTypesUsedInFabrics)
                        .andRoute(POST("/create"), handler::createFabric)
                        .andNest(path("/{fabricId}"), route(GET("/"), handler::getFabric)
                                .andRoute(GET("/published"), handler::getPublishedFabric)
                                .andRoute(POST("/rename"), handler::renameFabric)
                                .andRoute(POST("/publish"), handler::publishFabric)
                                .andRoute(POST("/unpublish"), handler::unpublishFabric)
                                .andRoute(POST("/feature"), handler::featureFabric)
                                .andRoute(POST("/unfeature"), handler::unfeatureFabric)
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
        return exchanges -> exchanges.pathMatchers(POST, "/api/fabrics/published").permitAll()
                .pathMatchers(GET, "/api/fabrics/featured").permitAll()
                .pathMatchers(GET, "/api/fabrics/{fabricId}/published").permitAll()
                .pathMatchers(GET, "/api/fabrics/topics/used").permitAll()
                .pathMatchers(GET, "/api/fabrics/colors/used").permitAll()
                .pathMatchers(GET, "/api/fabrics/fabric-types/used").permitAll();
    }

}
