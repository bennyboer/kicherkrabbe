package de.bennyboer.kicherkrabbe.fabrics.http;

import de.bennyboer.kicherkrabbe.fabrics.FabricsModule;
import lombok.AllArgsConstructor;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@AllArgsConstructor
public class FabricsHttpHandler {

    private final FabricsModule module;

    public Mono<ServerResponse> getFabrics(ServerRequest request) {
        return Mono.empty(); // TODO
    }

    public Mono<ServerResponse> getFabric(ServerRequest request) {
        return Mono.empty(); // TODO
    }

    public Mono<ServerResponse> createFabric(ServerRequest request) {
        return Mono.empty(); // TODO
    }

    public Mono<ServerResponse> renameFabric(ServerRequest request) {
        return Mono.empty(); // TODO
    }

    public Mono<ServerResponse> publishFabric(ServerRequest request) {
        return Mono.empty(); // TODO
    }

    public Mono<ServerResponse> unpublishFabric(ServerRequest request) {
        return Mono.empty(); // TODO
    }

    public Mono<ServerResponse> updateFabricImage(ServerRequest request) {
        return Mono.empty(); // TODO
    }

    public Mono<ServerResponse> updateFabricColors(ServerRequest request) {
        return Mono.empty(); // TODO
    }

    public Mono<ServerResponse> updateFabricThemes(ServerRequest request) {
        return Mono.empty(); // TODO
    }

    public Mono<ServerResponse> updateFabricAvailability(ServerRequest request) {
        return Mono.empty(); // TODO
    }

    public Mono<ServerResponse> deleteFabric(ServerRequest request) {
        return Mono.empty(); // TODO
    }

}
