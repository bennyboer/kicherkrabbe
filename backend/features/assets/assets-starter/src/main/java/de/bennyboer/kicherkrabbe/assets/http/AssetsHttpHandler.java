package de.bennyboer.kicherkrabbe.assets.http;

import de.bennyboer.kicherkrabbe.assets.AssetTooLargeError;
import de.bennyboer.kicherkrabbe.assets.AssetsModule;
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

@AllArgsConstructor
public class AssetsHttpHandler {

    private final AssetsModule module;

    private final ReactiveTransactionManager transactionManager;

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
                        if (parsed > 4096) {
                            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Width must not exceed 4096");
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
                .as(transactionOperator::transactional);
    }

    private Mono<Agent> toAgent(ServerRequest request) {
        return request.principal()
                .map(principal -> Agent.user(AgentId.of(principal.getName())))
                .switchIfEmpty(Mono.just(Agent.anonymous()));
    }

}
