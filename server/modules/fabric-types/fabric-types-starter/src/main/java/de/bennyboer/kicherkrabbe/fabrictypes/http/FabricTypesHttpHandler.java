package de.bennyboer.kicherkrabbe.fabrictypes.http;

import de.bennyboer.kicherkrabbe.fabrictypes.FabricTypesModule;
import lombok.AllArgsConstructor;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@AllArgsConstructor
public class FabricTypesHttpHandler {

    private final FabricTypesModule module;

    public Mono<ServerResponse> getFabricTypes(ServerRequest request) {
        return Mono.empty(); // TODO
    }

    public Mono<ServerResponse> createFabricType(ServerRequest request) {
        return Mono.empty(); // TODO
    }

    public Mono<ServerResponse> updateFabricType(ServerRequest request) {
        return Mono.empty(); // TODO
    }

    public Mono<ServerResponse> deleteFabricType(ServerRequest request) {
        return Mono.empty(); // TODO
    }

}
