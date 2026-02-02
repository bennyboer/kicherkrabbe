package de.bennyboer.kicherkrabbe.inquiries.http;

import de.bennyboer.kicherkrabbe.inquiries.InquiriesModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.time.Clock;
import java.util.Optional;

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
            ReactiveTransactionManager transactionManager,
            Optional<Clock> clock
    ) {
        return new InquiriesHttpHandler(module, transactionManager, clock.orElse(Clock.systemUTC()));
    }

    @Bean
    public RouterFunction<ServerResponse> inquiriesHttpRouting(InquiriesHttpHandler handler) {
        return nest(
                path("/inquiries"),
                route(POST("/send"), handler::sendInquiry)
                        .andRoute(GET("/status"), handler::getStatus)
                        .andRoute(GET("/statistics"), handler::getStatistics)
                        .andNest(
                                path("/settings"),
                                route(GET(""), handler::getSettings)
                                        .andRoute(POST("/enable"), handler::enable)
                                        .andRoute(POST("/disable"), handler::disable)
                                        .andRoute(POST("/rate-limits"), handler::updateRateLimits)
                        )
        );
    }

    @Bean
    public Customizer<ServerHttpSecurity.AuthorizeExchangeSpec> inquiriesAuthorizeExchangeSpecCustomizer() {
        return exchanges -> exchanges
                .pathMatchers(POST, "/inquiries/send").permitAll()
                .pathMatchers(GET, "/inquiries/status").permitAll();
    }

}
