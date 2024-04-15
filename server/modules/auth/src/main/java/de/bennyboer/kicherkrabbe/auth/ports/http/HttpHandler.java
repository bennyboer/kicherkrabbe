package de.bennyboer.kicherkrabbe.auth.ports.http;

import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public class HttpHandler {

    public Mono<ServerResponse> useCredentials(ServerRequest request) {
        return ServerResponse.ok().build(); // TODO
    }

}
