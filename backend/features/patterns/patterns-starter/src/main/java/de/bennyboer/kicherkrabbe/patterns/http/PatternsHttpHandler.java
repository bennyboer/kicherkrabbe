package de.bennyboer.kicherkrabbe.patterns.http;

import de.bennyboer.kicherkrabbe.changes.ResourceId;
import de.bennyboer.kicherkrabbe.eventsourcing.AggregateNotFoundError;
import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.money.Money;
import de.bennyboer.kicherkrabbe.patterns.*;
import de.bennyboer.kicherkrabbe.patterns.http.api.*;
import de.bennyboer.kicherkrabbe.patterns.http.api.requests.*;
import de.bennyboer.kicherkrabbe.patterns.http.api.responses.*;
import de.bennyboer.kicherkrabbe.patterns.feature.AlreadyFeaturedError;
import de.bennyboer.kicherkrabbe.patterns.publish.AlreadyPublishedError;
import de.bennyboer.kicherkrabbe.patterns.unfeature.AlreadyUnfeaturedError;
import de.bennyboer.kicherkrabbe.patterns.unpublish.AlreadyUnpublishedError;
import lombok.AllArgsConstructor;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toSet;
import static org.springframework.http.HttpStatus.*;

@AllArgsConstructor
public class PatternsHttpHandler {

    private final PatternsModule module;

    private final ReactiveTransactionManager transactionManager;

    public Mono<ServerResponse> getChanges(ServerRequest request) {
        var events$ = toAgent(request)
                .flatMapMany(module::getPatternChanges)
                .map(change -> {
                    var result = new PatternChangeDTO();

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
                .body(events$, PatternChangeDTO.class);
    }

    public Mono<ServerResponse> getAvailableCategoriesForPatterns(ServerRequest request) {
        return toAgent(request)
                .flatMapMany(module::getAvailableCategoriesForPatterns)
                .collectList()
                .map(categories -> {
                    var response = new QueryCategoriesResponse();
                    response.categories = toCategoryDTOs(categories);
                    return response;
                })
                .flatMap(result -> ServerResponse.ok().bodyValue(result));
    }

    public Mono<ServerResponse> getCategoriesUsedInPatterns(ServerRequest request) {
        return toAgent(request)
                .flatMapMany(module::getCategoriesUsedInPatterns)
                .collectList()
                .map(categories -> {
                    var response = new QueryCategoriesResponse();
                    response.categories = toCategoryDTOs(categories);
                    return response;
                })
                .flatMap(result -> ServerResponse.ok().bodyValue(result));
    }

    public Mono<ServerResponse> getPatterns(ServerRequest request) {
        return request.bodyToMono(QueryPatternsRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.getPatterns(
                        req.searchTerm,
                        req.categories,
                        req.skip,
                        req.limit,
                        agent
                )))
                .map(page -> {
                    var response = new QueryPatternsResponse();
                    response.patterns = toPatternDTOs(page.getResults());
                    response.total = page.getTotal();
                    response.skip = page.getSkip();
                    response.limit = page.getLimit();
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response));
    }

    public Mono<ServerResponse> getPattern(ServerRequest request) {
        String patternId = request.pathVariable("patternId");

        return toAgent(request)
                .flatMap(agent -> module.getPattern(patternId, agent))
                .map(pattern -> {
                    var response = new QueryPatternResponse();
                    response.pattern = toPatternDTO(pattern);
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorMap(
                        AggregateNotFoundError.class, e -> new ResponseStatusException(
                                NOT_FOUND,
                                e.getMessage(),
                                e
                        )
                );
    }

    public Mono<ServerResponse> getPublishedPatterns(ServerRequest request) {
        return request.bodyToMono(QueryPublishedPatternsRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.getPublishedPatterns(
                        req.searchTerm,
                        req.categories,
                        req.sizes,
                        req.sort,
                        req.skip,
                        req.limit,
                        agent
                )))
                .map(page -> {
                    var response = new QueryPublishedPatternsResponse();
                    response.patterns = toPublishedPatternDTOs(page.getResults());
                    response.total = page.getTotal();
                    response.skip = page.getSkip();
                    response.limit = page.getLimit();
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response));
    }

    public Mono<ServerResponse> getPublishedPattern(ServerRequest request) {
        String patternId = request.pathVariable("patternId");

        return toAgent(request)
                .flatMap(agent -> module.getPublishedPattern(patternId, agent))
                .map(pattern -> {
                    var response = new QueryPublishedPatternResponse();
                    response.pattern = toPublishedPatternDTO(pattern);
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .switchIfEmpty(Mono.error(new ResponseStatusException(NOT_FOUND, "Pattern not available")));
    }

    public Mono<ServerResponse> createPattern(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        return request.bodyToMono(CreatePatternRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.createPattern(
                        req.name,
                        req.number,
                        req.description,
                        req.attribution,
                        req.categories,
                        req.images,
                        req.variants,
                        req.extras,
                        agent
                )))
                .map(patternId -> {
                    var result = new CreatePatternResponse();
                    result.id = patternId;
                    return result;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorMap(
                        IllegalArgumentException.class, e -> new ResponseStatusException(
                                BAD_REQUEST,
                                e.getMessage(),
                                e
                        )
                )
                .onErrorMap(
                        CategoriesMissingError.class, e -> new ResponseStatusException(
                                PRECONDITION_FAILED,
                                e.getMessage(),
                                e
                        )
                )
                .onErrorResume(
                        NumberAlreadyInUseError.class, e -> ServerResponse.status(PRECONDITION_FAILED)
                                .bodyValue(Map.of(
                                        "reason", "NUMBER_ALREADY_IN_USE",
                                        "patternId", e.getConflictingPatternId().getValue(),
                                        "number", e.getNumber().getValue()
                                ))
                )
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> renamePattern(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        String patternId = request.pathVariable("patternId");

        return request.bodyToMono(RenamePatternRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.renamePattern(
                        patternId,
                        req.version,
                        req.name,
                        agent
                )))
                .map(version -> {
                    var response = new RenamePatternResponse();
                    response.version = version;
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorMap(
                        AggregateVersionOutdatedError.class, e -> new ResponseStatusException(
                                CONFLICT,
                                e.getMessage(),
                                e
                        )
                )
                .onErrorMap(
                        IllegalArgumentException.class, e -> new ResponseStatusException(
                                BAD_REQUEST,
                                e.getMessage(),
                                e
                        )
                )
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> publishPattern(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        String patternId = request.pathVariable("patternId");
        long version = request.queryParam("version")
                .map(Long::parseLong)
                .orElseThrow(() -> new ResponseStatusException(
                        BAD_REQUEST,
                        "Missing version query parameter"
                ));

        return toAgent(request)
                .flatMap(agent -> module.publishPattern(patternId, version, agent))
                .map(newVersion -> {
                    var response = new PublishPatternResponse();
                    response.version = newVersion;
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorMap(
                        AggregateVersionOutdatedError.class, e -> new ResponseStatusException(
                                CONFLICT,
                                e.getMessage(),
                                e
                        )
                )
                .onErrorMap(
                        AlreadyPublishedError.class, e -> new ResponseStatusException(
                                PRECONDITION_FAILED,
                                e.getMessage(),
                                e
                        )
                )
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> unpublishPattern(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        String patternId = request.pathVariable("patternId");
        long version = request.queryParam("version")
                .map(Long::parseLong)
                .orElseThrow(() -> new ResponseStatusException(
                        BAD_REQUEST,
                        "Missing version query parameter"
                ));

        return toAgent(request)
                .flatMap(agent -> module.unpublishPattern(patternId, version, agent))
                .map(newVersion -> {
                    var response = new UnpublishPatternResponse();
                    response.version = newVersion;
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorMap(
                        AggregateVersionOutdatedError.class, e -> new ResponseStatusException(
                                CONFLICT,
                                e.getMessage(),
                                e
                        )
                )
                .onErrorMap(
                        AlreadyUnpublishedError.class, e -> new ResponseStatusException(
                                PRECONDITION_FAILED,
                                e.getMessage(),
                                e
                        )
                )
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> featurePattern(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        String patternId = request.pathVariable("patternId");
        long version = request.queryParam("version")
                .map(Long::parseLong)
                .orElseThrow(() -> new ResponseStatusException(
                        BAD_REQUEST,
                        "Missing version query parameter"
                ));

        return toAgent(request)
                .flatMap(agent -> module.featurePattern(patternId, version, agent))
                .map(newVersion -> {
                    var response = new FeaturePatternResponse();
                    response.version = newVersion;
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorMap(
                        AggregateVersionOutdatedError.class, e -> new ResponseStatusException(
                                CONFLICT,
                                e.getMessage(),
                                e
                        )
                )
                .onErrorMap(
                        AlreadyFeaturedError.class, e -> new ResponseStatusException(
                                PRECONDITION_FAILED,
                                e.getMessage(),
                                e
                        )
                )
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> unfeaturePattern(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        String patternId = request.pathVariable("patternId");
        long version = request.queryParam("version")
                .map(Long::parseLong)
                .orElseThrow(() -> new ResponseStatusException(
                        BAD_REQUEST,
                        "Missing version query parameter"
                ));

        return toAgent(request)
                .flatMap(agent -> module.unfeaturePattern(patternId, version, agent))
                .map(newVersion -> {
                    var response = new UnfeaturePatternResponse();
                    response.version = newVersion;
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorMap(
                        AggregateVersionOutdatedError.class, e -> new ResponseStatusException(
                                CONFLICT,
                                e.getMessage(),
                                e
                        )
                )
                .onErrorMap(
                        AlreadyUnfeaturedError.class, e -> new ResponseStatusException(
                                PRECONDITION_FAILED,
                                e.getMessage(),
                                e
                        )
                )
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> updatePatternVariants(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        String patternId = request.pathVariable("patternId");

        return request.bodyToMono(UpdatePatternVariantsRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.updatePatternVariants(
                        patternId,
                        req.version,
                        req.variants,
                        agent
                )))
                .map(version -> {
                    var response = new UpdatePatternVariantsResponse();
                    response.version = version;
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorMap(
                        AggregateVersionOutdatedError.class, e -> new ResponseStatusException(
                                CONFLICT,
                                e.getMessage(),
                                e
                        )
                )
                .onErrorMap(
                        IllegalArgumentException.class, e -> new ResponseStatusException(
                                BAD_REQUEST,
                                e.getMessage(),
                                e
                        )
                )
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> updatePatternAttribution(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        String patternId = request.pathVariable("patternId");

        return request.bodyToMono(UpdatePatternAttributionRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.updatePatternAttribution(
                        patternId,
                        req.version,
                        req.attribution,
                        agent
                )))
                .map(version -> {
                    var response = new UpdatePatternAttributionResponse();
                    response.version = version;
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorMap(
                        AggregateVersionOutdatedError.class, e -> new ResponseStatusException(
                                CONFLICT,
                                e.getMessage(),
                                e
                        )
                )
                .onErrorMap(
                        IllegalArgumentException.class, e -> new ResponseStatusException(
                                BAD_REQUEST,
                                e.getMessage(),
                                e
                        )
                )
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> updatePatternCategories(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        String patternId = request.pathVariable("patternId");

        return request.bodyToMono(UpdatePatternCategoriesRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.updatePatternCategories(
                        patternId,
                        req.version,
                        req.categories,
                        agent
                )))
                .map(version -> {
                    var response = new UpdatePatternCategoriesResponse();
                    response.version = version;
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorMap(
                        AggregateVersionOutdatedError.class, e -> new ResponseStatusException(
                                CONFLICT,
                                e.getMessage(),
                                e
                        )
                )
                .onErrorMap(
                        IllegalArgumentException.class, e -> new ResponseStatusException(
                                BAD_REQUEST,
                                e.getMessage(),
                                e
                        )
                )
                .onErrorMap(
                        CategoriesMissingError.class, e -> new ResponseStatusException(
                                PRECONDITION_FAILED,
                                e.getMessage(),
                                e
                        )
                )
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> updatePatternImages(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        String patternId = request.pathVariable("patternId");

        return request.bodyToMono(UpdatePatternImagesRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.updatePatternImages(
                        patternId,
                        req.version,
                        req.images,
                        agent
                )))
                .map(version -> {
                    var response = new UpdatePatternImagesResponse();
                    response.version = version;
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorMap(
                        AggregateVersionOutdatedError.class, e -> new ResponseStatusException(
                                CONFLICT,
                                e.getMessage(),
                                e
                        )
                )
                .onErrorMap(
                        IllegalArgumentException.class, e -> new ResponseStatusException(
                                BAD_REQUEST,
                                e.getMessage(),
                                e
                        )
                )
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> updatePatternExtras(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        String patternId = request.pathVariable("patternId");

        return request.bodyToMono(UpdatePatternExtrasRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.updatePatternExtras(
                        patternId,
                        req.version,
                        req.extras,
                        agent
                )))
                .map(version -> {
                    var response = new UpdatePatternExtrasResponse();
                    response.version = version;
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorMap(
                        AggregateVersionOutdatedError.class, e -> new ResponseStatusException(
                                CONFLICT,
                                e.getMessage(),
                                e
                        )
                )
                .onErrorMap(
                        IllegalArgumentException.class, e -> new ResponseStatusException(
                                BAD_REQUEST,
                                e.getMessage(),
                                e
                        )
                )
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> updatePatternDescription(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        String patternId = request.pathVariable("patternId");

        return request.bodyToMono(UpdatePatternDescriptionRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.updatePatternDescription(
                        patternId,
                        req.version,
                        req.description,
                        agent
                )))
                .map(version -> {
                    var response = new UpdatePatternDescriptionResponse();
                    response.version = version;
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorMap(
                        AggregateVersionOutdatedError.class, e -> new ResponseStatusException(
                                CONFLICT,
                                e.getMessage(),
                                e
                        )
                )
                .onErrorMap(
                        IllegalArgumentException.class, e -> new ResponseStatusException(
                                BAD_REQUEST,
                                e.getMessage(),
                                e
                        )
                )
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> updatePatternNumber(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        String patternId = request.pathVariable("patternId");

        return request.bodyToMono(UpdatePatternNumberRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.updatePatternNumber(
                        patternId,
                        req.version,
                        req.number,
                        agent
                )))
                .map(version -> {
                    var response = new UpdatePatternNumberResponse();
                    response.version = version;
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorMap(
                        AggregateVersionOutdatedError.class, e -> new ResponseStatusException(
                                CONFLICT,
                                e.getMessage(),
                                e
                        )
                )
                .onErrorMap(
                        IllegalArgumentException.class, e -> new ResponseStatusException(
                                BAD_REQUEST,
                                e.getMessage(),
                                e
                        )
                )
                .onErrorResume(
                        NumberAlreadyInUseError.class, e -> ServerResponse.status(PRECONDITION_FAILED)
                                .bodyValue(Map.of(
                                        "reason", "NUMBER_ALREADY_IN_USE",
                                        "patternId", e.getConflictingPatternId().getValue(),
                                        "number", e.getNumber().getValue()
                                ))
                )
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> deletePattern(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        String patternId = request.pathVariable("patternId");
        long version = request.queryParam("version")
                .map(Long::parseLong)
                .orElseThrow(() -> new ResponseStatusException(
                        BAD_REQUEST,
                        "Missing version query parameter"
                ));

        return toAgent(request)
                .flatMap(agent -> module.deletePattern(patternId, version, agent))
                .onErrorMap(
                        AggregateVersionOutdatedError.class, e -> new ResponseStatusException(
                                CONFLICT,
                                e.getMessage(),
                                e
                        )
                )
                .then(Mono.defer(() -> ServerResponse.ok().build()))
                .as(transactionalOperator::transactional);
    }

    private Mono<Agent> toAgent(ServerRequest request) {
        return request.principal()
                .map(principal -> Agent.user(AgentId.of(principal.getName())))
                .switchIfEmpty(Mono.just(Agent.anonymous()));
    }

    private List<PublishedPatternDTO> toPublishedPatternDTOs(List<PublishedPattern> patterns) {
        return patterns.stream()
                .map(this::toPublishedPatternDTO)
                .toList();
    }

    private PublishedPatternDTO toPublishedPatternDTO(PublishedPattern pattern) {
        var result = new PublishedPatternDTO();

        result.id = pattern.getId().getValue();
        result.name = pattern.getName().getValue();
        result.number = pattern.getNumber().getValue();
        result.description = pattern.getDescription()
                .map(PatternDescription::getValue)
                .orElse(null);
        result.alias = pattern.getAlias().getValue();
        result.attribution = toPatternAttributionDTO(pattern.getAttribution());
        result.categories = pattern.getCategories()
                .stream()
                .map(PatternCategoryId::getValue)
                .collect(toSet());
        result.images = pattern.getImages()
                .stream()
                .map(ImageId::getValue)
                .toList();
        result.variants = toVariantDTOs(pattern.getVariants());
        result.extras = toExtraDTOs(pattern.getExtras());

        return result;
    }

    private List<PatternDTO> toPatternDTOs(List<PatternDetails> patterns) {
        return patterns.stream()
                .map(this::toPatternDTO)
                .toList();
    }

    private PatternDTO toPatternDTO(PatternDetails pattern) {
        var result = new PatternDTO();

        result.id = pattern.getId().getValue();
        result.version = pattern.getVersion().getValue();
        result.published = pattern.isPublished();
        result.name = pattern.getName().getValue();
        result.number = pattern.getNumber().getValue();
        result.description = pattern.getDescription()
                .map(PatternDescription::getValue)
                .orElse(null);
        result.attribution = toPatternAttributionDTO(pattern.getAttribution());
        result.categories = pattern.getCategories()
                .stream()
                .map(PatternCategoryId::getValue)
                .collect(toSet());
        result.images = pattern.getImages()
                .stream()
                .map(ImageId::getValue)
                .toList();
        result.variants = toVariantDTOs(pattern.getVariants());
        result.extras = toExtraDTOs(pattern.getExtras());
        result.createdAt = pattern.getCreatedAt();

        return result;
    }

    private List<PatternExtraDTO> toExtraDTOs(List<PatternExtra> extras) {
        return extras.stream()
                .map(this::toExtraDTO)
                .toList();
    }

    private PatternExtraDTO toExtraDTO(PatternExtra patternExtra) {
        var result = new PatternExtraDTO();

        result.name = patternExtra.getName().getValue();
        result.price = toMoneyDTO(patternExtra.getPrice());

        return result;
    }

    private List<PatternVariantDTO> toVariantDTOs(List<PatternVariant> variants) {
        return variants.stream()
                .map(this::toVariantDTO)
                .toList();
    }

    private PatternVariantDTO toVariantDTO(PatternVariant patternVariant) {
        var result = new PatternVariantDTO();

        result.name = patternVariant.getName().getValue();
        result.pricedSizeRanges = patternVariant.getPricedSizeRanges()
                .stream()
                .map(this::toPricedSizeRangeDTO)
                .collect(toSet());

        return result;
    }

    private PricedSizeRangeDTO toPricedSizeRangeDTO(PricedSizeRange pricedSizeRange) {
        var result = new PricedSizeRangeDTO();

        result.from = pricedSizeRange.getFrom();
        result.to = pricedSizeRange.getTo().orElse(null);
        result.unit = pricedSizeRange.getUnit().orElse(null);
        result.price = toMoneyDTO(pricedSizeRange.getPrice());

        return result;
    }

    private MoneyDTO toMoneyDTO(Money money) {
        var result = new MoneyDTO();

        result.amount = money.getAmount();
        result.currency = money.getCurrency().getShortForm();

        return result;
    }

    private PatternAttributionDTO toPatternAttributionDTO(PatternAttribution attribution) {
        var result = new PatternAttributionDTO();

        result.originalPatternName = attribution.getOriginalPatternName()
                .map(OriginalPatternName::getValue)
                .orElse(null);
        result.designer = attribution.getDesigner()
                .map(PatternDesigner::getValue)
                .orElse(null);

        return result;
    }

    private List<CategoryDTO> toCategoryDTOs(List<PatternCategory> categories) {
        return categories.stream()
                .map(this::toCategoryDTO)
                .toList();
    }

    private CategoryDTO toCategoryDTO(PatternCategory patternCategory) {
        var result = new CategoryDTO();

        result.id = patternCategory.getId().getValue();
        result.name = patternCategory.getName().getValue();

        return result;
    }

}
