package de.bennyboer.kicherkrabbe.telegram.http;

import de.bennyboer.kicherkrabbe.telegram.TelegramModule;
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
public class TelegramHttpConfig {

    @Bean
    public TelegramHttpHandler telegramHttpHandler(
            TelegramModule module,
            ReactiveTransactionManager transactionManager
    ) {
        return new TelegramHttpHandler(module, transactionManager);
    }

    @Bean
    public RouterFunction<ServerResponse> telegramHttpRouting(TelegramHttpHandler handler) {
        return nest(
                path("/telegram"),
                nest(path("/settings"), buildSettingsHttpRouting(handler))
        );
    }

    private RouterFunction<ServerResponse> buildSettingsHttpRouting(TelegramHttpHandler handler) {
        return route(GET(""), handler::getSettings)
                .andNest(path("/bot"), buildBotSettingsHttpRouting(handler));
    }

    private RouterFunction<ServerResponse> buildBotSettingsHttpRouting(TelegramHttpHandler handler) {
        return nest(path("/api-token"), buildBotApiTokenHttpRouting(handler));
    }

    private RouterFunction<ServerResponse> buildBotApiTokenHttpRouting(TelegramHttpHandler handler) {
        return route(POST("/update"), handler::updateBotApiToken)
                .andRoute(POST("/clear"), handler::clearBotApiToken);
    }

    @Bean
    public Customizer<ServerHttpSecurity.AuthorizeExchangeSpec> telegramAuthorizeExchangeSpecCustomizer() {
        return exchanges -> {
        };
    }

}
