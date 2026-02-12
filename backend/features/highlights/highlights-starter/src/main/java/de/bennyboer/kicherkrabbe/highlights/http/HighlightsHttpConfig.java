package de.bennyboer.kicherkrabbe.highlights.http;

import de.bennyboer.kicherkrabbe.highlights.HighlightsModule;
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
public class HighlightsHttpConfig {

    @Bean
    public HighlightsHttpHandler highlightsHttpHandler(
            HighlightsModule module,
            ReactiveTransactionManager transactionManager
    ) {
        return new HighlightsHttpHandler(module, transactionManager);
    }

    @Bean
    public RouterFunction<ServerResponse> highlightsHttpRouting(HighlightsHttpHandler handler) {
        return nest(
                path("/highlights"),
                route(GET("/published"), handler::getPublishedHighlights)
                        .andRoute(GET(""), handler::getHighlights)
                        .andRoute(GET("/changes"), handler::getChanges)
                        .andRoute(GET("/links"), handler::getLinks)
                        .andRoute(POST("/create"), handler::createHighlight)
                        .andNest(
                                path("/{highlightId}"),
                                route(GET(""), handler::getHighlight)
                                        .andRoute(POST("/update/image"), handler::updateImage)
                                        .andRoute(POST("/links/add"), handler::addLink)
                                        .andRoute(POST("/links/remove"), handler::removeLink)
                                        .andRoute(POST("/publish"), handler::publishHighlight)
                                        .andRoute(POST("/unpublish"), handler::unpublishHighlight)
                                        .andRoute(POST("/update/sort-order"), handler::updateSortOrder)
                                        .andRoute(DELETE(""), handler::deleteHighlight)
                        )
        );
    }

    @Bean
    public Customizer<ServerHttpSecurity.AuthorizeExchangeSpec> highlightsAuthorizeExchangeSpecCustomizer() {
        return exchanges -> exchanges.pathMatchers("/api/highlights/published").permitAll();
    }

}
