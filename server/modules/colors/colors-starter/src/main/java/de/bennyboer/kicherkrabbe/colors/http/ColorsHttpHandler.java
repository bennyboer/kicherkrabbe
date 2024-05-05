package de.bennyboer.kicherkrabbe.colors.http;

import de.bennyboer.kicherkrabbe.colors.ColorsModule;
import lombok.AllArgsConstructor;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@AllArgsConstructor
public class ColorsHttpHandler {

    private final ColorsModule module;

    public Mono<ServerResponse> getColors(ServerRequest request) {
        return Mono.empty(); // TODO
    }

    public Mono<ServerResponse> createColor(ServerRequest request) {
        return Mono.empty(); // TODO
    }

    public Mono<ServerResponse> updateColor(ServerRequest request) {
        return Mono.empty(); // TODO
    }

    public Mono<ServerResponse> deleteColor(ServerRequest request) {
        return Mono.empty(); // TODO
    }

}
