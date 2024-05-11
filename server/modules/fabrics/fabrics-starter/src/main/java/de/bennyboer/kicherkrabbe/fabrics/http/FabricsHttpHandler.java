package de.bennyboer.kicherkrabbe.fabrics.http;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.fabrics.FabricsModule;
import de.bennyboer.kicherkrabbe.fabrics.http.requests.*;
import de.bennyboer.kicherkrabbe.fabrics.http.responses.*;
import de.bennyboer.kicherkrabbe.fabrics.publish.AlreadyPublishedError;
import de.bennyboer.kicherkrabbe.fabrics.unpublish.AlreadyUnpublishedError;
import lombok.AllArgsConstructor;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpStatus.*;

@AllArgsConstructor
public class FabricsHttpHandler {

    private final FabricsModule module;

    private final ReactiveTransactionManager transactionManager;

    public Mono<ServerResponse> getFabrics(ServerRequest request) {
        return Mono.empty(); // TODO
    }

    public Mono<ServerResponse> getFabric(ServerRequest request) {
        return Mono.empty(); // TODO
    }

    public Mono<ServerResponse> getPublishedFabrics(ServerRequest request) {
        return Mono.empty(); // TODO
    }

    public Mono<ServerResponse> createFabric(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        return request.bodyToMono(CreateFabricRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.createFabric(
                        req.name,
                        req.imageId,
                        req.colorIds,
                        req.topicIds,
                        req.availability,
                        agent
                )))
                .map(fabricId -> {
                    var result = new CreateFabricResponse();
                    result.id = fabricId;
                    return result;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorMap(IllegalArgumentException.class, e -> new ResponseStatusException(
                        BAD_REQUEST,
                        e.getMessage(),
                        e
                ))
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> renameFabric(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        String fabricId = request.pathVariable("fabricId");

        return request.bodyToMono(RenameFabricRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.renameFabric(
                        fabricId,
                        req.version,
                        req.name,
                        agent
                )))
                .map(version -> {
                    var response = new RenameFabricResponse();
                    response.version = version;
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorMap(AggregateVersionOutdatedError.class, e -> new ResponseStatusException(
                        CONFLICT,
                        e.getMessage(),
                        e
                ))
                .onErrorMap(IllegalArgumentException.class, e -> new ResponseStatusException(
                        BAD_REQUEST,
                        e.getMessage(),
                        e
                ))
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> publishFabric(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        String fabricId = request.pathVariable("fabricId");
        long version = request.queryParam("version")
                .map(Long::parseLong)
                .orElseThrow(() -> new ResponseStatusException(
                        BAD_REQUEST,
                        "Missing version query parameter"
                ));

        return toAgent(request)
                .flatMap(agent -> module.publishFabric(fabricId, version, agent))
                .map(newVersion -> {
                    var response = new PublishFabricResponse();
                    response.version = newVersion;
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorMap(AggregateVersionOutdatedError.class, e -> new ResponseStatusException(
                        CONFLICT,
                        e.getMessage(),
                        e
                ))
                .onErrorMap(AlreadyPublishedError.class, e -> new ResponseStatusException(
                        PRECONDITION_FAILED,
                        e.getMessage(),
                        e
                ))
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> unpublishFabric(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        String fabricId = request.pathVariable("fabricId");
        long version = request.queryParam("version")
                .map(Long::parseLong)
                .orElseThrow(() -> new ResponseStatusException(
                        BAD_REQUEST,
                        "Missing version query parameter"
                ));

        return toAgent(request)
                .flatMap(agent -> module.unpublishFabric(fabricId, version, agent))
                .map(newVersion -> {
                    var response = new UnpublishFabricResponse();
                    response.version = newVersion;
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorMap(AggregateVersionOutdatedError.class, e -> new ResponseStatusException(
                        CONFLICT,
                        e.getMessage(),
                        e
                ))
                .onErrorMap(AlreadyUnpublishedError.class, e -> new ResponseStatusException(
                        PRECONDITION_FAILED,
                        e.getMessage(),
                        e
                ))
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> updateFabricImage(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        String fabricId = request.pathVariable("fabricId");

        return request.bodyToMono(UpdateFabricImageRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.updateFabricImage(
                        fabricId,
                        req.version,
                        req.imageId,
                        agent
                )))
                .map(version -> {
                    var response = new UpdateFabricImageResponse();
                    response.version = version;
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorMap(AggregateVersionOutdatedError.class, e -> new ResponseStatusException(
                        CONFLICT,
                        e.getMessage(),
                        e
                ))
                .onErrorMap(IllegalArgumentException.class, e -> new ResponseStatusException(
                        BAD_REQUEST,
                        e.getMessage(),
                        e
                ))
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> updateFabricColors(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        String fabricId = request.pathVariable("fabricId");

        return request.bodyToMono(UpdateFabricColorsRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.updateFabricColors(
                        fabricId,
                        req.version,
                        req.colorIds,
                        agent
                )))
                .map(version -> {
                    var response = new UpdateFabricColorsResponse();
                    response.version = version;
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorMap(AggregateVersionOutdatedError.class, e -> new ResponseStatusException(
                        CONFLICT,
                        e.getMessage(),
                        e
                ))
                .onErrorMap(IllegalArgumentException.class, e -> new ResponseStatusException(
                        BAD_REQUEST,
                        e.getMessage(),
                        e
                ))
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> updateFabricTopics(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        String fabricId = request.pathVariable("fabricId");

        return request.bodyToMono(UpdateFabricTopicsRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.updateFabricTopics(
                        fabricId,
                        req.version,
                        req.topicIds,
                        agent
                )))
                .map(version -> {
                    var response = new UpdateFabricTopicsResponse();
                    response.version = version;
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorMap(AggregateVersionOutdatedError.class, e -> new ResponseStatusException(
                        CONFLICT,
                        e.getMessage(),
                        e
                ))
                .onErrorMap(IllegalArgumentException.class, e -> new ResponseStatusException(
                        BAD_REQUEST,
                        e.getMessage(),
                        e
                ))
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> updateFabricAvailability(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        String fabricId = request.pathVariable("fabricId");

        return request.bodyToMono(UpdateFabricAvailabilityRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.updateFabricAvailability(
                        fabricId,
                        req.version,
                        req.availability,
                        agent
                )))
                .map(version -> {
                    var response = new UpdateFabricAvailabilityResponse();
                    response.version = version;
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorMap(AggregateVersionOutdatedError.class, e -> new ResponseStatusException(
                        CONFLICT,
                        e.getMessage(),
                        e
                ))
                .onErrorMap(IllegalArgumentException.class, e -> new ResponseStatusException(
                        BAD_REQUEST,
                        e.getMessage(),
                        e
                ))
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> deleteFabric(ServerRequest request) {
        String fabricId = request.pathVariable("fabricId");
        long version = request.queryParam("version")
                .map(Long::parseLong)
                .orElseThrow(() -> new ResponseStatusException(
                        BAD_REQUEST,
                        "Missing version query parameter"
                ));

        return toAgent(request)
                .flatMap(agent -> module.deleteFabric(fabricId, version, agent))
                .onErrorMap(AggregateVersionOutdatedError.class, e -> new ResponseStatusException(
                        CONFLICT,
                        e.getMessage(),
                        e
                ))
                .then(Mono.defer(() -> ServerResponse.ok().build()));
    }

    private Mono<Agent> toAgent(ServerRequest request) {
        return request.principal()
                .map(principal -> Agent.user(AgentId.of(principal.getName())))
                .switchIfEmpty(Mono.just(Agent.anonymous()));
    }

}
