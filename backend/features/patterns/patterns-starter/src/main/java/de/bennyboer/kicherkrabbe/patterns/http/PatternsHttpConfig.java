package de.bennyboer.kicherkrabbe.patterns.http;

import de.bennyboer.kicherkrabbe.patterns.PatternsModule;
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
public class PatternsHttpConfig {

    @Bean
    public PatternsHttpHandler patternsHttpHandler(
            PatternsModule module,
            ReactiveTransactionManager transactionManager
    ) {
        return new PatternsHttpHandler(module, transactionManager);
    }

    @Bean
    public RouterFunction<ServerResponse> patternsHttpRouting(PatternsHttpHandler handler) {
        return nest(
                path("/patterns"),
                route(POST(""), handler::getPatterns)
                        .andRoute(GET("/changes"), handler::getChanges)
                        .andRoute(POST("/published"), handler::getPublishedPatterns)
                        .andRoute(GET("/featured"), handler::getFeaturedPatterns)
                        .andRoute(GET("/categories"), handler::getAvailableCategoriesForPatterns)
                        .andRoute(GET("/categories/used"), handler::getCategoriesUsedInPatterns)
                        .andRoute(POST("/create"), handler::createPattern)
                        .andNest(path("/{patternId}"), route(GET(""), handler::getPattern)
                                .andRoute(GET("/published"), handler::getPublishedPattern)
                                .andRoute(POST("/rename"), handler::renamePattern)
                                .andRoute(POST("/publish"), handler::publishPattern)
                                .andRoute(POST("/unpublish"), handler::unpublishPattern)
                                .andRoute(POST("/feature"), handler::featurePattern)
                                .andRoute(POST("/unfeature"), handler::unfeaturePattern)
                                .andRoute(DELETE(""), handler::deletePattern)
                                .andNest(
                                        path("/update"),
                                        route(POST("/attribution"), handler::updatePatternAttribution)
                                                .andRoute(POST("/categories"), handler::updatePatternCategories)
                                                .andRoute(POST("/images"), handler::updatePatternImages)
                                                .andRoute(POST("/variants"), handler::updatePatternVariants)
                                                .andRoute(POST("/extras"), handler::updatePatternExtras)
                                                .andRoute(POST("/description"), handler::updatePatternDescription)
                                                .andRoute(POST("/number"), handler::updatePatternNumber)
                                ))
        );
    }

    @Bean
    public Customizer<ServerHttpSecurity.AuthorizeExchangeSpec> patternsAuthorizeExchangeSpecCustomizer() {
        return exchanges -> exchanges.pathMatchers(POST, "/patterns/published").permitAll()
                .pathMatchers(GET, "/patterns/featured").permitAll()
                .pathMatchers(GET, "/patterns/{patternId}/published").permitAll()
                .pathMatchers(GET, "/patterns/categories/used").permitAll();
    }

}
