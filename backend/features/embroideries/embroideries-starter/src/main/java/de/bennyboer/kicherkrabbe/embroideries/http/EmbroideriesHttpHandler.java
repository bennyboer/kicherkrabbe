package de.bennyboer.kicherkrabbe.embroideries.http;

import de.bennyboer.kicherkrabbe.changes.ResourceId;
import de.bennyboer.kicherkrabbe.embroideries.AliasAlreadyInUseError;
import de.bennyboer.kicherkrabbe.embroideries.CategoriesMissingError;
import de.bennyboer.kicherkrabbe.embroideries.EmbroideriesModule;
import de.bennyboer.kicherkrabbe.embroideries.EmbroideryCategory;
import de.bennyboer.kicherkrabbe.embroideries.EmbroideryCategoryId;
import de.bennyboer.kicherkrabbe.embroideries.EmbroideryDetails;
import de.bennyboer.kicherkrabbe.embroideries.EmbroideryNotFoundError;
import de.bennyboer.kicherkrabbe.embroideries.PublishedEmbroidery;
import de.bennyboer.kicherkrabbe.embroideries.feature.AlreadyFeaturedError;
import de.bennyboer.kicherkrabbe.embroideries.http.api.CategoryDTO;
import de.bennyboer.kicherkrabbe.embroideries.http.api.EmbroideriesSortDTO;
import de.bennyboer.kicherkrabbe.embroideries.http.api.EmbroideryChangeDTO;
import de.bennyboer.kicherkrabbe.embroideries.http.api.EmbroideryDTO;
import de.bennyboer.kicherkrabbe.embroideries.http.api.PublishedEmbroideryDTO;
import de.bennyboer.kicherkrabbe.embroideries.http.api.requests.CreateEmbroideryRequest;
import de.bennyboer.kicherkrabbe.embroideries.http.api.requests.QueryEmbroideriesRequest;
import de.bennyboer.kicherkrabbe.embroideries.http.api.requests.QueryPublishedEmbroideriesRequest;
import de.bennyboer.kicherkrabbe.embroideries.http.api.requests.RenameEmbroideryRequest;
import de.bennyboer.kicherkrabbe.embroideries.http.api.requests.UpdateEmbroideryCategoriesRequest;
import de.bennyboer.kicherkrabbe.embroideries.http.api.requests.UpdateEmbroideryImageRequest;
import de.bennyboer.kicherkrabbe.embroideries.http.api.responses.CreateEmbroideryResponse;
import de.bennyboer.kicherkrabbe.embroideries.http.api.responses.FeatureEmbroideryResponse;
import de.bennyboer.kicherkrabbe.embroideries.http.api.responses.PublishEmbroideryResponse;
import de.bennyboer.kicherkrabbe.embroideries.http.api.responses.QueryCategoriesResponse;
import de.bennyboer.kicherkrabbe.embroideries.http.api.responses.QueryEmbroideriesResponse;
import de.bennyboer.kicherkrabbe.embroideries.http.api.responses.QueryEmbroideryResponse;
import de.bennyboer.kicherkrabbe.embroideries.http.api.responses.QueryFeaturedEmbroideriesResponse;
import de.bennyboer.kicherkrabbe.embroideries.http.api.responses.QueryPublishedEmbroideriesResponse;
import de.bennyboer.kicherkrabbe.embroideries.http.api.responses.QueryPublishedEmbroideryResponse;
import de.bennyboer.kicherkrabbe.embroideries.http.api.responses.RenameEmbroideryResponse;
import de.bennyboer.kicherkrabbe.embroideries.http.api.responses.UnfeatureEmbroideryResponse;
import de.bennyboer.kicherkrabbe.embroideries.http.api.responses.UnpublishEmbroideryResponse;
import de.bennyboer.kicherkrabbe.embroideries.http.api.responses.UpdateEmbroideryCategoriesResponse;
import de.bennyboer.kicherkrabbe.embroideries.http.api.responses.UpdateEmbroideryImageResponse;
import de.bennyboer.kicherkrabbe.embroideries.publish.AlreadyPublishedError;
import de.bennyboer.kicherkrabbe.embroideries.unfeature.AlreadyUnfeaturedError;
import de.bennyboer.kicherkrabbe.embroideries.unpublish.AlreadyUnpublishedError;
import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import jakarta.annotation.Nullable;
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
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import static de.bennyboer.kicherkrabbe.embroideries.http.api.EmbroideriesSortDirectionDTO.DESCENDING;
import static java.util.stream.Collectors.toSet;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.PRECONDITION_FAILED;

@AllArgsConstructor
public class EmbroideriesHttpHandler {

    private final EmbroideriesModule module;

    private final ReactiveTransactionManager transactionManager;

    public Mono<ServerResponse> getChanges(ServerRequest request) {
        var events$ = toAgent(request)
                .flatMapMany(module::getEmbroideryChanges)
                .map(change -> {
                    var result = new EmbroideryChangeDTO();

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
                .body(events$, EmbroideryChangeDTO.class);
    }

    public Mono<ServerResponse> getAvailableCategoriesForEmbroideries(ServerRequest request) {
        return toAgent(request)
                .flatMapMany(module::getAvailableCategoriesForEmbroideries)
                .collectList()
                .map(categories -> {
                    var response = new QueryCategoriesResponse();
                    response.categories = toCategoryDTOs(categories);
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response));
    }

    public Mono<ServerResponse> getEmbroideries(ServerRequest request) {
        return request.bodyToMono(QueryEmbroideriesRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.getEmbroideries(
                        req.searchTerm,
                        req.categories,
                        req.skip,
                        req.limit,
                        agent
                )))
                .map(page -> {
                    var response = new QueryEmbroideriesResponse();
                    response.embroideries = page.getResults()
                            .stream()
                            .map(this::toEmbroideryDTO)
                            .toList();
                    response.skip = page.getSkip();
                    response.limit = page.getLimit();
                    response.total = page.getTotal();
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorMap(IllegalArgumentException.class, e -> new ResponseStatusException(BAD_REQUEST, e.getMessage(), e));
    }

    public Mono<ServerResponse> getEmbroidery(ServerRequest request) {
        String embroideryId = request.pathVariable("embroideryId");

        return toAgent(request)
                .flatMap(agent -> module.getEmbroidery(embroideryId, agent))
                .map(embroidery -> {
                    var response = new QueryEmbroideryResponse();
                    response.embroidery = toEmbroideryDTO(embroidery);
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorMap(EmbroideryNotFoundError.class, e -> new ResponseStatusException(NOT_FOUND, e.getMessage(), e));
    }

    public Mono<ServerResponse> getCategoriesUsedInEmbroideries(ServerRequest request) {
        return toAgent(request)
                .flatMapMany(module::getCategoriesUsedInEmbroideries)
                .collectList()
                .map(categories -> {
                    var response = new QueryCategoriesResponse();
                    response.categories = toCategoryDTOs(categories);
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response));
    }

    public Mono<ServerResponse> getPublishedEmbroideries(ServerRequest request) {
        return request.bodyToMono(QueryPublishedEmbroideriesRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.getPublishedEmbroideries(
                        Optional.ofNullable(req.searchTerm).orElse(""),
                        Optional.ofNullable(req.categories).orElseGet(Set::of),
                        isAscending(req.sort),
                        req.skip,
                        req.limit,
                        agent
                )))
                .map(page -> {
                    var response = new QueryPublishedEmbroideriesResponse();
                    response.embroideries = toPublishedEmbroideryDTOs(page.getResults());
                    response.skip = page.getSkip();
                    response.limit = page.getLimit();
                    response.total = page.getTotal();
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorMap(IllegalArgumentException.class, e -> new ResponseStatusException(BAD_REQUEST, e.getMessage(), e));
    }

    public Mono<ServerResponse> getPublishedEmbroidery(ServerRequest request) {
        String embroideryIdOrAlias = request.pathVariable("embroideryId");

        return toAgent(request)
                .flatMap(agent -> module.getPublishedEmbroidery(embroideryIdOrAlias, agent))
                .map(embroidery -> {
                    var response = new QueryPublishedEmbroideryResponse();
                    response.embroidery = toPublishedEmbroideryDTO(embroidery);
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .switchIfEmpty(Mono.error(new ResponseStatusException(NOT_FOUND, "Embroidery not available")));
    }

    public Mono<ServerResponse> getFeaturedEmbroideries(ServerRequest request) {
        var seed = request.queryParam("seed").map(Long::parseLong);

        return toAgent(request)
                .flatMapMany(module::getFeaturedEmbroideries)
                .collectList()
                .map(embroideries -> {
                    if (seed.isPresent()) {
                        var shuffled = new ArrayList<>(embroideries);
                        Collections.shuffle(shuffled, new Random(seed.get()));
                        return shuffled;
                    }
                    return embroideries;
                })
                .map(embroideries -> {
                    var response = new QueryFeaturedEmbroideriesResponse();
                    response.embroideries = toPublishedEmbroideryDTOs(embroideries);
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response));
    }

    public Mono<ServerResponse> createEmbroidery(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        return request.bodyToMono(CreateEmbroideryRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.createEmbroidery(
                        req.name,
                        req.image,
                        req.categories,
                        agent
                )))
                .map(embroideryId -> {
                    var response = new CreateEmbroideryResponse();
                    response.id = embroideryId;
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorMap(IllegalArgumentException.class, e -> new ResponseStatusException(BAD_REQUEST, e.getMessage(), e))
                .onErrorMap(CategoriesMissingError.class, e -> new ResponseStatusException(PRECONDITION_FAILED, e.getMessage(), e))
                .onErrorResume(AliasAlreadyInUseError.class, this::toAliasAlreadyInUseResponse)
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> renameEmbroidery(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);
        String embroideryId = request.pathVariable("embroideryId");

        return request.bodyToMono(RenameEmbroideryRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.renameEmbroidery(
                        embroideryId,
                        req.version,
                        req.name,
                        agent
                )))
                .map(version -> {
                    var response = new RenameEmbroideryResponse();
                    response.version = version;
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorMap(AggregateVersionOutdatedError.class, e -> new ResponseStatusException(CONFLICT, e.getMessage(), e))
                .onErrorMap(IllegalArgumentException.class, e -> new ResponseStatusException(BAD_REQUEST, e.getMessage(), e))
                .onErrorResume(AliasAlreadyInUseError.class, this::toAliasAlreadyInUseResponse)
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> publishEmbroidery(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);
        String embroideryId = request.pathVariable("embroideryId");
        long version = getVersionQueryParam(request);

        return toAgent(request)
                .flatMap(agent -> module.publishEmbroidery(embroideryId, version, agent))
                .map(newVersion -> {
                    var response = new PublishEmbroideryResponse();
                    response.version = newVersion;
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorMap(AggregateVersionOutdatedError.class, e -> new ResponseStatusException(CONFLICT, e.getMessage(), e))
                .onErrorMap(AlreadyPublishedError.class, e -> new ResponseStatusException(PRECONDITION_FAILED, e.getMessage(), e))
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> unpublishEmbroidery(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);
        String embroideryId = request.pathVariable("embroideryId");
        long version = getVersionQueryParam(request);

        return toAgent(request)
                .flatMap(agent -> module.unpublishEmbroidery(embroideryId, version, agent))
                .map(newVersion -> {
                    var response = new UnpublishEmbroideryResponse();
                    response.version = newVersion;
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorMap(AggregateVersionOutdatedError.class, e -> new ResponseStatusException(CONFLICT, e.getMessage(), e))
                .onErrorMap(AlreadyUnpublishedError.class, e -> new ResponseStatusException(PRECONDITION_FAILED, e.getMessage(), e))
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> featureEmbroidery(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);
        String embroideryId = request.pathVariable("embroideryId");
        long version = getVersionQueryParam(request);

        return toAgent(request)
                .flatMap(agent -> module.featureEmbroidery(embroideryId, version, agent))
                .map(newVersion -> {
                    var response = new FeatureEmbroideryResponse();
                    response.version = newVersion;
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorMap(AggregateVersionOutdatedError.class, e -> new ResponseStatusException(CONFLICT, e.getMessage(), e))
                .onErrorMap(AlreadyFeaturedError.class, e -> new ResponseStatusException(PRECONDITION_FAILED, e.getMessage(), e))
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> unfeatureEmbroidery(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);
        String embroideryId = request.pathVariable("embroideryId");
        long version = getVersionQueryParam(request);

        return toAgent(request)
                .flatMap(agent -> module.unfeatureEmbroidery(embroideryId, version, agent))
                .map(newVersion -> {
                    var response = new UnfeatureEmbroideryResponse();
                    response.version = newVersion;
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorMap(AggregateVersionOutdatedError.class, e -> new ResponseStatusException(CONFLICT, e.getMessage(), e))
                .onErrorMap(AlreadyUnfeaturedError.class, e -> new ResponseStatusException(PRECONDITION_FAILED, e.getMessage(), e))
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> updateEmbroideryImage(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);
        String embroideryId = request.pathVariable("embroideryId");

        return request.bodyToMono(UpdateEmbroideryImageRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.updateEmbroideryImage(
                        embroideryId,
                        req.version,
                        req.image,
                        agent
                )))
                .map(version -> {
                    var response = new UpdateEmbroideryImageResponse();
                    response.version = version;
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorMap(AggregateVersionOutdatedError.class, e -> new ResponseStatusException(CONFLICT, e.getMessage(), e))
                .onErrorMap(IllegalArgumentException.class, e -> new ResponseStatusException(BAD_REQUEST, e.getMessage(), e))
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> updateEmbroideryCategories(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);
        String embroideryId = request.pathVariable("embroideryId");

        return request.bodyToMono(UpdateEmbroideryCategoriesRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.updateEmbroideryCategories(
                        embroideryId,
                        req.version,
                        req.categories,
                        agent
                )))
                .map(version -> {
                    var response = new UpdateEmbroideryCategoriesResponse();
                    response.version = version;
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorMap(AggregateVersionOutdatedError.class, e -> new ResponseStatusException(CONFLICT, e.getMessage(), e))
                .onErrorMap(IllegalArgumentException.class, e -> new ResponseStatusException(BAD_REQUEST, e.getMessage(), e))
                .onErrorMap(CategoriesMissingError.class, e -> new ResponseStatusException(PRECONDITION_FAILED, e.getMessage(), e))
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> deleteEmbroidery(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);
        String embroideryId = request.pathVariable("embroideryId");
        long version = getVersionQueryParam(request);

        return toAgent(request)
                .flatMap(agent -> module.deleteEmbroidery(embroideryId, version, agent))
                .onErrorMap(AggregateVersionOutdatedError.class, e -> new ResponseStatusException(CONFLICT, e.getMessage(), e))
                .then(Mono.defer(() -> ServerResponse.ok().build()))
                .as(transactionalOperator::transactional);
    }

    private long getVersionQueryParam(ServerRequest request) {
        return request.queryParam("version")
                .map(Long::parseLong)
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Missing version query parameter"));
    }

    private Mono<ServerResponse> toAliasAlreadyInUseResponse(AliasAlreadyInUseError e) {
        return ServerResponse.status(PRECONDITION_FAILED)
                .bodyValue(Map.of(
                        "reason", "ALIAS_ALREADY_IN_USE",
                        "embroideryId", e.getConflictingEmbroideryId().getValue(),
                        "alias", e.getAlias().getValue()
                ));
    }

    private Mono<Agent> toAgent(ServerRequest request) {
        return request.principal()
                .map(principal -> Agent.user(AgentId.of(principal.getName())))
                .switchIfEmpty(Mono.just(Agent.anonymous()));
    }

    private EmbroideryDTO toEmbroideryDTO(EmbroideryDetails embroidery) {
        var result = new EmbroideryDTO();

        result.id = embroidery.getId().getValue();
        result.version = embroidery.getVersion().getValue();
        result.published = embroidery.isPublished();
        result.featured = embroidery.isFeatured();
        result.name = embroidery.getName().getValue();
        result.image = embroidery.getImage().getValue();
        result.categories = embroidery.getCategories()
                .stream()
                .map(EmbroideryCategoryId::getValue)
                .collect(toSet());
        result.createdAt = embroidery.getCreatedAt();

        return result;
    }

    private boolean isAscending(@Nullable EmbroideriesSortDTO sort) {
        return Optional.ofNullable(sort)
                .map(s -> s.direction != DESCENDING)
                .orElse(true);
    }

    private List<PublishedEmbroideryDTO> toPublishedEmbroideryDTOs(List<PublishedEmbroidery> embroideries) {
        return embroideries.stream()
                .map(this::toPublishedEmbroideryDTO)
                .toList();
    }

    private PublishedEmbroideryDTO toPublishedEmbroideryDTO(PublishedEmbroidery embroidery) {
        var result = new PublishedEmbroideryDTO();

        result.id = embroidery.getId().getValue();
        result.name = embroidery.getName().getValue();
        result.alias = embroidery.getAlias().getValue();
        result.image = embroidery.getImage().getValue();
        result.categories = embroidery.getCategories()
                .stream()
                .map(EmbroideryCategoryId::getValue)
                .collect(toSet());

        return result;
    }

    private List<CategoryDTO> toCategoryDTOs(List<EmbroideryCategory> categories) {
        return categories.stream()
                .map(this::toCategoryDTO)
                .toList();
    }

    private CategoryDTO toCategoryDTO(EmbroideryCategory category) {
        var result = new CategoryDTO();

        result.id = category.getId().getValue();
        result.name = category.getName().getValue();

        return result;
    }

}
