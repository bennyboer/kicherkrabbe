package de.bennyboer.kicherkrabbe.offers.http;

import de.bennyboer.kicherkrabbe.offers.OffersModule;
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
public class OffersHttpConfig {

    @Bean
    public OffersHttpHandler offersHttpHandler(
            OffersModule module,
            ReactiveTransactionManager transactionManager
    ) {
        return new OffersHttpHandler(module, transactionManager);
    }

    @Bean
    public RouterFunction<ServerResponse> offersHttpRouting(OffersHttpHandler handler) {
        return nest(
                path("/offers"),
                route(POST(""), handler::getOffers)
                        .andRoute(GET("/changes"), handler::getChanges)
                        .andRoute(POST("/published"), handler::getPublishedOffers)
                        .andRoute(POST("/products"), handler::getProducts)
                        .andRoute(GET("/categories"), handler::getAvailableCategoriesForOffers)
                        .andRoute(GET("/sizes"), handler::getAvailableSizesForOffers)
                        .andRoute(POST("/create"), handler::createOffer)
                        .andNest(path("/{offerId}"), route(GET(""), handler::getOffer)
                                .andRoute(GET("/published"), handler::getPublishedOffer)
                                .andRoute(DELETE(""), handler::deleteOffer)
                                .andRoute(POST("/publish"), handler::publishOffer)
                                .andRoute(POST("/unpublish"), handler::unpublishOffer)
                                .andRoute(POST("/reserve"), handler::reserveOffer)
                                .andRoute(POST("/unreserve"), handler::unreserveOffer)
                                .andRoute(POST("/archive"), handler::archiveOffer)
                                .andRoute(POST("/images/update"), handler::updateImages)
                                .andRoute(POST("/notes/update"), handler::updateNotes)
                                .andRoute(POST("/price/update"), handler::updatePrice)
                                .andRoute(POST("/title/update"), handler::updateTitle)
                                .andRoute(POST("/size/update"), handler::updateSize)
                                .andRoute(POST("/categories/update"), handler::updateCategories)
                                .andRoute(POST("/discount/add"), handler::addDiscount)
                                .andRoute(POST("/discount/remove"), handler::removeDiscount)
                        )
        );
    }

    @Bean
    public Customizer<ServerHttpSecurity.AuthorizeExchangeSpec> offersAuthorizeExchangeSpecCustomizer() {
        return exchanges -> exchanges
                .pathMatchers(POST, "/offers/published").permitAll()
                .pathMatchers(GET, "/offers/{offerId}/published").permitAll()
                .pathMatchers(GET, "/offers/categories").permitAll()
                .pathMatchers(GET, "/offers/sizes").permitAll();
    }

}
