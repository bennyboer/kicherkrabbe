package de.bennyboer.kicherkrabbe.inquiries.http;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.inquiries.InquiriesDisabledException;
import de.bennyboer.kicherkrabbe.inquiries.InquiriesModule;
import de.bennyboer.kicherkrabbe.inquiries.TooManyRequestsException;
import de.bennyboer.kicherkrabbe.inquiries.api.requests.SendInquiryRequest;
import de.bennyboer.kicherkrabbe.inquiries.api.requests.UpdateRateLimitsRequest;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import lombok.AllArgsConstructor;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.springframework.http.HttpStatus.*;

@AllArgsConstructor
public class InquiriesHttpHandler {

    private final InquiriesModule module;

    private final ReactiveTransactionManager transactionManager;

    private final Clock clock;

    public Mono<ServerResponse> sendInquiry(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);
        var ipAddress = Optional.ofNullable(request.exchange().getRequest().getRemoteAddress())
                .map(InetSocketAddress::getHostString)
                .orElse(null);

        return request.bodyToMono(SendInquiryRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.sendInquiry(
                        req.requestId,
                        req.sender,
                        req.subject,
                        req.message,
                        agent,
                        ipAddress
                )))
                .then(ServerResponse.ok().build())
                .onErrorMap(
                        IllegalArgumentException.class, e -> new ResponseStatusException(
                                BAD_REQUEST,
                                e.getMessage(),
                                e
                        )
                )
                .onErrorMap(
                        TooManyRequestsException.class, e -> new ResponseStatusException(
                                TOO_MANY_REQUESTS,
                                e.getMessage(),
                                e
                        )
                )
                .onErrorMap(
                        InquiriesDisabledException.class, e -> new ResponseStatusException(
                                FORBIDDEN,
                                e.getMessage(),
                                e
                        )
                )
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> getStatus(ServerRequest request) {
        return toAgent(request)
                .flatMap(module::getStatus)
                .flatMap(status -> ServerResponse.ok().bodyValue(status));
    }

    public Mono<ServerResponse> getSettings(ServerRequest request) {
        return toAgent(request)
                .flatMap(module::getSettings)
                .flatMap(settings -> ServerResponse.ok().bodyValue(settings))
                .onErrorResume(MissingPermissionError.class, e -> ServerResponse.status(FORBIDDEN).build());
    }

    public Mono<ServerResponse> enable(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        return toAgent(request)
                .flatMap(agent -> module.setSendingInquiriesEnabled(true, agent))
                .then(ServerResponse.ok().build())
                .onErrorResume(MissingPermissionError.class, e -> ServerResponse.status(FORBIDDEN).build())
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> disable(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        return toAgent(request)
                .flatMap(agent -> module.setSendingInquiriesEnabled(false, agent))
                .then(ServerResponse.ok().build())
                .onErrorResume(MissingPermissionError.class, e -> ServerResponse.status(FORBIDDEN).build())
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> updateRateLimits(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        return request.bodyToMono(UpdateRateLimitsRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.updateRateLimits(req, agent)))
                .then(ServerResponse.ok().build())
                .onErrorResume(MissingPermissionError.class, e -> ServerResponse.status(FORBIDDEN).build())
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> getStatistics(ServerRequest request) {
        Instant from = request.queryParam("from")
                .map(Instant::parse)
                .orElse(clock.instant().minus(29, DAYS).truncatedTo(DAYS));
        Instant to = request.queryParam("to")
                .map(Instant::parse)
                .orElse(clock.instant().truncatedTo(DAYS).plus(1, DAYS));

        return toAgent(request)
                .flatMap(agent -> module.getRequestStatistics(from, to, agent))
                .flatMap(stats -> ServerResponse.ok().bodyValue(stats))
                .onErrorResume(MissingPermissionError.class, e -> ServerResponse.status(FORBIDDEN).build());
    }

    private Mono<Agent> toAgent(ServerRequest request) {
        return request.principal()
                .map(principal -> Agent.user(AgentId.of(principal.getName())))
                .switchIfEmpty(Mono.just(Agent.anonymous()));
    }

}
