package de.bennyboer.kicherkrabbe.frontend.http;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.HEAD;
import static org.springframework.http.MediaType.ALL;
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

        HttpMethod method = exchange.getRequest().getMethod();
        boolean isGetOrHeadMethod = method == GET || method == HEAD;
        if (!isGetOrHeadMethod) {
            return chain.filter(exchange);
        }

        if (shouldRedirectToIndexHtml(exchange)) {
            return chain.filter(exchange.mutate()
                    .request(exchange.getRequest().mutate().path("/index.html").build())
                    .build());
        }

        return chain.filter(exchange);
    }

    private boolean shouldRedirectToIndexHtml(ServerWebExchange exchange) {
        List<MediaType> accept = exchange.getRequest()
                .getHeaders()
                .getAccept();
        if (accept.contains(TEXT_HTML)) {
            return true;
        }

        if (accept.contains(ALL)) {
            String path = exchange.getRequest()
                    .getURI()
                    .getPath();

            String lastPathPart = path.substring(path.lastIndexOf("/") + 1);
            int indexOfDot = lastPathPart.indexOf(".");
            if (indexOfDot == -1) {
                return true;
            }

            String fileEnding = lastPathPart.substring(lastPathPart.lastIndexOf(".") + 1);
            boolean hasFileEnding = !fileEnding.isEmpty();

            return !hasFileEnding;
        }

        return false;
    }

}