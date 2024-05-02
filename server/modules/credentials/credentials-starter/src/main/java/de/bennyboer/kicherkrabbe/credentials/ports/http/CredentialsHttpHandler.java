package de.bennyboer.kicherkrabbe.credentials.ports.http;

import de.bennyboer.kicherkrabbe.credentials.CredentialsModule;
import de.bennyboer.kicherkrabbe.credentials.ports.http.requests.UseCredentialsRequest;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import lombok.AllArgsConstructor;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@AllArgsConstructor
public class CredentialsHttpHandler {

    private final CredentialsModule module;

    public Mono<ServerResponse> useCredentials(ServerRequest request) {
        return request.bodyToMono(UseCredentialsRequest.class)
                .flatMap(req -> module.useCredentials(req.name, req.password, Agent.anonymous()))
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorResume(e -> ServerResponse.status(UNAUTHORIZED).build())
                .switchIfEmpty(ServerResponse.status(UNAUTHORIZED).build());
    }

}
