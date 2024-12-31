package de.bennyboer.kicherkrabbe.mailing.http;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.mailing.MailingModule;
import de.bennyboer.kicherkrabbe.mailing.api.requests.ClearMailgunApiTokenRequest;
import de.bennyboer.kicherkrabbe.mailing.api.requests.UpdateMailgunApiTokenRequest;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import lombok.AllArgsConstructor;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;

@AllArgsConstructor
public class MailingHttpHandler {

    private final MailingModule module;

    private final ReactiveTransactionManager transactionManager;

    public Mono<ServerResponse> getSettings(ServerRequest request) {
        return toAgent(request)
                .flatMap(module::getSettings)
                .flatMap(settings -> ServerResponse.ok().bodyValue(settings))
                .onErrorResume(MissingPermissionError.class, e -> ServerResponse.status(FORBIDDEN).build());
    }

    public Mono<ServerResponse> updateMailgunApiToken(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        return request.bodyToMono(UpdateMailgunApiTokenRequest.class)
                .flatMap(req -> toAgent(request)
                        .flatMap(agent -> module.updateMailgunApiToken(req, agent)))
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorResume(MissingPermissionError.class, e -> ServerResponse.status(FORBIDDEN).build())
                .onErrorResume(AggregateVersionOutdatedError.class, e -> ServerResponse.status(CONFLICT).build())
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> clearMailgunApiToken(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        return request.bodyToMono(ClearMailgunApiTokenRequest.class)
                .flatMap(req -> toAgent(request)
                        .flatMap(agent -> module.clearMailgunApiToken(req, agent)))
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorResume(MissingPermissionError.class, e -> ServerResponse.status(FORBIDDEN).build())
                .onErrorResume(AggregateVersionOutdatedError.class, e -> ServerResponse.status(CONFLICT).build())
                .as(transactionalOperator::transactional);
    }

    private Mono<Agent> toAgent(ServerRequest request) {
        return request.principal()
                .map(principal -> Agent.user(AgentId.of(principal.getName())))
                .switchIfEmpty(Mono.just(Agent.anonymous()));
    }

}
