package de.bennyboer.kicherkrabbe.embroideries.http;

import de.bennyboer.kicherkrabbe.embroideries.EmbroideriesModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class EmbroideriesHttpConfig {

    @Bean
    public EmbroideriesHttpHandler embroideriesHttpHandler(
            EmbroideriesModule module,
            ReactiveTransactionManager transactionManager
    ) {
        return new EmbroideriesHttpHandler(module, transactionManager);
    }

    @Bean
    public RouterFunction<ServerResponse> embroideriesHttpRouting(EmbroideriesHttpHandler handler) {
        return nest(
                path("/embroideries"),
                route(POST(""), handler::getEmbroideries)
                        .andRoute(GET("/changes"), handler::getChanges)
                        .andRoute(GET("/categories"), handler::getAvailableCategoriesForEmbroideries)
                        .andRoute(POST("/create"), handler::createEmbroidery)
                        .andNest(path("/{embroideryId}"), route(GET(""), handler::getEmbroidery)
                                .andRoute(POST("/rename"), handler::renameEmbroidery)
                                .andRoute(POST("/publish"), handler::publishEmbroidery)
                                .andRoute(POST("/unpublish"), handler::unpublishEmbroidery)
                                .andRoute(POST("/feature"), handler::featureEmbroidery)
                                .andRoute(POST("/unfeature"), handler::unfeatureEmbroidery)
                                .andRoute(DELETE(""), handler::deleteEmbroidery)
                                .andNest(
                                        path("/update"),
                                        route(POST("/image"), handler::updateEmbroideryImage)
                                                .andRoute(POST("/categories"), handler::updateEmbroideryCategories)
                                ))
        );
    }

}
