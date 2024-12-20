package de.bennyboer.kicherkrabbe.inquiries.http;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.inquiries.InquiriesDisabledException;
import de.bennyboer.kicherkrabbe.inquiries.InquiriesModule;
import de.bennyboer.kicherkrabbe.inquiries.TooManyRequestsException;
import de.bennyboer.kicherkrabbe.inquiries.api.requests.SendInquiryRequest;
import lombok.AllArgsConstructor;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.util.Optional;

import static org.springframework.http.HttpStatus.*;

@AllArgsConstructor
public class InquiriesHttpHandler {

    private final InquiriesModule module;

    private final ReactiveTransactionManager transactionManager;

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

    private Mono<Agent> toAgent(ServerRequest request) {
        return request.principal()
                .map(principal -> Agent.user(AgentId.of(principal.getName())))
                .switchIfEmpty(Mono.just(Agent.anonymous()));
    }

}
