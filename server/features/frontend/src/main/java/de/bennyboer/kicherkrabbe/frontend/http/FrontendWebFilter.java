package de.bennyboer.kicherkrabbe.frontend.http;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.MediaType.TEXT_HTML;

@Component
public class FrontendWebFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest()
                .getURI()
                .getPath();

        boolean isApi = path.startsWith("/api/");
        if (isApi) {
            return chain.filter(exchange);
        }

        boolean isGetMethod = exchange.getRequest().getMethod() == GET;
        if (!isGetMethod) {
            return chain.filter(exchange);
        }

        List<MediaType> accept = exchange.getRequest()
                .getHeaders()
                .getAccept();
        if (accept.contains(TEXT_HTML)) {
            return chain.filter(exchange.mutate()
                    .request(exchange.getRequest().mutate().path("/index.html").build())
                    .build());
        }

        return chain.filter(exchange);
    }

}