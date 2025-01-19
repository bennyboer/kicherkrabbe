package de.bennyboer.kicherkrabbe.products.http;

import de.bennyboer.kicherkrabbe.products.ProductsModule;
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
public class ProductsHttpConfig {

    @Bean
    public ProductsHttpHandler productsHttpHandler(
            ProductsModule module,
            ReactiveTransactionManager transactionManager
    ) {
        return new ProductsHttpHandler(module, transactionManager);
    }

    @Bean
    public RouterFunction<ServerResponse> productsHttpRouting(ProductsHttpHandler handler) {
        return nest(
                path("/api/products"),
                route(GET(""), handler::getProducts)
                        .andRoute(POST("/create"), handler::createProduct)
                        .andNest(path("/{productId}"), buildProductRoutes(handler))
                        .andNest(path("/links"), buildLinksRoutes(handler))
        );
    }

    private RouterFunction<ServerResponse> buildProductRoutes(ProductsHttpHandler handler) {
        // /api/products/{productId}
        return route(GET(""), handler::getProduct)
                .andRoute(DELETE(""), handler::deleteProduct)
                .andNest(path("/links"), buildProductLinksRoutes(handler))
                .andRoute(POST("/fabric-composition/update"), handler::updateFabricComposition)
                .andRoute(POST("/images/update"), handler::updateImages)
                .andRoute(POST("/notes/update"), handler::updateNotes)
                .andRoute(POST("/produced-at/update"), handler::updateProducedAt);
    }

    private RouterFunction<ServerResponse> buildLinksRoutes(ProductsHttpHandler handler) {
        // /api/products/links
        return route(GET(""), handler::getLinks);
    }

    private RouterFunction<ServerResponse> buildProductLinksRoutes(ProductsHttpHandler handler) {
        // /api/products/{productId}/links
        return route(POST("/add"), handler::addLinkToProduct)
                .andRoute(DELETE("/{linkType}/{linkId}"), handler::removeLinkFromProduct);
    }

    @Bean
    public Customizer<ServerHttpSecurity.AuthorizeExchangeSpec> productsAuthorizeExchangeSpecCustomizer() {
        return exchanges -> {
        };
    }

}
