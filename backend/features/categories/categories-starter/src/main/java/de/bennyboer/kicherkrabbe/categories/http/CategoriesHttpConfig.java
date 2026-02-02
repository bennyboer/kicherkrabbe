package de.bennyboer.kicherkrabbe.categories.http;

import de.bennyboer.kicherkrabbe.categories.CategoriesModule;
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
public class CategoriesHttpConfig {

    @Bean
    public CategoriesHttpHandler categoriesHttpHandler(
            CategoriesModule module,
            ReactiveTransactionManager transactionManager
    ) {
        return new CategoriesHttpHandler(module, transactionManager);
    }

    @Bean
    public RouterFunction<ServerResponse> categoriesHttpRouting(CategoriesHttpHandler handler) {
        return nest(
                path("/categories"),
                route(GET(""), handler::getCategories)
                        .andRoute(GET("/groups/{group}"), handler::getCategoriesByGroup)
                        .andRoute(GET("/changes"), handler::getChanges)
                        .andRoute(POST("/create"), handler::createCategory)
                        .andNest(
                                path("/{categoryId}"),
                                route(GET(""), handler::getCategory)
                                        .andRoute(POST("/rename"), handler::renameCategory)
                                        .andRoute(POST("/regroup"), handler::regroupCategory)
                                        .andRoute(DELETE(""), handler::deleteCategory)
                        )
        );
    }

    @Bean
    public Customizer<ServerHttpSecurity.AuthorizeExchangeSpec> categoriesAuthorizeExchangeSpecCustomizer() {
        return exchanges -> {
        };
    }

}
