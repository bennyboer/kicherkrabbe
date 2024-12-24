package de.bennyboer.kicherkrabbe.mailbox.http;

import de.bennyboer.kicherkrabbe.mailbox.MailboxModule;
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
public class MailboxHttpConfig {

    @Bean
    public MailboxHttpHandler mailboxHttpHandler(
            MailboxModule module,
            ReactiveTransactionManager transactionManager
    ) {
        return new MailboxHttpHandler(module, transactionManager);
    }

    @Bean
    public RouterFunction<ServerResponse> mailboxHttpRouting(MailboxHttpHandler handler) {
        return nest(
                path("/api/mailbox"),
                nest(
                        path("/mails"),
                        route(GET(""), handler::getMails)
                                .andRoute(GET("/unread/count"), handler::getUnreadMailsCount)
                                .andNest(
                                        path("/{mailId}"),
                                        route(GET(""), handler::getMail)
                                                .andRoute(DELETE(""), handler::deleteMail)
                                                .andRoute(POST("/read"), handler::markMailAsRead)
                                                .andRoute(POST("/unread"), handler::markMailAsUnread)
                                )
                )
        );
    }

    @Bean
    public Customizer<ServerHttpSecurity.AuthorizeExchangeSpec> mailboxAuthorizeExchangeSpecCustomizer() {
        return exchanges -> {
        };
    }

}
