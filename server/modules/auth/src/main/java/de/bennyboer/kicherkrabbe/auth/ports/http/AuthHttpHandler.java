package de.bennyboer.kicherkrabbe.auth.ports.http;

import de.bennyboer.kicherkrabbe.auth.AuthModule;
import de.bennyboer.kicherkrabbe.auth.ports.http.requests.UseCredentialsRequest;
import lombok.AllArgsConstructor;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@AllArgsConstructor
public class AuthHttpHandler {

    private final AuthModule module;

    public Mono<ServerResponse> useCredentials(ServerRequest request) {
        return request.bodyToMono(UseCredentialsRequest.class)
                .flatMap(req -> module.useCredentials(req.name, req.password))
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorResume(e -> ServerResponse.status(UNAUTHORIZED).build())
                .switchIfEmpty(ServerResponse.status(UNAUTHORIZED).build());
    }

}
