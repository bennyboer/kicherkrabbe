package de.bennyboer.kicherkrabbe.credentials.http;

import de.bennyboer.kicherkrabbe.credentials.CredentialsModule;
import de.bennyboer.kicherkrabbe.credentials.http.api.requests.UseCredentialsRequest;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import lombok.AllArgsConstructor;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@AllArgsConstructor
public class CredentialsHttpHandler {

    private final CredentialsModule module;

    private final ReactiveTransactionManager transactionManager;

    public Mono<ServerResponse> useCredentials(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        return request.bodyToMono(UseCredentialsRequest.class)
                .flatMap(req -> module.useCredentials(req.name, req.password, Agent.anonymous()))
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorResume(e -> ServerResponse.status(UNAUTHORIZED).build())
                .switchIfEmpty(ServerResponse.status(UNAUTHORIZED).build())
                .as(transactionalOperator::transactional);
    }

}
