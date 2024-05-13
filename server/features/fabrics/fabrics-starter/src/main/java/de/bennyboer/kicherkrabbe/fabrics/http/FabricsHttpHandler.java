package de.bennyboer.kicherkrabbe.fabrics.http;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateNotFoundError;
import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.fabrics.*;
import de.bennyboer.kicherkrabbe.fabrics.http.api.FabricDTO;
import de.bennyboer.kicherkrabbe.fabrics.http.api.FabricTypeAvailabilityDTO;
import de.bennyboer.kicherkrabbe.fabrics.http.api.PublishedFabricDTO;
import de.bennyboer.kicherkrabbe.fabrics.http.api.requests.*;
import de.bennyboer.kicherkrabbe.fabrics.http.api.responses.*;
import de.bennyboer.kicherkrabbe.fabrics.publish.AlreadyPublishedError;
import de.bennyboer.kicherkrabbe.fabrics.unpublish.AlreadyUnpublishedError;
import lombok.AllArgsConstructor;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

@AllArgsConstructor
public class FabricsHttpHandler {

    private final FabricsModule module;

    private final ReactiveTransactionManager transactionManager;

    public Mono<ServerResponse> getFabricsTopics(ServerRequest request) {
        return Mono.empty(); // TODO Get topics used for at least one fabric
    }

    public Mono<ServerResponse> getFabricsColors(ServerRequest request) {
        return Mono.empty(); // TODO Get colors used for at least one fabric
    }

    public Mono<ServerResponse> getChanges(ServerRequest request) {
        return Mono.empty(); // TODO
    }

    public Mono<ServerResponse> getFabrics(ServerRequest request) {
        return request.bodyToMono(QueryFabricsRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.getFabrics(
                        req.searchTerm,
                        req.skip,
                        req.limit,
                        agent
                )))
                .map(page -> {
                    var response = new QueryFabricsResponse();
                    response.fabrics = toFabricDTOs(page.getResults());
                    response.total = page.getTotal();
                    response.skip = page.getSkip();
                    response.limit = page.getLimit();
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response));
    }

    public Mono<ServerResponse> getFabric(ServerRequest request) {
        String fabricId = request.pathVariable("fabricId");

        return toAgent(request)
                .flatMap(agent -> module.getFabric(fabricId, agent))
                .map(fabric -> {
                    var response = new QueryFabricResponse();
                    response.fabric = toFabricDTO(fabric);
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorMap(AggregateNotFoundError.class, e -> new ResponseStatusException(
                        NOT_FOUND,
                        e.getMessage(),
                        e
                ));
    }

    public Mono<ServerResponse> getPublishedFabrics(ServerRequest request) {
        return request.bodyToMono(QueryPublishedFabricsRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.getPublishedFabrics(
                        req.searchTerm,
                        req.colorIds,
                        req.topicIds,
                        req.availability,
                        req.sort,
                        req.skip,
                        req.limit,
                        agent
                )))
                .map(page -> {
                    var response = new QueryPublishedFabricsResponse();
                    response.fabrics = toPublishedFabricDTOs(page.getResults());
                    response.total = page.getTotal();
                    response.skip = page.getSkip();
                    response.limit = page.getLimit();
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response));
    }

    public Mono<ServerResponse> getPublishedFabric(ServerRequest request) {
        String fabricId = request.pathVariable("fabricId");

        return toAgent(request)
                .flatMap(agent -> module.getPublishedFabric(fabricId, agent))
                .map(fabric -> {
                    var response = new QueryPublishedFabricResponse();
                    response.fabric = toPublishedFabricDTO(fabric);
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .switchIfEmpty(Mono.error(new ResponseStatusException(NOT_FOUND, "Fabric not available")));
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

    private List<FabricDTO> toFabricDTOs(List<FabricDetails> fabrics) {
        return fabrics.stream()
                .map(this::toFabricDTO)
                .toList();
    }

    private FabricDTO toFabricDTO(FabricDetails fabric) {
        var result = new FabricDTO();

        result.id = fabric.getId().getValue();
        result.version = fabric.getVersion().getValue();
        result.name = fabric.getName().getValue();
        result.imageId = fabric.getImage().getValue();
        result.colorIds = toColorIds(fabric.getColors());
        result.topicIds = toTopicIds(fabric.getTopics());
        result.availability = toFabricTypeAvailabilityDTOs(fabric.getAvailability());
        result.published = fabric.isPublished();
        result.createdAt = fabric.getCreatedAt();

        return result;
    }

    private List<PublishedFabricDTO> toPublishedFabricDTOs(List<PublishedFabric> fabrics) {
        return fabrics.stream()
                .map(this::toPublishedFabricDTO)
                .toList();
    }

    private PublishedFabricDTO toPublishedFabricDTO(PublishedFabric fabric) {
        var result = new PublishedFabricDTO();

        result.id = fabric.getId().getValue();
        result.name = fabric.getName().getValue();
        result.imageId = fabric.getImage().getValue();
        result.colorIds = toColorIds(fabric.getColors());
        result.topicIds = toTopicIds(fabric.getTopics());
        result.availability = toFabricTypeAvailabilityDTOs(fabric.getAvailability());

        return result;
    }

    private Set<String> toTopicIds(Set<TopicId> topicIds) {
        return topicIds.stream()
                .map(TopicId::getValue)
                .collect(Collectors.toSet());
    }

    private Set<String> toColorIds(Set<ColorId> colorIds) {
        return colorIds.stream()
                .map(ColorId::getValue)
                .collect(Collectors.toSet());
    }

    private Set<FabricTypeAvailabilityDTO> toFabricTypeAvailabilityDTOs(Set<FabricTypeAvailability> availability) {
        return availability.stream()
                .map(this::toFabricTypeAvailabilityDTO)
                .collect(Collectors.toSet());
    }

    private FabricTypeAvailabilityDTO toFabricTypeAvailabilityDTO(FabricTypeAvailability availability) {
        var result = new FabricTypeAvailabilityDTO();

        result.typeId = availability.getTypeId().getValue();
        result.inStock = availability.isInStock();

        return result;
    }

}
