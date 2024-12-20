package de.bennyboer.kicherkrabbe.inquiries.http;

import de.bennyboer.kicherkrabbe.inquiries.InquiriesModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class InquiriesHttpConfig {

    @Bean
    public InquiriesHttpHandler inquiriesHttpHandler(
            InquiriesModule module,
            ReactiveTransactionManager transactionManager
    ) {
        return new InquiriesHttpHandler(module, transactionManager);
    }

    @Bean
    public RouterFunction<ServerResponse> inquiriesHttpRouting(InquiriesHttpHandler handler) {
        return nest(
                path("/api/inquiries"),
                route(POST("/send"), handler::sendInquiry)
                        .andRoute(GET("/status"), handler::getStatus)
        );
    }

    @Bean
    public Customizer<ServerHttpSecurity.AuthorizeExchangeSpec> inquiriesAuthorizeExchangeSpecCustomizer() {
        return exchanges -> exchanges
                .pathMatchers(POST, "/api/inquiries/send").permitAll()
                .pathMatchers(GET, "/api/inquiries/status").permitAll();
    }

}
