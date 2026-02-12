package de.bennyboer.kicherkrabbe.highlights.http;

import de.bennyboer.kicherkrabbe.changes.ResourceId;
import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.highlights.HighlightDetails;
import de.bennyboer.kicherkrabbe.highlights.HighlightsModule;
import de.bennyboer.kicherkrabbe.highlights.HighlightsPage;
import de.bennyboer.kicherkrabbe.highlights.api.HighlightChangeDTO;
import de.bennyboer.kicherkrabbe.highlights.api.HighlightDTO;
import de.bennyboer.kicherkrabbe.highlights.api.PublishedHighlightDTO;
import de.bennyboer.kicherkrabbe.highlights.api.requests.*;
import de.bennyboer.kicherkrabbe.highlights.api.responses.*;
import de.bennyboer.kicherkrabbe.highlights.publish.AlreadyPublishedError;
import de.bennyboer.kicherkrabbe.highlights.transformer.LinkTransformer;
import de.bennyboer.kicherkrabbe.highlights.transformer.LinkTypeTransformer;
import de.bennyboer.kicherkrabbe.highlights.unpublish.AlreadyUnpublishedError;
import lombok.AllArgsConstructor;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpStatus.*;

@AllArgsConstructor
public class HighlightsHttpHandler {

    private final HighlightsModule module;

    private final ReactiveTransactionManager transactionManager;

    public Mono<ServerResponse> getPublishedHighlights(ServerRequest request) {
        return module.getPublishedHighlights()
                .map(this::toPublishedHighlightDTO)
                .collectList()
                .map(highlights -> {
                    var result = new QueryPublishedHighlightsResponse();
                    result.highlights = highlights;
                    return result;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response));
    }

    public Mono<ServerResponse> getHighlights(ServerRequest request) {
        long skip;
        long limit;
        try {
            skip = request.queryParam("skip").map(Long::parseLong).orElse(0L);
            limit = request.queryParam("limit").map(Long::parseLong).orElse((long) Integer.MAX_VALUE);
        } catch (NumberFormatException e) {
            return Mono.error(new ResponseStatusException(BAD_REQUEST, "Invalid skip or limit parameter", e));
        }

        return toAgent(request)
                .flatMap(agent -> module.getHighlights(skip, limit, agent))
                .map(this::toQueryHighlightsResponse)
                .flatMap(response -> ServerResponse.ok().bodyValue(response));
    }

    public Mono<ServerResponse> getLinks(ServerRequest request) {
        String searchTerm = request.queryParam("searchTerm").orElse("");
        long skip;
        long limit;
        try {
            skip = request.queryParam("skip").map(Long::parseLong).orElse(0L);
            limit = request.queryParam("limit").map(Long::parseLong).orElse((long) Integer.MAX_VALUE);
        } catch (NumberFormatException e) {
            return Mono.error(new ResponseStatusException(BAD_REQUEST, "Invalid skip or limit parameter", e));
        }

        return toAgent(request)
                .flatMap(agent -> module.getLinks(searchTerm, skip, limit, agent))
                .flatMap(response -> ServerResponse.ok().bodyValue(response));
    }

    public Mono<ServerResponse> getHighlight(ServerRequest request) {
        String highlightId = request.pathVariable("highlightId");

        return toAgent(request)
                .flatMap(agent -> module.getHighlight(highlightId, agent))
                .map(this::toHighlightDTO)
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .switchIfEmpty(Mono.error(new ResponseStatusException(NOT_FOUND, "Highlight not found.")));
    }

    public Mono<ServerResponse> getChanges(ServerRequest request) {
        var events$ = toAgent(request)
                .flatMapMany(module::getHighlightChanges)
                .map(change -> {
                    var result = new HighlightChangeDTO();

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
                .body(events$, HighlightChangeDTO.class);
    }

    public Mono<ServerResponse> createHighlight(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        return request.bodyToMono(CreateHighlightRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.createHighlight(
                        req.imageId,
                        req.sortOrder,
                        agent
                )))
                .map(highlightId -> {
                    var result = new CreateHighlightResponse();
                    result.id = highlightId;
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

    public Mono<ServerResponse> updateImage(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        String highlightId = request.pathVariable("highlightId");

        return request.bodyToMono(UpdateHighlightImageRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.updateImage(
                        highlightId,
                        req.version,
                        req.imageId,
                        agent
                )))
                .map(version -> {
                    var result = new UpdateHighlightImageResponse();
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

    public Mono<ServerResponse> addLink(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        String highlightId = request.pathVariable("highlightId");

        return request.bodyToMono(AddLinkToHighlightRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.addLink(
                        highlightId,
                        req.version,
                        LinkTypeTransformer.toDomain(req.linkType),
                        req.linkId,
                        req.linkName,
                        agent
                )))
                .map(version -> {
                    var result = new AddLinkToHighlightResponse();
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

    public Mono<ServerResponse> removeLink(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        String highlightId = request.pathVariable("highlightId");

        return request.bodyToMono(RemoveLinkFromHighlightRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.removeLink(
                        highlightId,
                        req.version,
                        LinkTypeTransformer.toDomain(req.linkType),
                        req.linkId,
                        agent
                )))
                .map(version -> {
                    var result = new RemoveLinkFromHighlightResponse();
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

    public Mono<ServerResponse> publishHighlight(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        String highlightId = request.pathVariable("highlightId");

        return request.bodyToMono(PublishHighlightRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.publishHighlight(
                        highlightId,
                        req.version,
                        agent
                )))
                .map(version -> {
                    var result = new PublishHighlightResponse();
                    result.version = version;
                    return result;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorMap(AggregateVersionOutdatedError.class, e -> new ResponseStatusException(
                        CONFLICT,
                        e.getMessage(),
                        e
                ))
                .onErrorMap(AlreadyPublishedError.class, e -> new ResponseStatusException(
                        CONFLICT,
                        "Highlight is already published",
                        e
                ))
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> unpublishHighlight(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        String highlightId = request.pathVariable("highlightId");

        return request.bodyToMono(UnpublishHighlightRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.unpublishHighlight(
                        highlightId,
                        req.version,
                        agent
                )))
                .map(version -> {
                    var result = new UnpublishHighlightResponse();
                    result.version = version;
                    return result;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorMap(AggregateVersionOutdatedError.class, e -> new ResponseStatusException(
                        CONFLICT,
                        e.getMessage(),
                        e
                ))
                .onErrorMap(AlreadyUnpublishedError.class, e -> new ResponseStatusException(
                        CONFLICT,
                        "Highlight is already unpublished",
                        e
                ))
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> updateSortOrder(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        String highlightId = request.pathVariable("highlightId");

        return request.bodyToMono(UpdateHighlightSortOrderRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.updateSortOrder(
                        highlightId,
                        req.version,
                        req.sortOrder,
                        agent
                )))
                .map(version -> {
                    var result = new UpdateHighlightSortOrderResponse();
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

    public Mono<ServerResponse> deleteHighlight(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        String highlightId = request.pathVariable("highlightId");
        long version = request.queryParam("version")
                .map(Long::parseLong)
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Version parameter is required"));

        return toAgent(request)
                .flatMap(agent -> module.deleteHighlight(highlightId, version, agent))
                .map(updatedVersion -> {
                    var result = new DeleteHighlightResponse();
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

    private QueryHighlightsResponse toQueryHighlightsResponse(HighlightsPage result) {
        var response = new QueryHighlightsResponse();

        response.skip = result.getSkip();
        response.limit = result.getLimit();
        response.total = result.getTotal();
        response.highlights = result.getResults()
                .stream()
                .map(this::toHighlightDTO)
                .toList();

        return response;
    }

    private HighlightDTO toHighlightDTO(HighlightDetails details) {
        var result = new HighlightDTO();

        result.id = details.getId().getValue();
        result.version = details.getVersion().getValue();
        result.imageId = details.getImageId().getValue();
        result.links = details.getLinks().getLinks().stream()
                .map(LinkTransformer::toApi)
                .toList();
        result.published = details.isPublished();
        result.sortOrder = details.getSortOrder();
        result.createdAt = details.getCreatedAt();

        return result;
    }

    private PublishedHighlightDTO toPublishedHighlightDTO(HighlightDetails details) {
        var result = new PublishedHighlightDTO();

        result.id = details.getId().getValue();
        result.imageId = details.getImageId().getValue();
        result.links = details.getLinks().getLinks().stream()
                .map(LinkTransformer::toApi)
                .toList();

        return result;
    }

}
