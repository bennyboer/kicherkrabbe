package de.bennyboer.kicherkrabbe.topics.http;

import de.bennyboer.kicherkrabbe.topics.TopicsModule;
import lombok.AllArgsConstructor;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@AllArgsConstructor
public class TopicsHttpHandler {

    private final TopicsModule module;

    public Mono<ServerResponse> getTopics(ServerRequest request) {
        return Mono.empty(); // TODO
    }

    public Mono<ServerResponse> createTopic(ServerRequest request) {
        return Mono.empty(); // TODO
    }

    public Mono<ServerResponse> updateTopic(ServerRequest request) {
        return Mono.empty(); // TODO
    }

    public Mono<ServerResponse> deleteTopic(ServerRequest request) {
        return Mono.empty(); // TODO
    }

}
