package de.bennyboer.kicherkrabbe.fabrictypes.http;

import de.bennyboer.kicherkrabbe.changes.ResourceId;
import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.fabrictypes.FabricTypesModule;
import de.bennyboer.kicherkrabbe.fabrictypes.http.requests.CreateFabricTypeRequest;
import de.bennyboer.kicherkrabbe.fabrictypes.http.requests.UpdateFabricTypeRequest;
import de.bennyboer.kicherkrabbe.fabrictypes.http.responses.*;
import lombok.AllArgsConstructor;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;

@AllArgsConstructor
public class FabricTypesHttpHandler {

    private final FabricTypesModule module;

    private final ReactiveTransactionManager transactionManager;

    public Mono<ServerResponse> getFabricTypes(ServerRequest request) {
        String searchTerm = request.queryParam("searchTerm").orElse("");
        long skip = request.queryParam("skip").map(Long::parseLong).orElse(0L);
        long limit = request.queryParam("limit").map(Long::parseLong).orElse((long) Integer.MAX_VALUE);

        return toAgent(request)
                .flatMap(agent -> module.getFabricTypes(searchTerm, skip, limit, agent))
                .map(result -> {
                    var response = new QueryFabricTypesResponse();
                    response.skip = result.getSkip();
                    response.limit = result.getLimit();
                    response.total = result.getTotal();
                    response.fabricTypes = result.getResults()
                            .stream()
                            .map(t -> {
                                var fabricType = new FabricTypeDTO();

                                fabricType.id = t.getId().getValue();
                                fabricType.version = t.getVersion().getValue();
                                fabricType.name = t.getName().getValue();
                                fabricType.createdAt = t.getCreatedAt();

                                return fabricType;
                            })
                            .toList();
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response));
    }

    public Mono<ServerResponse> getChanges(ServerRequest request) {
        var events$ = toAgent(request)
                .flatMapMany(module::getFabricTypeChanges)
                .map(change -> {
                    var result = new FabricTypeChangeDTO();

                    result.type = change.getType().getValue();
                    result.affected = change.getAffected()
                            .stream()
                            .map(ResourceId::getValue)
                            .toList();
                    result.payload = change.getPayload();

                    return result;
                });

        return ServerResponse.ok()
                .header("Content-Type", "text/event-stream")
                .body(events$, FabricTypeChangeDTO.class);
    }

    public Mono<ServerResponse> createFabricType(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        return request.bodyToMono(CreateFabricTypeRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.createFabricType(
                        req.name,
                        agent
                )))
                .map(fabricTypeId -> {
                    var result = new CreateFabricTypeResponse();
                    result.id = fabricTypeId;
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

    public Mono<ServerResponse> updateFabricType(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        String fabricTypeId = request.pathVariable("typeId");

        return request.bodyToMono(UpdateFabricTypeRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.updateFabricType(
                        fabricTypeId,
                        req.version,
                        req.name,
                        agent
                )))
                .map(version -> {
                    var result = new UpdateFabricTypeResponse();
                    result.version = version;
                    return result;
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

    public Mono<ServerResponse> deleteFabricType(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        String fabricTypeId = request.pathVariable("typeId");
        long version = request.queryParam("version").map(Long::parseLong).orElseThrow();

        return toAgent(request)
                .flatMap(agent -> module.deleteFabricType(fabricTypeId, version, agent))
                .map(updatedVersion -> {
                    var result = new DeleteFabricTypeResponse();
                    result.version = updatedVersion;
                    return result;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorMap(AggregateVersionOutdatedError.class, e -> new ResponseStatusException(
                        CONFLICT,
                        e.getMessage(),
                        e
                ))
                .as(transactionalOperator::transactional);
    }

    private Mono<Agent> toAgent(ServerRequest request) {
        return request.principal()
                .map(principal -> Agent.user(AgentId.of(principal.getName())))
                .switchIfEmpty(Mono.just(Agent.anonymous()));
    }

}
