package de.bennyboer.kicherkrabbe.assets.http;

import de.bennyboer.kicherkrabbe.assets.AssetsModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class AssetsHttpConfig {

    @Bean
    public AssetsHttpHandler assetsHttpHandler(AssetsModule module, ReactiveTransactionManager transactionManager) {
        return new AssetsHttpHandler(module, transactionManager);
    }

    @Bean
    public RouterFunction<ServerResponse> assetsHttpRouting(AssetsHttpHandler handler) {
        return nest(
                path("/assets"),
                route(POST("/upload"), handler::uploadAsset)
                        .andNest(
                                path("/{assetId}"),
                                route(GET("/content"), handler::getAssetContent)
                                        .andRoute(DELETE("/"), handler::deleteAsset)
                        )
        );
    }

    @Bean
    public Customizer<ServerHttpSecurity.AuthorizeExchangeSpec> assetsAuthorizeExchangeSpecCustomizer() {
        return exchanges -> exchanges.pathMatchers(GET, "/assets/{assetId}/content").permitAll();
    }

}
