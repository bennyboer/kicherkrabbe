package de.bennyboer.kicherkrabbe.categories.http;

import de.bennyboer.kicherkrabbe.categories.CategoriesModule;
import de.bennyboer.kicherkrabbe.categories.CategoriesPage;
import de.bennyboer.kicherkrabbe.categories.CategoryDetails;
import de.bennyboer.kicherkrabbe.categories.CategoryGroup;
import de.bennyboer.kicherkrabbe.categories.http.api.CategoryChangeDTO;
import de.bennyboer.kicherkrabbe.categories.http.api.CategoryDTO;
import de.bennyboer.kicherkrabbe.categories.http.api.CategoryGroupDTO;
import de.bennyboer.kicherkrabbe.categories.http.api.requests.CreateCategoryRequest;
import de.bennyboer.kicherkrabbe.categories.http.api.requests.RegroupCategoryRequest;
import de.bennyboer.kicherkrabbe.categories.http.api.requests.RenameCategoryRequest;
import de.bennyboer.kicherkrabbe.categories.http.api.responses.*;
import de.bennyboer.kicherkrabbe.changes.ResourceId;
import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;

@AllArgsConstructor
public class CategoriesHttpHandler {

    private final CategoriesModule module;

    private final ReactiveTransactionManager transactionManager;

    public Mono<ServerResponse> getCategory(ServerRequest request) {
        String categoryId = request.pathVariable("categoryId");

        return toAgent(request)
                .flatMap(agent -> module.getCategory(categoryId, agent))
                .map(this::toCategoryDTO)
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found.")));
    }

    public Mono<ServerResponse> getCategories(ServerRequest request) {
        String searchTerm = request.queryParam("searchTerm").orElse("");
        long skip = request.queryParam("skip").map(Long::parseLong).orElse(0L);
        long limit = request.queryParam("limit").map(Long::parseLong).orElse((long) Integer.MAX_VALUE);

        return toAgent(request)
                .flatMap(agent -> module.getCategories(searchTerm, skip, limit, agent))
                .map(this::toQueryCategoriesResponse)
                .flatMap(response -> ServerResponse.ok().bodyValue(response));
    }

    public Mono<ServerResponse> getCategoriesByGroup(ServerRequest request) {
        String searchTerm = request.queryParam("searchTerm").orElse("");
        long skip = request.queryParam("skip").map(Long::parseLong).orElse(0L);
        long limit = request.queryParam("limit").map(Long::parseLong).orElse((long) Integer.MAX_VALUE);
        CategoryGroup group = switch (request.pathVariable("group")) {
            case "CLOTHING" -> CategoryGroup.CLOTHING;
            case "NONE" -> CategoryGroup.NONE;
            default -> throw new IllegalStateException("Unexpected group: " + request.pathVariable("group"));
        };

        return toAgent(request)
                .flatMap(agent -> module.getCategoriesByGroup(group, searchTerm, skip, limit, agent))
                .map(this::toQueryCategoriesResponse)
                .flatMap(response -> ServerResponse.ok().bodyValue(response));
    }

    public Mono<ServerResponse> getChanges(ServerRequest request) {
        var events$ = toAgent(request)
                .flatMapMany(module::getCategoryChanges)
                .map(change -> {
                    var result = new CategoryChangeDTO();

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
                .body(events$, CategoryChangeDTO.class);
    }

    public Mono<ServerResponse> createCategory(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        return request.bodyToMono(CreateCategoryRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.createCategory(
                        req.name,
                        switch (req.group) {
                            case CLOTHING -> CategoryGroup.CLOTHING;
                            case NONE -> CategoryGroup.NONE;
                        },
                        agent
                )))
                .map(categoryId -> {
                    var result = new CreateCategoryResponse();
                    result.id = categoryId;
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

    public Mono<ServerResponse> renameCategory(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        String categoryId = request.pathVariable("categoryId");

        return request.bodyToMono(RenameCategoryRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.renameCategory(
                        categoryId,
                        req.version,
                        req.name,
                        agent
                )))
                .map(version -> {
                    var result = new RenameCategoryResponse();
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

    public Mono<ServerResponse> regroupCategory(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        String categoryId = request.pathVariable("categoryId");

        return request.bodyToMono(RegroupCategoryRequest.class)
                .flatMap(req -> toAgent(request).flatMap(agent -> module.regroupCategory(
                        categoryId,
                        req.version,
                        switch (req.group) {
                            case CLOTHING -> CategoryGroup.CLOTHING;
                            case NONE -> CategoryGroup.NONE;
                        },
                        agent
                )))
                .map(version -> {
                    var result = new RegroupCategoryResponse();
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

    public Mono<ServerResponse> deleteCategory(ServerRequest request) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        String categoryId = request.pathVariable("categoryId");
        long version = request.queryParam("version").map(Long::parseLong).orElseThrow();

        return toAgent(request)
                .flatMap(agent -> module.deleteCategory(categoryId, version, agent))
                .map(updatedVersion -> {
                    var result = new DeleteCategoryResponse();
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

    private QueryCategoriesResponse toQueryCategoriesResponse(CategoriesPage result) {
        var response = new QueryCategoriesResponse();

        response.skip = result.getSkip();
        response.limit = result.getLimit();
        response.total = result.getTotal();
        response.categories = result.getResults()
                .stream()
                .map(this::toCategoryDTO)
                .toList();

        return response;
    }

    private CategoryDTO toCategoryDTO(CategoryDetails details) {
        var result = new CategoryDTO();

        result.id = details.getId().getValue();
        result.version = details.getVersion().getValue();
        result.name = details.getName().getValue();
        result.group = switch (details.getGroup()) {
            case CLOTHING -> CategoryGroupDTO.CLOTHING;
            case NONE -> CategoryGroupDTO.NONE;
        };
        result.createdAt = details.getCreatedAt();

        return result;
    }

}
