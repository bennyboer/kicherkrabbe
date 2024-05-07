package de.bennyboer.kicherkrabbe.colors.http;

import de.bennyboer.kicherkrabbe.changes.ResourceId;
import de.bennyboer.kicherkrabbe.colors.ColorsModule;
import de.bennyboer.kicherkrabbe.colors.http.requests.CreateColorRequest;
import de.bennyboer.kicherkrabbe.colors.http.requests.UpdateColorRequest;
import de.bennyboer.kicherkrabbe.colors.http.responses.*;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import lombok.AllArgsConstructor;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@AllArgsConstructor
public class ColorsHttpHandler {

    private final ColorsModule module;

    private final ReactiveTransactionManager transactionManager;

    public Mono<ServerResponse> getColors(ServerRequest request) {
        String searchTerm = request.queryParam("searchTerm").orElse("");
        long skip = request.queryParam("skip").map(Long::parseLong).orElse(0L);
        long limit = request.queryParam("limit").map(Long::parseLong).orElse((long) Integer.MAX_VALUE);

        return toAgent(request)
                .flatMap(agent -> module.getColors(searchTerm, skip, limit, agent))
                .map(result -> {
                    var response = new QueryColorsResponse();
                    response.skip = result.getSkip();
                    response.limit = result.getLimit();
                    response.total = result.getTotal();
                    response.colors = result.getResults()
                            .stream()
                            .map(c -> {
                                var color = new ColorDTO();

                                color.id = c.getId().getValue();
                                color.name = c.getName().getValue();
                                color.red = c.getRed();
                                color.green = c.getGreen();
                                color.blue = c.getBlue();
                                color.createdAt = c.getCreatedAt();

                                return color;
                            })
                            .toList();
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response));
    }

    public Mono<ServerResponse> getColorChanges(ServerRequest request) {
        var events$ = toAgent(request)
                .flatMapMany(module::getColorChanges)
                .map(change -> {
                    var result = new ColorChangeDTO();

                    result.type = change.getType().getValue();
                    result.affected = change.getAffected()
                            .stream()
                            .map(ResourceId::getValue)
                            .toList();

                    return result;
                });

        return ServerResponse.ok()
                .header("Content-Type", "text/event-stream")
                .body(events$, ColorChangeDTO.class);
    }

    public Mono<ServerResponse> createColor(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        return request.bodyToMono(CreateColorRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.createColor(
                        req.name,
                        req.red,
                        req.green,
                        req.blue,
                        agent
                )))
                .map(colorId -> {
                    var result = new CreateColorResponse();
                    result.id = colorId;
                    return result;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> updateColor(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        String colorId = request.pathVariable("colorId");

        return request.bodyToMono(UpdateColorRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.updateColor(
                        colorId,
                        req.version,
                        req.name,
                        req.red,
                        req.green,
                        req.blue,
                        agent
                )))
                .map(version -> {
                    var result = new UpdateColorResponse();
                    result.version = version;
                    return result;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .as(transactionalOperator::transactional);
    }

    public Mono<ServerResponse> deleteColor(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        String colorId = request.pathVariable("colorId");
        long version = request.queryParam("version").map(Long::parseLong).orElseThrow();

        return toAgent(request).flatMap(agent -> module.deleteColor(colorId, version, agent))
                .map(updatedVersion -> {
                    var result = new DeleteColorResponse();
                    result.version = updatedVersion;
                    return result;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .as(transactionalOperator::transactional);
    }

    private Mono<Agent> toAgent(ServerRequest request) {
        return request.principal()
                .map(principal -> Agent.user(AgentId.of(principal.getName())))
                .switchIfEmpty(Mono.just(Agent.anonymous()));
    }

}
