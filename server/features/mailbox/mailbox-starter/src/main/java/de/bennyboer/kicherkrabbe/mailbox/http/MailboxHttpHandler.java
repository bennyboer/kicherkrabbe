package de.bennyboer.kicherkrabbe.mailbox.http;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.mailbox.MailboxModule;
import de.bennyboer.kicherkrabbe.mailbox.api.StatusDTO;
import de.bennyboer.kicherkrabbe.mailbox.api.requests.MarkMailAsReadRequest;
import de.bennyboer.kicherkrabbe.mailbox.api.requests.MarkMailAsUnreadRequest;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import lombok.AllArgsConstructor;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpStatus.FORBIDDEN;

@AllArgsConstructor
public class MailboxHttpHandler {

    private final MailboxModule module;

    private final ReactiveTransactionManager transactionManager;

    public Mono<ServerResponse> getMails(ServerRequest request) {
        String searchTerm = request.queryParam("searchTerm").orElse("");
        StatusDTO status = request.queryParam("status")
                .map(String::toUpperCase)
                .map(StatusDTO::valueOf)
                .orElse(null);
        long skip = request.queryParam("skip")
                .map(Long::parseLong)
                .filter(s -> s >= 0)
                .orElse(0L);
        long limit = request.queryParam("limit")
                .map(Long::parseLong)
                .filter(l -> l >= 0)
                .orElse(100L);

        return toAgent(request)
                .flatMap(agent -> module.getMails(
                        searchTerm,
                        status,
                        skip,
                        limit,
                        agent
                ))
                .flatMap(agent -> ServerResponse.ok().bodyValue(agent))
                .onErrorResume(MissingPermissionError.class, e -> ServerResponse.status(FORBIDDEN).build());
    }

    public Mono<ServerResponse> getMail(ServerRequest request) {
        String mailId = request.pathVariable("mailId");

        return toAgent(request)
                .flatMap(agent -> module.getMail(mailId, agent))
                .flatMap(agent -> ServerResponse.ok().bodyValue(agent))
                .switchIfEmpty(ServerResponse.notFound().build())
                .onErrorResume(MissingPermissionError.class, e -> ServerResponse.status(FORBIDDEN).build());
    }

    public Mono<ServerResponse> getUnreadMailsCount(ServerRequest request) {
        return toAgent(request)
                .flatMap(module::getUnreadMailsCount)
                .flatMap(count -> ServerResponse.ok().bodyValue(count))
                .onErrorResume(MissingPermissionError.class, e -> ServerResponse.status(FORBIDDEN).build());
    }

    public Mono<ServerResponse> markMailAsRead(ServerRequest request) {
        String mailId = request.pathVariable("mailId");

        var transactionalOperator = TransactionalOperator.create(transactionManager);

        return request.bodyToMono(MarkMailAsReadRequest.class)
                .flatMap(req -> {
                    if (req.version < 0) {
                        return Mono.error(new IllegalArgumentException("Version is required"));
                    }
                    return Mono.just(req);
                })
                .flatMap(req -> toAgent(request).flatMap(agent -> module.markMailAsRead(mailId, req, agent)))
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .switchIfEmpty(ServerResponse.notFound().build())
                .onErrorResume(MissingPermissionError.class, e -> ServerResponse.status(FORBIDDEN).build())
                .onErrorResume(AggregateVersionOutdatedError.class, e -> ServerResponse.status(409).build())
                .onErrorResume(IllegalArgumentException.class, e -> ServerResponse.badRequest().build())
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> markMailAsUnread(ServerRequest request) {
        String mailId = request.pathVariable("mailId");

        var transactionalOperator = TransactionalOperator.create(transactionManager);

        return request.bodyToMono(MarkMailAsUnreadRequest.class)
                .flatMap(req -> {
                    if (req.version < 0) {
                        return Mono.error(new IllegalArgumentException("Version is required"));
                    }
                    return Mono.just(req);
                })
                .flatMap(req -> toAgent(request).flatMap(agent -> module.markMailAsUnread(mailId, req, agent)))
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .switchIfEmpty(ServerResponse.notFound().build())
                .onErrorResume(MissingPermissionError.class, e -> ServerResponse.status(FORBIDDEN).build())
                .onErrorResume(AggregateVersionOutdatedError.class, e -> ServerResponse.status(409).build())
                .onErrorResume(IllegalArgumentException.class, e -> ServerResponse.badRequest().build())
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> deleteMail(ServerRequest request) {
        String mailId = request.pathVariable("mailId");

        if (request.queryParam("version").isEmpty()) {
            return ServerResponse.badRequest().build();
        }
        long version = request.queryParam("version")
                .map(Long::parseLong)
                .orElse(0L);

        var transactionalOperator = TransactionalOperator.create(transactionManager);

        return toAgent(request).flatMap(agent -> module.deleteMail(mailId, version, agent))
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .switchIfEmpty(ServerResponse.notFound().build())
                .onErrorResume(MissingPermissionError.class, e -> ServerResponse.status(FORBIDDEN).build())
                .onErrorResume(AggregateVersionOutdatedError.class, e -> ServerResponse.status(409).build())
                .as(transactionalOperator::transactional);
    }

    private Mono<Agent> toAgent(ServerRequest request) {
        return request.principal()
                .map(principal -> Agent.user(AgentId.of(principal.getName())))
                .switchIfEmpty(Mono.just(Agent.anonymous()));
    }

}
