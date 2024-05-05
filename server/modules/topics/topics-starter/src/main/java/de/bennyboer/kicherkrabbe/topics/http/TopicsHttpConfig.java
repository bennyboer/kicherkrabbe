package de.bennyboer.kicherkrabbe.topics.http;

import de.bennyboer.kicherkrabbe.topics.TopicsModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class TopicsHttpConfig {

    @Bean
    public TopicsHttpHandler topicsHttpHandler(TopicsModule module) {
        return new TopicsHttpHandler(module);
    }

    @Bean
    public RouterFunction<ServerResponse> topicsHttpRouting(TopicsHttpHandler handler) {
        return nest(
                path("/api/topics"),
                route(GET("/"), handler::getTopics)
                        .andRoute(POST("/create"), handler::createTopic)
                        .andNest(
                                path("/{topicId}"),
                                route(POST("/update"), handler::updateTopic)
                                        .andRoute(DELETE("/"), handler::deleteTopic)
                        )
        );
    }

    @Bean
    public Customizer<ServerHttpSecurity.AuthorizeExchangeSpec> topicsAuthorizeExchangeSpecCustomizer() {
        return exchanges -> {
        };
    }

}
