package de.bennyboer.kicherkrabbe.fabrics.http;

import de.bennyboer.kicherkrabbe.changes.ResourceId;
import de.bennyboer.kicherkrabbe.eventsourcing.AggregateNotFoundError;
import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.fabrics.*;
import de.bennyboer.kicherkrabbe.fabrics.http.api.*;
import de.bennyboer.kicherkrabbe.fabrics.http.api.requests.*;
import de.bennyboer.kicherkrabbe.fabrics.http.api.responses.*;
import de.bennyboer.kicherkrabbe.fabrics.persistence.colors.Color;
import de.bennyboer.kicherkrabbe.fabrics.persistence.fabrictypes.FabricType;
import de.bennyboer.kicherkrabbe.fabrics.persistence.topics.Topic;
import de.bennyboer.kicherkrabbe.fabrics.feature.AlreadyFeaturedError;
import de.bennyboer.kicherkrabbe.fabrics.publish.AlreadyPublishedError;
import de.bennyboer.kicherkrabbe.fabrics.unfeature.AlreadyUnfeaturedError;
import de.bennyboer.kicherkrabbe.fabrics.unpublish.AlreadyUnpublishedError;
import lombok.AllArgsConstructor;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

@AllArgsConstructor
public class FabricsHttpHandler {

    private final FabricsModule module;

    private final ReactiveTransactionManager transactionManager;

    public Mono<ServerResponse> getChanges(ServerRequest request) {
        var events$ = toAgent(request)
                .flatMapMany(module::getFabricChanges)
                .map(change -> {
                    var result = new FabricChangeDTO();

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
                .body(events$, FabricChangeDTO.class);
    }

    public Mono<ServerResponse> getAvailableTopicsForFabrics(ServerRequest request) {
        return toAgent(request)
                .flatMapMany(module::getAvailableTopicsForFabrics)
                .collectList()
                .map(topics -> {
                    var response = new QueryTopicsResponse();
                    response.topics = toTopicDTOs(topics);
                    return response;
                })
                .flatMap(result -> ServerResponse.ok().bodyValue(result));
    }

    public Mono<ServerResponse> getTopicsUsedInFabrics(ServerRequest request) {
        return toAgent(request)
                .flatMapMany(module::getTopicsUsedInFabrics)
                .collectList()
                .map(topics -> {
                    var response = new QueryTopicsResponse();
                    response.topics = toTopicDTOs(topics);
                    return response;
                })
                .flatMap(result -> ServerResponse.ok().bodyValue(result));
    }

    public Mono<ServerResponse> getAvailableColorsForFabrics(ServerRequest request) {
        return toAgent(request)
                .flatMapMany(module::getAvailableColorsForFabrics)
                .collectList()
                .map(colors -> {
                    var response = new QueryColorsResponse();
                    response.colors = toColorDTOs(colors);
                    return response;
                })
                .flatMap(result -> ServerResponse.ok().bodyValue(result));
    }

    public Mono<ServerResponse> getColorsUsedInFabrics(ServerRequest request) {
        return toAgent(request)
                .flatMapMany(module::getColorsUsedInFabrics)
                .collectList()
                .map(colors -> {
                    var response = new QueryColorsResponse();
                    response.colors = toColorDTOs(colors);
                    return response;
                })
                .flatMap(result -> ServerResponse.ok().bodyValue(result));
    }

    public Mono<ServerResponse> getAvailableFabricTypesForFabrics(ServerRequest request) {
        return toAgent(request)
                .flatMapMany(module::getAvailableFabricTypesForFabrics)
                .collectList()
                .map(fabricTypes -> {
                    var response = new QueryFabricTypesResponse();
                    response.fabricTypes = toFabricTypeDTOs(fabricTypes);
                    return response;
                })
                .flatMap(result -> ServerResponse.ok().bodyValue(result));
    }

    public Mono<ServerResponse> getFabricTypesUsedInFabrics(ServerRequest request) {
        return toAgent(request)
                .flatMapMany(module::getFabricTypesUsedInFabrics)
                .collectList()
                .map(fabricTypes -> {
                    var response = new QueryFabricTypesResponse();
                    response.fabricTypes = toFabricTypeDTOs(fabricTypes);
                    return response;
                })
                .flatMap(result -> ServerResponse.ok().bodyValue(result));
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

    public Mono<ServerResponse> getFeaturedFabrics(ServerRequest request) {
        var seed = request.queryParam("seed").map(Long::parseLong);

        return toAgent(request)
                .flatMapMany(module::getFeaturedFabrics)
                .collectList()
                .map(fabrics -> {
                    if (seed.isPresent()) {
                        var shuffled = new ArrayList<>(fabrics);
                        Collections.shuffle(shuffled, new Random(seed.get()));
                        return shuffled;
                    }
                    return fabrics;
                })
                .map(fabrics -> {
                    var response = new QueryFeaturedFabricsResponse();
                    response.fabrics = toPublishedFabricDTOs(fabrics);
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response));
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
                .onErrorMap(TopicsMissingError.class, e -> new ResponseStatusException(
                        PRECONDITION_FAILED,
                        e.getMessage(),
                        e
                ))
                .onErrorMap(ColorsMissingError.class, e -> new ResponseStatusException(
                        PRECONDITION_FAILED,
                        e.getMessage(),
                        e
                ))
                .onErrorMap(FabricTypesMissingError.class, e -> new ResponseStatusException(
                        PRECONDITION_FAILED,
                        e.getMessage(),
                        e
                ))
                .onErrorResume(AliasAlreadyInUseError.class, e -> ServerResponse.status(PRECONDITION_FAILED)
                        .bodyValue(Map.of(
                                "reason", "ALIAS_ALREADY_IN_USE",
                                "fabricId", e.getConflictingFabricId().getValue(),
                                "alias", e.getAlias().getValue()
                        ))
                )
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
                .onErrorResume(AliasAlreadyInUseError.class, e -> ServerResponse.status(PRECONDITION_FAILED)
                        .bodyValue(Map.of(
                                "reason", "ALIAS_ALREADY_IN_USE",
                                "fabricId", e.getConflictingFabricId().getValue(),
                                "alias", e.getAlias().getValue()
                        ))
                )
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

    public Mono<ServerResponse> featureFabric(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        String fabricId = request.pathVariable("fabricId");
        long version = request.queryParam("version")
                .map(Long::parseLong)
                .orElseThrow(() -> new ResponseStatusException(
                        BAD_REQUEST,
                        "Missing version query parameter"
                ));

        return toAgent(request)
                .flatMap(agent -> module.featureFabric(fabricId, version, agent))
                .map(newVersion -> {
                    var response = new FeatureFabricResponse();
                    response.version = newVersion;
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorMap(AggregateVersionOutdatedError.class, e -> new ResponseStatusException(
                        CONFLICT,
                        e.getMessage(),
                        e
                ))
                .onErrorMap(AlreadyFeaturedError.class, e -> new ResponseStatusException(
                        PRECONDITION_FAILED,
                        e.getMessage(),
                        e
                ))
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> unfeatureFabric(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        String fabricId = request.pathVariable("fabricId");
        long version = request.queryParam("version")
                .map(Long::parseLong)
                .orElseThrow(() -> new ResponseStatusException(
                        BAD_REQUEST,
                        "Missing version query parameter"
                ));

        return toAgent(request)
                .flatMap(agent -> module.unfeatureFabric(fabricId, version, agent))
                .map(newVersion -> {
                    var response = new UnfeatureFabricResponse();
                    response.version = newVersion;
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorMap(AggregateVersionOutdatedError.class, e -> new ResponseStatusException(
                        CONFLICT,
                        e.getMessage(),
                        e
                ))
                .onErrorMap(AlreadyUnfeaturedError.class, e -> new ResponseStatusException(
                        PRECONDITION_FAILED,
                        e.getMessage(),
                        e
                ))
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> updateFabricImages(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        String fabricId = request.pathVariable("fabricId");

        return request.bodyToMono(UpdateFabricImagesRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.updateFabricImages(
                        fabricId,
                        req.version,
                        req.imageId,
                        req.exampleImageIds != null ? req.exampleImageIds : List.of(),
                        agent
                )))
                .map(version -> {
                    var response = new UpdateFabricImagesResponse();
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
                .onErrorMap(ColorsMissingError.class, e -> new ResponseStatusException(
                        PRECONDITION_FAILED,
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
                .onErrorMap(TopicsMissingError.class, e -> new ResponseStatusException(
                        PRECONDITION_FAILED,
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
                .onErrorMap(FabricTypesMissingError.class, e -> new ResponseStatusException(
                        PRECONDITION_FAILED,
                        e.getMessage(),
                        e
                ))
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> deleteFabric(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

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
                .then(Mono.defer(() -> ServerResponse.ok().build()))
                .as(transactionalOperator::transactional);
    }

    private Mono<Agent> toAgent(ServerRequest request) {
        return request.principal()
                .map(principal -> Agent.user(AgentId.of(principal.getName())))
                .switchIfEmpty(Mono.just(Agent.anonymous()));
    }

    private List<ColorDTO> toColorDTOs(List<Color> colors) {
        return colors.stream()
                .map(this::toColorDTO)
                .toList();
    }

    private List<FabricTypeDTO> toFabricTypeDTOs(List<FabricType> fabricTypes) {
        return fabricTypes.stream()
                .map(this::toFabricTypeDTO)
                .toList();
    }

    private FabricTypeDTO toFabricTypeDTO(FabricType fabricType) {
        var result = new FabricTypeDTO();

        result.id = fabricType.getId().getValue();
        result.name = fabricType.getName().getValue();

        return result;
    }

    private ColorDTO toColorDTO(Color color) {
        var result = new ColorDTO();

        result.id = color.getId().getValue();
        result.name = color.getName().getValue();
        result.red = color.getRed();
        result.green = color.getGreen();
        result.blue = color.getBlue();

        return result;
    }

    private List<TopicDTO> toTopicDTOs(List<Topic> topics) {
        return topics.stream()
                .map(this::toTopicDTO)
                .toList();
    }

    private TopicDTO toTopicDTO(Topic topic) {
        var result = new TopicDTO();

        result.id = topic.getId().getValue();
        result.name = topic.getName().getValue();

        return result;
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
        result.exampleImageIds = fabric.getExampleImages().stream().map(ImageId::getValue).toList();
        result.colorIds = toColorIds(fabric.getColors());
        result.topicIds = toTopicIds(fabric.getTopics());
        result.availability = toFabricTypeAvailabilityDTOs(fabric.getAvailability());
        result.published = fabric.isPublished();
        result.featured = fabric.isFeatured();
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
        result.alias = fabric.getAlias().getValue();
        result.name = fabric.getName().getValue();
        result.imageId = fabric.getImage().getValue();
        result.exampleImageIds = fabric.getExampleImages().stream().map(ImageId::getValue).toList();
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
