package de.bennyboer.kicherkrabbe.assets.http;

import de.bennyboer.kicherkrabbe.assets.AssetStillReferencedError;
import de.bennyboer.kicherkrabbe.assets.AssetTooLargeError;
import de.bennyboer.kicherkrabbe.assets.AssetsModule;
import de.bennyboer.kicherkrabbe.assets.StorageLimitExceededError;
import de.bennyboer.kicherkrabbe.assets.http.api.AssetDTO;
import de.bennyboer.kicherkrabbe.assets.http.api.AssetReferenceDTO;
import de.bennyboer.kicherkrabbe.assets.http.api.requests.QueryAssetsRequest;
import de.bennyboer.kicherkrabbe.assets.http.api.responses.QueryAssetsResponse;
import de.bennyboer.kicherkrabbe.assets.http.api.responses.QueryContentTypesResponse;
import de.bennyboer.kicherkrabbe.assets.http.api.responses.QueryStorageInfoResponse;
import de.bennyboer.kicherkrabbe.assets.http.responses.UploadAssetResponse;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.Part;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.util.MimeType;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.Set;

@AllArgsConstructor
public class AssetsHttpHandler {

    private final AssetsModule module;

    private final ReactiveTransactionManager transactionManager;

    public Mono<ServerResponse> getAssets(ServerRequest request) {
        return request.bodyToMono(QueryAssetsRequest.class)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body must be given")))
                .flatMap(req -> {
                    if (req.skip < 0) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Skip must not be negative"));
                    }
                    if (req.limit <= 0) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Limit must be positive"));
                    }
                    if (req.limit > 100) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Limit must not exceed 100"));
                    }

                    return toAgent(request)
                            .flatMap(agent -> module.getAssets(
                                    req.searchTerm,
                                    Optional.ofNullable(req.contentTypes).orElse(Set.of()),
                                    req.sortProperty,
                                    req.sortDirection,
                                    req.skip,
                                    req.limit,
                                    agent
                            ));
                })
                .map(page -> {
                    var response = new QueryAssetsResponse();
                    response.skip = page.getSkip();
                    response.limit = page.getLimit();
                    response.total = page.getTotal();
                    response.assets = page.getResults().stream()
                            .map(asset -> {
                                var dto = new AssetDTO();
                                dto.id = asset.getId().getValue();
                                dto.version = asset.getVersion().getValue();
                                dto.contentType = asset.getContentType().getValue();
                                dto.fileSize = asset.getFileSize();
                                dto.createdAt = asset.getCreatedAt().toString();
                                dto.references = asset.getReferences().stream()
                                        .map(ref -> {
                                            var refDto = new AssetReferenceDTO();
                                            refDto.resourceType = ref.getResourceType().name();
                                            refDto.resourceId = ref.getResourceId().getValue();
                                            refDto.resourceName = ref.getResourceName();
                                            return refDto;
                                        })
                                        .toList();
                                return dto;
                            })
                            .toList();
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response));
    }

    public Mono<ServerResponse> getContentTypes(ServerRequest request) {
        return toAgent(request)
                .flatMap(module::getContentTypes)
                .map(types -> {
                    var response = new QueryContentTypesResponse();
                    response.contentTypes = types;
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response));
    }

    public Mono<ServerResponse> getStorageInfo(ServerRequest request) {
        return toAgent(request)
                .flatMap(module::getStorageInfo)
                .map(info -> {
                    var response = new QueryStorageInfoResponse();
                    response.usedBytes = info.getUsedBytes();
                    response.limitBytes = info.getLimitBytes();
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response));
    }

    public Mono<ServerResponse> uploadAsset(ServerRequest request) {
        var transactionOperator = TransactionalOperator.create(transactionManager);

        Mono<Agent> agent$ = toAgent(request);
        Mono<Part> part$ = request.body(BodyExtractors.toParts())
                .singleOrEmpty();

        return Mono.zip(agent$, part$)
                .flatMap(tuple -> {
                    Agent agent = tuple.getT1();
                    Part part = tuple.getT2();

                    Flux<DataBuffer> content$ = part.content();
                    String contentType = Optional.ofNullable(part.headers().getContentType())
                            .map(MimeType::toString)
                            .orElse(null);
                    if (contentType == null) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing content type"));
                    }

                    return module.uploadAsset(contentType, content$, agent);
                })
                .map(assetId -> {
                    var response = new UploadAssetResponse();
                    response.assetId = assetId;
                    return response;
                })
                .flatMap(assetId -> ServerResponse.ok().bodyValue(assetId))
                .onErrorResume(
                        AssetTooLargeError.class,
                        _ -> ServerResponse.status(HttpStatus.CONTENT_TOO_LARGE).build()
                )
                .onErrorResume(
                        StorageLimitExceededError.class,
                        _ -> ServerResponse.status(HttpStatus.INSUFFICIENT_STORAGE).build()
                )
                .as(transactionOperator::transactional);
    }

    public Mono<ServerResponse> getAssetContent(ServerRequest request) {
        String assetId = request.pathVariable("assetId");
        @Nullable Integer width = request.queryParam("width")
                .map(w -> {
                    try {
                        int parsed = Integer.parseInt(w);
                        if (parsed <= 0) {
                            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Width must be positive");
                        }
                        if (parsed > 99999) {
                            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Width must not exceed 99999");
                        }
                        return parsed;
                    } catch (NumberFormatException e) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid width parameter");
                    }
                })
                .orElse(null);

        return toAgent(request)
                .flatMap(agent -> module.getAssetContent(assetId, width, agent))
                .flatMap(content -> ServerResponse.ok()
                        .contentType(MediaType.parseMediaType(content.getContentType().getValue()))
                        .body(content.getBuffers(), DataBuffer.class))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED)));
    }

    public Mono<ServerResponse> deleteAsset(ServerRequest request) {
        var transactionOperator = TransactionalOperator.create(transactionManager);

        String assetId = request.pathVariable("assetId");
        long version = request.queryParam("version")
                .map(Long::parseLong)
                .orElse(0L);

        return toAgent(request)
                .flatMap(agent -> module.deleteAsset(assetId, version, agent))
                .then(Mono.defer(() -> ServerResponse.ok().build()))
                .onErrorResume(
                        AssetStillReferencedError.class,
                        _ -> ServerResponse.status(HttpStatus.CONFLICT).build()
                )
                .as(transactionOperator::transactional);
    }

    private Mono<Agent> toAgent(ServerRequest request) {
        return request.principal()
                .map(principal -> Agent.user(AgentId.of(principal.getName())))
                .switchIfEmpty(Mono.just(Agent.anonymous()));
    }

}
