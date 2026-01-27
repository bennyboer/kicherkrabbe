package de.bennyboer.kicherkrabbe.products.http;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import de.bennyboer.kicherkrabbe.products.ProductsModule;
import de.bennyboer.kicherkrabbe.products.api.LinkTypeDTO;
import de.bennyboer.kicherkrabbe.products.api.requests.*;
import lombok.AllArgsConstructor;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Locale;

import static org.springframework.http.HttpStatus.FORBIDDEN;

@AllArgsConstructor
public class ProductsHttpHandler {

    private final ProductsModule module;

    private final ReactiveTransactionManager transactionManager;

    public Mono<ServerResponse> getProducts(ServerRequest request) {
        String searchTerm = request.queryParam("searchTerm").orElse("");
        Instant from = request.queryParam("from")
                .map(Instant::parse)
                .orElse(null);
        Instant to = request.queryParam("to")
                .map(Instant::parse)
                .orElse(null);
        long skip = request.queryParam("skip")
                .map(Long::parseLong)
                .filter(s -> s >= 0)
                .orElse(0L);
        long limit = request.queryParam("limit")
                .map(Long::parseLong)
                .filter(l -> l >= 0)
                .orElse(100L);

        return toAgent(request)
                .flatMap(agent -> module.getProducts(
                        searchTerm,
                        from,
                        to,
                        skip,
                        limit,
                        agent
                ))
                .flatMap(agent -> ServerResponse.ok().bodyValue(agent))
                .onErrorResume(MissingPermissionError.class, e -> ServerResponse.status(FORBIDDEN).build());
    }

    public Mono<ServerResponse> getProduct(ServerRequest request) {
        String productId = request.pathVariable("productId");

        return toAgent(request)
                .flatMap(agent -> module.getProduct(productId, agent))
                .flatMap(agent -> ServerResponse.ok().bodyValue(agent))
                .switchIfEmpty(ServerResponse.notFound().build())
                .onErrorResume(MissingPermissionError.class, e -> ServerResponse.status(FORBIDDEN).build());
    }

    public Mono<ServerResponse> getLinks(ServerRequest request) {
        String searchTerm = request.queryParam("searchTerm").orElse("");
        long skip = request.queryParam("skip")
                .map(Long::parseLong)
                .filter(s -> s >= 0)
                .orElse(0L);
        long limit = request.queryParam("limit")
                .map(Long::parseLong)
                .filter(l -> l >= 0)
                .orElse(100L);

        return toAgent(request)
                .flatMap(agent -> module.getLinks(searchTerm, skip, limit, agent))
                .flatMap(agent -> ServerResponse.ok().bodyValue(agent))
                .onErrorResume(MissingPermissionError.class, e -> ServerResponse.status(FORBIDDEN).build());
    }

    public Mono<ServerResponse> createProduct(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        return request.bodyToMono(CreateProductRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.createProduct(req, agent)))
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorResume(MissingPermissionError.class, e -> ServerResponse.status(FORBIDDEN).build())
                .onErrorResume(IllegalArgumentException.class, e -> ServerResponse.badRequest().build())
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> addLinkToProduct(ServerRequest request) {
        String productId = request.pathVariable("productId");

        var transactionalOperator = TransactionalOperator.create(transactionManager);

        return request.bodyToMono(AddLinkToProductRequest.class)
                .flatMap(req -> {
                    if (req.version < 0) {
                        return Mono.error(new IllegalArgumentException("Version is required"));
                    }

                    return Mono.just(req);
                })
                .flatMap(req -> toAgent(request).flatMap(agent -> module.addLinkToProduct(productId, req, agent)))
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .switchIfEmpty(ServerResponse.notFound().build())
                .onErrorResume(MissingPermissionError.class, e -> ServerResponse.status(FORBIDDEN).build())
                .onErrorResume(AggregateVersionOutdatedError.class, e -> ServerResponse.status(409).build())
                .onErrorResume(IllegalArgumentException.class, e -> ServerResponse.badRequest().build())
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> removeLinkFromProduct(ServerRequest request) {
        String productId = request.pathVariable("productId");
        LinkTypeDTO linkType = switch (request.pathVariable("linkType").toLowerCase(Locale.ROOT)) {
            case "pattern" -> LinkTypeDTO.PATTERN;
            case "fabric" -> LinkTypeDTO.FABRIC;
            default -> throw new IllegalArgumentException("Invalid link type");
        };
        String linkId = request.pathVariable("linkId");

        if (request.queryParam("version").isEmpty()) {
            return ServerResponse.badRequest().build();
        }
        long version = request.queryParam("version")
                .map(Long::parseLong)
                .orElseThrow();

        var transactionalOperator = TransactionalOperator.create(transactionManager);

        return toAgent(request).flatMap(agent -> module.removeLinkFromProduct(productId, version, linkType, linkId, agent))
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .switchIfEmpty(ServerResponse.notFound().build())
                .onErrorResume(MissingPermissionError.class, e -> ServerResponse.status(FORBIDDEN).build())
                .onErrorResume(AggregateVersionOutdatedError.class, e -> ServerResponse.status(409).build())
                .onErrorResume(IllegalArgumentException.class, e -> ServerResponse.badRequest().build())
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> updateNotes(ServerRequest request) {
        String productId = request.pathVariable("productId");

        var transactionalOperator = TransactionalOperator.create(transactionManager);

        return request.bodyToMono(UpdateNotesRequest.class)
                .flatMap(req -> {
                    if (req.version < 0) {
                        return Mono.error(new IllegalArgumentException("Version is required"));
                    }

                    return Mono.just(req);
                })
                .flatMap(req -> toAgent(request).flatMap(agent -> module.updateNotes(productId, req, agent)))
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .switchIfEmpty(ServerResponse.notFound().build())
                .onErrorResume(MissingPermissionError.class, e -> ServerResponse.status(FORBIDDEN).build())
                .onErrorResume(AggregateVersionOutdatedError.class, e -> ServerResponse.status(409).build())
                .onErrorResume(IllegalArgumentException.class, e -> ServerResponse.badRequest().build())
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> updateFabricComposition(ServerRequest request) {
        String productId = request.pathVariable("productId");

        var transactionalOperator = TransactionalOperator.create(transactionManager);

        return request.bodyToMono(UpdateFabricCompositionRequest.class)
                .flatMap(req -> {
                    if (req.version < 0) {
                        return Mono.error(new IllegalArgumentException("Version is required"));
                    }

                    return Mono.just(req);
                })
                .flatMap(req -> toAgent(request).flatMap(agent -> module.updateFabricComposition(productId, req, agent)))
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .switchIfEmpty(ServerResponse.notFound().build())
                .onErrorResume(MissingPermissionError.class, e -> ServerResponse.status(FORBIDDEN).build())
                .onErrorResume(AggregateVersionOutdatedError.class, e -> ServerResponse.status(409).build())
                .onErrorResume(IllegalArgumentException.class, e -> ServerResponse.badRequest().build())
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> updateImages(ServerRequest request) {
        String productId = request.pathVariable("productId");

        var transactionalOperator = TransactionalOperator.create(transactionManager);

        return request.bodyToMono(UpdateImagesRequest.class)
                .flatMap(req -> {
                    if (req.version < 0) {
                        return Mono.error(new IllegalArgumentException("Version is required"));
                    }

                    return Mono.just(req);
                })
                .flatMap(req -> toAgent(request).flatMap(agent -> module.updateImages(productId, req, agent)))
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .switchIfEmpty(ServerResponse.notFound().build())
                .onErrorResume(MissingPermissionError.class, e -> ServerResponse.status(FORBIDDEN).build())
                .onErrorResume(AggregateVersionOutdatedError.class, e -> ServerResponse.status(409).build())
                .onErrorResume(IllegalArgumentException.class, e -> ServerResponse.badRequest().build())
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> updateProducedAt(ServerRequest request) {
        String productId = request.pathVariable("productId");

        var transactionalOperator = TransactionalOperator.create(transactionManager);

        return request.bodyToMono(UpdateProducedAtDateRequest.class)
                .flatMap(req -> {
                    if (req.version < 0) {
                        return Mono.error(new IllegalArgumentException("Version is required"));
                    }

                    return Mono.just(req);
                })
                .flatMap(req -> toAgent(request).flatMap(agent -> module.updateProducedAt(productId, req, agent)))
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .switchIfEmpty(ServerResponse.notFound().build())
                .onErrorResume(MissingPermissionError.class, e -> ServerResponse.status(FORBIDDEN).build())
                .onErrorResume(AggregateVersionOutdatedError.class, e -> ServerResponse.status(409).build())
                .onErrorResume(IllegalArgumentException.class, e -> ServerResponse.badRequest().build())
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> deleteProduct(ServerRequest request) {
        String productId = request.pathVariable("productId");

        if (request.queryParam("version").isEmpty()) {
            return ServerResponse.badRequest().build();
        }
        long version = request.queryParam("version")
                .map(Long::parseLong)
                .orElse(0L);

        var transactionalOperator = TransactionalOperator.create(transactionManager);

        return toAgent(request).flatMap(agent -> module.deleteProduct(productId, version, agent))
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .switchIfEmpty(ServerResponse.notFound().build())
                .onErrorResume(MissingPermissionError.class, e -> ServerResponse.status(FORBIDDEN).build())
                .onErrorResume(AggregateVersionOutdatedError.class, e -> ServerResponse.status(409).build())
                .as(transactionalOperator::transactional);
    }

    private Mono<Agent> toAgent(ServerRequest request) {
        return request.principal()
                .map(principal -> Agent.user(AgentId.of(principal.getName())))
                .switchIfEmpty(Mono.just(Agent.anonymous()));
    }

}
