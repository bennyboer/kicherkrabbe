package de.bennyboer.kicherkrabbe.notifications.http;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.notifications.DateRangeFilter;
import de.bennyboer.kicherkrabbe.notifications.NotificationsModule;
import de.bennyboer.kicherkrabbe.notifications.api.requests.*;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import lombok.AllArgsConstructor;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.Instant;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;

@AllArgsConstructor
public class NotificationsHttpHandler {

    private final NotificationsModule module;

    private final ReactiveTransactionManager transactionManager;

    public Mono<ServerResponse> getNotifications(ServerRequest request) {
        Instant from = request.queryParam("from")
                .map(Instant::parse)
                .orElse(null);
        Instant to = request.queryParam("to")
                .map(Instant::parse)
                .orElse(null);
        long skip = request.queryParam("skip")
                .map(Long::parseLong)
                .filter(l -> l >= 0)
                .orElse(0L);
        long limit = request.queryParam("limit")
                .map(Long::parseLong)
                .filter(l -> l > 0)
                .orElse(100L);

        return toAgent(request)
                .flatMap(agent -> module.getNotifications(
                        DateRangeFilter.of(from, to),
                        skip,
                        limit,
                        agent
                ))
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorResume(MissingPermissionError.class, e -> ServerResponse.status(FORBIDDEN).build());
    }

    public Mono<ServerResponse> getSettings(ServerRequest request) {
        return toAgent(request)
                .flatMap(module::getSettings)
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorResume(MissingPermissionError.class, e -> ServerResponse.status(FORBIDDEN).build());
    }

    public Mono<ServerResponse> enableSystemNotifications(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        return request.bodyToMono(EnableSystemNotificationsRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.enableSystemNotifications(req.version, agent)))
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorResume(MissingPermissionError.class, e -> ServerResponse.status(FORBIDDEN).build())
                .onErrorResume(AggregateVersionOutdatedError.class, e -> ServerResponse.status(CONFLICT).build())
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> disableSystemNotifications(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        return request.bodyToMono(DisableSystemNotificationsRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.disableSystemNotifications(
                        req.version,
                        agent
                )))
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorResume(MissingPermissionError.class, e -> ServerResponse.status(FORBIDDEN).build())
                .onErrorResume(AggregateVersionOutdatedError.class, e -> ServerResponse.status(CONFLICT).build())
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> updateSystemNotificationChannel(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        return request.bodyToMono(UpdateSystemChannelRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.updateSystemChannel(req, agent)))
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorResume(MissingPermissionError.class, e -> ServerResponse.status(FORBIDDEN).build())
                .onErrorResume(AggregateVersionOutdatedError.class, e -> ServerResponse.status(CONFLICT).build())
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> activateSystemNotificationChannel(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        return request.bodyToMono(ActivateSystemChannelRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.activateSystemChannel(req, agent)))
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorResume(MissingPermissionError.class, e -> ServerResponse.status(FORBIDDEN).build())
                .onErrorResume(AggregateVersionOutdatedError.class, e -> ServerResponse.status(CONFLICT).build())
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> deactivateSystemNotificationChannel(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        return request.bodyToMono(DeactivateSystemChannelRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.deactivateSystemChannel(req, agent)))
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
