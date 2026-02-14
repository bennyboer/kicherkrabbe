package de.bennyboer.kicherkrabbe.credentials.http;

import de.bennyboer.kicherkrabbe.credentials.CredentialsModule;
import de.bennyboer.kicherkrabbe.credentials.http.api.requests.LogoutRequest;
import de.bennyboer.kicherkrabbe.credentials.http.api.requests.RefreshTokenRequest;
import de.bennyboer.kicherkrabbe.credentials.http.api.requests.UseCredentialsRequest;
import de.bennyboer.kicherkrabbe.credentials.http.api.responses.RefreshTokenResponse;
import de.bennyboer.kicherkrabbe.credentials.http.api.responses.UseCredentialsResponse;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Slf4j
@AllArgsConstructor
public class CredentialsHttpHandler {

    private final CredentialsModule module;

    private final ReactiveTransactionManager transactionManager;

    public Mono<ServerResponse> useCredentials(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        return request.bodyToMono(UseCredentialsRequest.class)
                .flatMap(req -> module.useCredentials(req.name, req.password, Agent.anonymous()))
                .map(result -> {
                    var response = new UseCredentialsResponse();
                    response.token = result.getAccessToken();
                    response.refreshToken = result.getRefreshToken();
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorResume(e -> ServerResponse.status(UNAUTHORIZED).build())
                .switchIfEmpty(ServerResponse.status(UNAUTHORIZED).build())
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> refreshToken(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        return request.bodyToMono(RefreshTokenRequest.class)
                .flatMap(req -> module.refreshTokens(req.refreshToken))
                .map(result -> {
                    var response = new RefreshTokenResponse();
                    response.token = result.getAccessToken();
                    response.refreshToken = result.getRefreshToken();
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorResume(e -> ServerResponse.status(UNAUTHORIZED).build())
                .switchIfEmpty(ServerResponse.status(UNAUTHORIZED).build())
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> logout(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        return request.bodyToMono(LogoutRequest.class)
                .flatMap(req -> module.logout(req.refreshToken))
                .then(ServerResponse.ok().build())
                .onErrorResume(e -> {
                    log.warn("Error during logout", e);
                    return ServerResponse.ok().build();
                })
                .as(transactionalOperator::transactional);
    }

}
