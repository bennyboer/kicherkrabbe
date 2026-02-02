package de.bennyboer.kicherkrabbe.notifications.http;

import de.bennyboer.kicherkrabbe.notifications.NotificationsModule;
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
public class NotificationsHttpConfig {

    @Bean
    public NotificationsHttpHandler notificationsHttpHandler(
            NotificationsModule module,
            ReactiveTransactionManager transactionManager
    ) {
        return new NotificationsHttpHandler(module, transactionManager);
    }

    @Bean
    public RouterFunction<ServerResponse> notificationsHttpRouting(NotificationsHttpHandler handler) {
        return nest(
                path("/notifications"),
                route(GET(""), handler::getNotifications)
                        .andNest(path("/settings"), buildSettingsRoutes(handler))
        );
    }

    private RouterFunction<ServerResponse> buildSettingsRoutes(NotificationsHttpHandler handler) {
        // /notifications/settings
        return route(GET(""), handler::getSettings)
                .andNest(path("/system"), buildSystemSettingsRoutes(handler));
    }

    private RouterFunction<ServerResponse> buildSystemSettingsRoutes(NotificationsHttpHandler handler) {
        // /notifications/settings/system
        return route(POST("/enable"), handler::enableSystemNotifications)
                .andRoute(POST("/disable"), handler::disableSystemNotifications)
                .andNest(path("/channels"), buildSystemSettingsChannelsRoutes(handler));
    }

    private RouterFunction<ServerResponse> buildSystemSettingsChannelsRoutes(NotificationsHttpHandler handler) {
        // /notifications/settings/system/channels
        return route(POST("/update"), handler::updateSystemNotificationChannel)
                .andRoute(POST("/activate"), handler::activateSystemNotificationChannel)
                .andRoute(POST("/deactivate"), handler::deactivateSystemNotificationChannel);
    }

    @Bean
    public Customizer<ServerHttpSecurity.AuthorizeExchangeSpec> notificationsAuthorizeExchangeSpecCustomizer() {
        return exchanges -> {
        };
    }

}
