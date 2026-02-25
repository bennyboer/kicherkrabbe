package de.bennyboer.kicherkrabbe.offers.http;

import de.bennyboer.kicherkrabbe.changes.ResourceId;
import de.bennyboer.kicherkrabbe.eventsourcing.AggregateNotFoundError;
import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.offers.*;
import de.bennyboer.kicherkrabbe.offers.api.*;
import de.bennyboer.kicherkrabbe.offers.api.requests.*;
import de.bennyboer.kicherkrabbe.offers.api.responses.*;
import de.bennyboer.kicherkrabbe.offers.persistence.lookup.product.LookupProduct;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import de.bennyboer.kicherkrabbe.offers.archive.NotReservedForArchiveError;
import de.bennyboer.kicherkrabbe.offers.delete.CannotDeleteNonDraftError;
import de.bennyboer.kicherkrabbe.offers.publish.AlreadyPublishedError;
import de.bennyboer.kicherkrabbe.offers.reserve.AlreadyReservedError;
import de.bennyboer.kicherkrabbe.offers.reserve.NotPublishedError;
import de.bennyboer.kicherkrabbe.offers.unpublish.AlreadyUnpublishedError;
import de.bennyboer.kicherkrabbe.offers.unpublish.CannotUnpublishReservedError;
import de.bennyboer.kicherkrabbe.offers.unreserve.NotReservedError;
import lombok.AllArgsConstructor;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

@AllArgsConstructor
public class OffersHttpHandler {

    private final OffersModule module;

    private final ReactiveTransactionManager transactionManager;

    public Mono<ServerResponse> getChanges(ServerRequest request) {
        var events$ = toAgent(request)
                .flatMapMany(module::getOfferChanges)
                .map(change -> {
                    var result = new OfferChangeDTO();
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
                .body(events$, OfferChangeDTO.class);
    }

    public Mono<ServerResponse> getOffers(ServerRequest request) {
        return request.bodyToMono(QueryOffersRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.getOffers(
                        req.searchTerm,
                        req.skip,
                        req.limit,
                        agent
                )))
                .map(page -> {
                    var response = new QueryOffersResponse();
                    response.offers = toOfferDTOs(page.getResults());
                    response.total = page.getTotal();
                    response.skip = page.getSkip();
                    response.limit = page.getLimit();
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorResume(MissingPermissionError.class, e -> ServerResponse.status(FORBIDDEN).build());
    }

    public Mono<ServerResponse> getOffer(ServerRequest request) {
        String offerId = request.pathVariable("offerId");

        return toAgent(request)
                .flatMap(agent -> module.getOffer(offerId, agent))
                .map(offer -> {
                    var response = new QueryOfferResponse();
                    response.offer = toOfferDTO(offer);
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorResume(MissingPermissionError.class, e -> ServerResponse.status(FORBIDDEN).build())
                .onErrorMap(AggregateNotFoundError.class, e -> new ResponseStatusException(
                        NOT_FOUND,
                        e.getMessage(),
                        e
                ));
    }

    public Mono<ServerResponse> getPublishedOffers(ServerRequest request) {
        return request.bodyToMono(QueryPublishedOffersRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.getPublishedOffers(
                        req.searchTerm,
                        req.categories,
                        req.sizes,
                        req.priceRange,
                        req.sort,
                        req.skip,
                        req.limit,
                        agent
                )))
                .map(page -> {
                    var response = new QueryPublishedOffersResponse();
                    response.offers = toPublishedOfferDTOs(page.getResults());
                    response.total = page.getTotal();
                    response.skip = page.getSkip();
                    response.limit = page.getLimit();
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorResume(MissingPermissionError.class, e -> ServerResponse.status(FORBIDDEN).build());
    }

    public Mono<ServerResponse> getAvailableSizesForOffers(ServerRequest request) {
        return toAgent(request)
                .flatMapMany(module::getAvailableSizesForOffers)
                .collectList()
                .map(sizes -> {
                    var response = new QueryAvailableSizesForOffersResponse();
                    response.sizes = sizes;
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorResume(MissingPermissionError.class, e -> ServerResponse.status(FORBIDDEN).build());
    }

    public Mono<ServerResponse> getPublishedOffer(ServerRequest request) {
        String offerId = request.pathVariable("offerId");

        return toAgent(request)
                .flatMap(agent -> module.getPublishedOffer(offerId, agent))
                .map(offer -> {
                    var response = new QueryPublishedOfferResponse();
                    response.offer = toPublishedOfferDTO(offer);
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .switchIfEmpty(Mono.error(new ResponseStatusException(NOT_FOUND, "Offer not available")));
    }

    public Mono<ServerResponse> getProducts(ServerRequest request) {
        return request.bodyToMono(QueryProductsForOfferCreationRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.getProductsForOfferCreation(
                        req.searchTerm,
                        req.skip,
                        req.limit,
                        agent
                )))
                .map(page -> {
                    var response = new QueryProductsForOfferCreationResponse();
                    response.products = page.getResults().stream()
                            .map(this::toProductForOfferCreationDTO)
                            .toList();
                    response.total = page.getTotal();
                    response.skip = page.getSkip();
                    response.limit = page.getLimit();
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorResume(MissingPermissionError.class, e -> ServerResponse.status(FORBIDDEN).build());
    }

    public Mono<ServerResponse> getAvailableCategoriesForOffers(ServerRequest request) {
        return toAgent(request)
                .flatMapMany(module::getAvailableCategoriesForOffers)
                .map(category -> {
                    var dto = new OfferCategoryDTO();
                    dto.id = category.getId().getValue();
                    dto.name = category.getName().getValue();
                    return dto;
                })
                .collectList()
                .map(categories -> {
                    var response = new QueryAvailableCategoriesForOffersResponse();
                    response.categories = categories;
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorResume(MissingPermissionError.class, e -> ServerResponse.status(FORBIDDEN).build());
    }

    public Mono<ServerResponse> createOffer(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        return request.bodyToMono(CreateOfferRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.createOffer(
                        req.title,
                        req.size,
                        req.categoryIds,
                        req.productId,
                        req.imageIds,
                        req.notes,
                        req.price,
                        agent
                )))
                .map(offerId -> {
                    var result = new CreateOfferResponse();
                    result.id = offerId;
                    return result;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorMap(IllegalArgumentException.class, e -> new ResponseStatusException(
                        BAD_REQUEST,
                        e.getMessage(),
                        e
                ))
                .onErrorResume(
                        AliasAlreadyInUseError.class, e -> ServerResponse.status(PRECONDITION_FAILED)
                                .bodyValue(Map.of(
                                        "reason", "ALIAS_ALREADY_IN_USE",
                                        "offerId", e.getConflictingOfferId().getValue(),
                                        "alias", e.getAlias().getValue()
                                ))
                )
                .onErrorResume(MissingPermissionError.class, e -> ServerResponse.status(FORBIDDEN).build())
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> deleteOffer(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        String offerId = request.pathVariable("offerId");
        long version = request.queryParam("version")
                .map(Long::parseLong)
                .orElseThrow(() -> new ResponseStatusException(
                        BAD_REQUEST,
                        "Missing version query parameter"
                ));

        return toAgent(request)
                .flatMap(agent -> module.deleteOffer(offerId, version, agent))
                .onErrorMap(AggregateVersionOutdatedError.class, e -> new ResponseStatusException(
                        CONFLICT,
                        e.getMessage(),
                        e
                ))
                .onErrorMap(CannotDeleteNonDraftError.class, e -> new ResponseStatusException(
                        CONFLICT,
                        e.getMessage(),
                        e
                ))
                .then(Mono.defer(() -> ServerResponse.ok().build()))
                .onErrorResume(MissingPermissionError.class, e -> ServerResponse.status(FORBIDDEN).build())
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> publishOffer(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        String offerId = request.pathVariable("offerId");
        long version = request.queryParam("version")
                .map(Long::parseLong)
                .orElseThrow(() -> new ResponseStatusException(
                        BAD_REQUEST,
                        "Missing version query parameter"
                ));

        return toAgent(request)
                .flatMap(agent -> module.publishOffer(offerId, version, agent))
                .map(newVersion -> {
                    var response = new PublishOfferResponse();
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
                .onErrorResume(MissingPermissionError.class, e -> ServerResponse.status(FORBIDDEN).build())
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> unpublishOffer(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        String offerId = request.pathVariable("offerId");
        long version = request.queryParam("version")
                .map(Long::parseLong)
                .orElseThrow(() -> new ResponseStatusException(
                        BAD_REQUEST,
                        "Missing version query parameter"
                ));

        return toAgent(request)
                .flatMap(agent -> module.unpublishOffer(offerId, version, agent))
                .map(newVersion -> {
                    var response = new UnpublishOfferResponse();
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
                .onErrorMap(CannotUnpublishReservedError.class, e -> new ResponseStatusException(
                        CONFLICT,
                        e.getMessage(),
                        e
                ))
                .onErrorResume(MissingPermissionError.class, e -> ServerResponse.status(FORBIDDEN).build())
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> reserveOffer(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        String offerId = request.pathVariable("offerId");
        long version = request.queryParam("version")
                .map(Long::parseLong)
                .orElseThrow(() -> new ResponseStatusException(
                        BAD_REQUEST,
                        "Missing version query parameter"
                ));

        return toAgent(request)
                .flatMap(agent -> module.reserveOffer(offerId, version, agent))
                .map(newVersion -> {
                    var response = new ReserveOfferResponse();
                    response.version = newVersion;
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorMap(AggregateVersionOutdatedError.class, e -> new ResponseStatusException(
                        CONFLICT,
                        e.getMessage(),
                        e
                ))
                .onErrorMap(NotPublishedError.class, e -> new ResponseStatusException(
                        PRECONDITION_FAILED,
                        e.getMessage(),
                        e
                ))
                .onErrorMap(AlreadyReservedError.class, e -> new ResponseStatusException(
                        PRECONDITION_FAILED,
                        e.getMessage(),
                        e
                ))
                .onErrorResume(MissingPermissionError.class, e -> ServerResponse.status(FORBIDDEN).build())
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> unreserveOffer(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        String offerId = request.pathVariable("offerId");
        long version = request.queryParam("version")
                .map(Long::parseLong)
                .orElseThrow(() -> new ResponseStatusException(
                        BAD_REQUEST,
                        "Missing version query parameter"
                ));

        return toAgent(request)
                .flatMap(agent -> module.unreserveOffer(offerId, version, agent))
                .map(newVersion -> {
                    var response = new UnreserveOfferResponse();
                    response.version = newVersion;
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorMap(AggregateVersionOutdatedError.class, e -> new ResponseStatusException(
                        CONFLICT,
                        e.getMessage(),
                        e
                ))
                .onErrorMap(NotReservedError.class, e -> new ResponseStatusException(
                        PRECONDITION_FAILED,
                        e.getMessage(),
                        e
                ))
                .onErrorResume(MissingPermissionError.class, e -> ServerResponse.status(FORBIDDEN).build())
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> archiveOffer(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        String offerId = request.pathVariable("offerId");
        long version = request.queryParam("version")
                .map(Long::parseLong)
                .orElseThrow(() -> new ResponseStatusException(
                        BAD_REQUEST,
                        "Missing version query parameter"
                ));

        return toAgent(request)
                .flatMap(agent -> module.archiveOffer(offerId, version, agent))
                .map(newVersion -> {
                    var response = new ArchiveOfferResponse();
                    response.version = newVersion;
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorMap(AggregateVersionOutdatedError.class, e -> new ResponseStatusException(
                        CONFLICT,
                        e.getMessage(),
                        e
                ))
                .onErrorMap(NotReservedForArchiveError.class, e -> new ResponseStatusException(
                        PRECONDITION_FAILED,
                        e.getMessage(),
                        e
                ))
                .onErrorResume(MissingPermissionError.class, e -> ServerResponse.status(FORBIDDEN).build())
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> updateImages(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        String offerId = request.pathVariable("offerId");

        return request.bodyToMono(UpdateImagesRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.updateOfferImages(
                        offerId,
                        req.version,
                        req.imageIds,
                        agent
                )))
                .map(version -> {
                    var response = new UpdateImagesResponse();
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
                .onErrorResume(MissingPermissionError.class, e -> ServerResponse.status(FORBIDDEN).build())
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> updateNotes(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        String offerId = request.pathVariable("offerId");

        return request.bodyToMono(UpdateNotesRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.updateOfferNotes(
                        offerId,
                        req.version,
                        req.notes,
                        agent
                )))
                .map(version -> {
                    var response = new UpdateNotesResponse();
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
                .onErrorResume(MissingPermissionError.class, e -> ServerResponse.status(FORBIDDEN).build())
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> updatePrice(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        String offerId = request.pathVariable("offerId");

        return request.bodyToMono(UpdatePriceRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.updateOfferPrice(
                        offerId,
                        req.version,
                        req.price,
                        agent
                )))
                .map(version -> {
                    var response = new UpdatePriceResponse();
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
                .onErrorResume(MissingPermissionError.class, e -> ServerResponse.status(FORBIDDEN).build())
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> addDiscount(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        String offerId = request.pathVariable("offerId");

        return request.bodyToMono(AddDiscountRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.addOfferDiscount(
                        offerId,
                        req.version,
                        req.discountedPrice,
                        agent
                )))
                .map(version -> {
                    var response = new AddDiscountResponse();
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
                .onErrorResume(MissingPermissionError.class, e -> ServerResponse.status(FORBIDDEN).build())
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> removeDiscount(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        String offerId = request.pathVariable("offerId");
        long version = request.queryParam("version")
                .map(Long::parseLong)
                .orElseThrow(() -> new ResponseStatusException(
                        BAD_REQUEST,
                        "Missing version query parameter"
                ));

        return toAgent(request)
                .flatMap(agent -> module.removeOfferDiscount(offerId, version, agent))
                .map(newVersion -> {
                    var response = new RemoveDiscountResponse();
                    response.version = newVersion;
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorMap(AggregateVersionOutdatedError.class, e -> new ResponseStatusException(
                        CONFLICT,
                        e.getMessage(),
                        e
                ))
                .onErrorResume(MissingPermissionError.class, e -> ServerResponse.status(FORBIDDEN).build())
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> updateTitle(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        String offerId = request.pathVariable("offerId");

        return request.bodyToMono(UpdateTitleRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.updateOfferTitle(
                        offerId,
                        req.version,
                        req.title,
                        agent
                )))
                .map(version -> {
                    var response = new UpdateTitleResponse();
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
                .onErrorResume(
                        AliasAlreadyInUseError.class, e -> ServerResponse.status(PRECONDITION_FAILED)
                                .bodyValue(Map.of(
                                        "reason", "ALIAS_ALREADY_IN_USE",
                                        "offerId", e.getConflictingOfferId().getValue(),
                                        "alias", e.getAlias().getValue()
                                ))
                )
                .onErrorResume(MissingPermissionError.class, e -> ServerResponse.status(FORBIDDEN).build())
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> updateSize(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        String offerId = request.pathVariable("offerId");

        return request.bodyToMono(UpdateSizeRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.updateOfferSize(
                        offerId,
                        req.version,
                        req.size,
                        agent
                )))
                .map(version -> {
                    var response = new UpdateSizeResponse();
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
                .onErrorResume(MissingPermissionError.class, e -> ServerResponse.status(FORBIDDEN).build())
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> updateCategories(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        String offerId = request.pathVariable("offerId");

        return request.bodyToMono(UpdateCategoriesRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.updateOfferCategories(
                        offerId,
                        req.version,
                        req.categoryIds,
                        agent
                )))
                .map(version -> {
                    var response = new UpdateCategoriesResponse();
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
                .onErrorResume(MissingPermissionError.class, e -> ServerResponse.status(FORBIDDEN).build())
                .as(transactionalOperator::transactional);
    }

    private Mono<Agent> toAgent(ServerRequest request) {
        return request.principal()
                .map(principal -> Agent.user(AgentId.of(principal.getName())))
                .switchIfEmpty(Mono.just(Agent.anonymous()));
    }

    private List<OfferDTO> toOfferDTOs(List<OfferDetails> offers) {
        return offers.stream()
                .map(this::toOfferDTO)
                .toList();
    }

    private OfferDTO toOfferDTO(OfferDetails offer) {
        var result = new OfferDTO();
        result.id = offer.getId().getValue();
        result.version = offer.getVersion().getValue();
        result.title = offer.getTitle().getValue();
        result.size = offer.getSize().getValue();
        result.categoryIds = offer.getCategories().stream().map(OfferCategoryId::getValue).collect(Collectors.toSet());
        result.product = toProductDTO(offer.getProduct());
        result.imageIds = offer.getImages().stream().map(ImageId::getValue).toList();
        result.links = toLinkDTOs(offer.getLinks());
        result.fabricCompositionItems = toFabricCompositionItemDTOs(offer.getFabricComposition());
        result.pricing = toPricingDTO(offer.getPricing());
        result.notes = toNotesDTO(offer.getNotes());
        result.published = offer.isPublished();
        result.reserved = offer.isReserved();
        result.createdAt = offer.getCreatedAt();
        result.archivedAt = offer.getArchivedAt().orElse(null);
        return result;
    }

    private List<PublishedOfferDTO> toPublishedOfferDTOs(List<PublishedOffer> offers) {
        return offers.stream()
                .map(this::toPublishedOfferDTO)
                .toList();
    }

    private PublishedOfferDTO toPublishedOfferDTO(PublishedOffer offer) {
        var result = new PublishedOfferDTO();
        result.id = offer.getId().getValue();
        result.alias = offer.getAlias().getValue();
        result.title = offer.getTitle().getValue();
        result.size = offer.getSize().getValue();
        result.categoryIds = offer.getCategories().stream().map(OfferCategoryId::getValue).collect(Collectors.toSet());
        result.imageIds = offer.getImages().stream().map(ImageId::getValue).toList();
        result.links = toLinkDTOs(offer.getLinks());
        result.fabricCompositionItems = toFabricCompositionItemDTOs(offer.getFabricComposition());
        result.pricing = toPricingDTO(offer.getPricing());
        result.notes = toNotesDTO(offer.getNotes());
        return result;
    }

    private ProductForOfferCreationDTO toProductForOfferCreationDTO(LookupProduct product) {
        var dto = new ProductForOfferCreationDTO();
        dto.id = product.getId().getValue();
        dto.number = product.getNumber().getValue();
        dto.imageIds = product.getImages().stream().map(ImageId::getValue).toList();
        dto.links = toLinkDTOs(product.getLinks());
        dto.fabricCompositionItems = toFabricCompositionItemDTOs(product.getFabricComposition());
        return dto;
    }

    private ProductDTO toProductDTO(Product product) {
        var dto = new ProductDTO();
        dto.id = product.getId().getValue();
        dto.number = product.getNumber().getValue();
        return dto;
    }

    private Set<LinkDTO> toLinkDTOs(Links links) {
        return links.getLinks().stream()
                .map(link -> {
                    var dto = new LinkDTO();
                    dto.type = LinkTypeDTO.valueOf(link.getType().name());
                    dto.id = link.getId().getValue();
                    dto.name = link.getName().getValue();
                    return dto;
                })
                .collect(Collectors.toSet());
    }

    private Set<FabricCompositionItemDTO> toFabricCompositionItemDTOs(FabricComposition composition) {
        return composition.getItems().stream()
                .map(item -> {
                    var dto = new FabricCompositionItemDTO();
                    dto.fabricType = FabricTypeDTO.valueOf(item.getFabricType().name());
                    dto.percentage = item.getPercentage().getValue();
                    return dto;
                })
                .collect(Collectors.toSet());
    }

    private PricingDTO toPricingDTO(Pricing pricing) {
        var dto = new PricingDTO();
        dto.price = toMoneyDTO(pricing.getPrice());
        dto.discountedPrice = pricing.getDiscountedPrice()
                .map(this::toMoneyDTO)
                .orElse(null);
        dto.priceHistory = pricing.getPriceHistory().stream()
                .map(entry -> {
                    var entryDTO = new PriceHistoryEntryDTO();
                    entryDTO.price = toMoneyDTO(entry.getPrice());
                    entryDTO.timestamp = entry.getTimestamp();
                    return entryDTO;
                })
                .toList();
        return dto;
    }

    private MoneyDTO toMoneyDTO(de.bennyboer.kicherkrabbe.money.Money money) {
        var dto = new MoneyDTO();
        dto.amount = money.getAmount();
        dto.currency = money.getCurrency().getShortForm();
        return dto;
    }

    private NotesDTO toNotesDTO(Notes notes) {
        var dto = new NotesDTO();
        dto.description = notes.getDescription().getValue();
        dto.contains = notes.getContains().map(Note::getValue).orElse(null);
        dto.care = notes.getCare().map(Note::getValue).orElse(null);
        dto.safety = notes.getSafety().map(Note::getValue).orElse(null);
        return dto;
    }

}
