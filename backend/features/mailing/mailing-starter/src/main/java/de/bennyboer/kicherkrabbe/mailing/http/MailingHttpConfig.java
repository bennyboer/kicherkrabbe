package de.bennyboer.kicherkrabbe.mailing.http;

import de.bennyboer.kicherkrabbe.mailing.MailingModule;
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
public class MailingHttpConfig {

    @Bean
    public MailingHttpHandler mailingHttpHandler(
            MailingModule module,
            ReactiveTransactionManager transactionManager
    ) {
        return new MailingHttpHandler(module, transactionManager);
    }

    @Bean
    public RouterFunction<ServerResponse> mailingHttpRouting(MailingHttpHandler handler) {
        return nest(
                path("/mailing"),
                nest(path("/mails"), buildMailsHttpRouting(handler))
                        .andNest(path("/settings"), buildSettingsHttpRouting(handler))
        );
    }

    private RouterFunction<ServerResponse> buildMailsHttpRouting(MailingHttpHandler handler) {
        // /mailing/mails
        return route(GET(""), handler::getMails)
                .andNest(path("/{mailId}"), buildMailHttpRouting(handler));
    }

    private RouterFunction<ServerResponse> buildMailHttpRouting(MailingHttpHandler handler) {
        // /mailing/mails/{mailId}
        return route(GET(""), handler::getMail);
    }

    private RouterFunction<ServerResponse> buildSettingsHttpRouting(MailingHttpHandler handler) {
        // /mailing/settings
        return route(GET(""), handler::getSettings)
                .andNest(path("/rate-limit"), buildRateLimitSettingsHttpRouting(handler))
                .andNest(path("/mailgun"), buildMailgunSettingsHttpRouting(handler));
    }

    private RouterFunction<ServerResponse> buildRateLimitSettingsHttpRouting(MailingHttpHandler handler) {
        // /mailing/settings/rate-limit
        return route(POST("/update"), handler::updateRateLimitSettings);
    }

    private RouterFunction<ServerResponse> buildMailgunSettingsHttpRouting(MailingHttpHandler handler) {
        // /mailing/settings/mailgun
        return nest(path("/api-token"), buildMailgunApiTokenHttpRouting(handler));
    }

    private RouterFunction<ServerResponse> buildMailgunApiTokenHttpRouting(MailingHttpHandler handler) {
        // /mailing/settings/mailgun/api-token
        return route(POST("/update"), handler::updateMailgunApiToken)
                .andRoute(POST("/clear"), handler::clearMailgunApiToken);
    }

    @Bean
    public Customizer<ServerHttpSecurity.AuthorizeExchangeSpec> mailingAuthorizeExchangeSpecCustomizer() {
        return exchanges -> {
        };
    }

}
