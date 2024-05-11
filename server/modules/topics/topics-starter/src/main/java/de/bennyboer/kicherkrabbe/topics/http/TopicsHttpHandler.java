package de.bennyboer.kicherkrabbe.topics.http;

import de.bennyboer.kicherkrabbe.changes.ResourceId;
import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.topics.TopicsModule;
import de.bennyboer.kicherkrabbe.topics.http.requests.CreateTopicRequest;
import de.bennyboer.kicherkrabbe.topics.http.requests.UpdateTopicRequest;
import de.bennyboer.kicherkrabbe.topics.http.responses.*;
import lombok.AllArgsConstructor;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;

@AllArgsConstructor
public class TopicsHttpHandler {

    private final TopicsModule module;

    private final ReactiveTransactionManager transactionManager;

    public Mono<ServerResponse> getTopics(ServerRequest request) {
        String searchTerm = request.queryParam("searchTerm").orElse("");
        long skip = request.queryParam("skip").map(Long::parseLong).orElse(0L);
        long limit = request.queryParam("limit").map(Long::parseLong).orElse((long) Integer.MAX_VALUE);

        return toAgent(request)
                .flatMap(agent -> module.getTopics(searchTerm, skip, limit, agent))
                .map(result -> {
                    var response = new QueryTopicsResponse();
                    response.skip = result.getSkip();
                    response.limit = result.getLimit();
                    response.total = result.getTotal();
                    response.topics = result.getResults()
                            .stream()
                            .map(t -> {
                                var topic = new TopicDTO();

                                topic.id = t.getId().getValue();
                                topic.version = t.getVersion().getValue();
                                topic.name = t.getName().getValue();
                                topic.createdAt = t.getCreatedAt();

                                return topic;
                            })
                            .toList();
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response));
    }

    public Mono<ServerResponse> getChanges(ServerRequest request) {
        var events$ = toAgent(request)
                .flatMapMany(module::getTopicChanges)
                .map(change -> {
                    var result = new TopicChangeDTO();

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
                .body(events$, TopicChangeDTO.class);
    }

    public Mono<ServerResponse> createTopic(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        return request.bodyToMono(CreateTopicRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.createTopic(
                        req.name,
                        agent
                )))
                .map(topicId -> {
                    var result = new CreateTopicResponse();
                    result.id = topicId;
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

    public Mono<ServerResponse> updateTopic(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        String topicId = request.pathVariable("topicId");

        return request.bodyToMono(UpdateTopicRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.updateTopic(
                        topicId,
                        req.version,
                        req.name,
                        agent
                )))
                .map(version -> {
                    var result = new UpdateTopicResponse();
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

    public Mono<ServerResponse> deleteTopic(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        String topicId = request.pathVariable("topicId");
        long version = request.queryParam("version").map(Long::parseLong).orElseThrow();

        return toAgent(request)
                .flatMap(agent -> module.deleteTopic(topicId, version, agent))
                .map(updatedVersion -> {
                    var result = new DeleteTopicResponse();
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

}
