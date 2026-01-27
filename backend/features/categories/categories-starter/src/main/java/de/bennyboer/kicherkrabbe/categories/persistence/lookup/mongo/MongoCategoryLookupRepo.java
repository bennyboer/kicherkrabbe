package de.bennyboer.kicherkrabbe.categories.persistence.lookup.mongo;

import de.bennyboer.kicherkrabbe.categories.CategoryGroup;
import de.bennyboer.kicherkrabbe.categories.CategoryId;
import de.bennyboer.kicherkrabbe.categories.persistence.lookup.CategoryLookupRepo;
import de.bennyboer.kicherkrabbe.categories.persistence.lookup.LookupCategory;
import de.bennyboer.kicherkrabbe.categories.persistence.lookup.LookupCategoryPage;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.mongo.MongoEventSourcingReadModelRepo;
import lombok.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;
import static org.springframework.data.mongodb.core.query.Criteria.where;

public class MongoCategoryLookupRepo
        extends MongoEventSourcingReadModelRepo<CategoryId, LookupCategory, MongoLookupCategory>
        implements CategoryLookupRepo {

    public MongoCategoryLookupRepo(ReactiveMongoTemplate template) {
        this("categories_lookup", template);
    }

    public MongoCategoryLookupRepo(String collectionName, ReactiveMongoTemplate template) {
        super(collectionName, template, new MongoLookupCategorySerializer());
    }

    @Override
    public Mono<LookupCategoryPage> find(Collection<CategoryId> categoryIds, String searchTerm, long skip, long limit) {
        Set<String> ids = categoryIds.stream()
                .map(CategoryId::getValue)
                .collect(Collectors.toSet());

        Criteria criteria = where("_id").in(ids);

        if (!searchTerm.isBlank()) {
            String quotedSearchTerm = Pattern.quote(searchTerm);
            criteria.and("name").regex(quotedSearchTerm, "i");
        }

        var match = match(criteria);
        var sortByCreationDate = sort(Sort.by(Sort.Order.asc("createdAt")));
        var transformToPage = transformToPage(skip, limit);

        Aggregation aggregation = newAggregation(match, sortByCreationDate, transformToPage);

        return template.aggregate(aggregation, collectionName, PipelinePage.class)
                .next()
                .map(result -> LookupCategoryPage.of(
                        skip,
                        limit,
                        result.getTotal(),
                        result.getMatches().stream().map(serializer::deserialize).toList()
                ));
    }

    @Override
    public Mono<LookupCategoryPage> findByGroup(
            Collection<CategoryId> categoryIds,
            CategoryGroup group,
            String searchTerm,
            long skip,
            long limit
    ) {
        Set<String> ids = categoryIds.stream()
                .map(CategoryId::getValue)
                .collect(Collectors.toSet());

        Criteria criteria = where("_id").in(ids)
                .and("group").is(switch (group) {
                    case CLOTHING -> "CLOTHING";
                    case NONE -> "NONE";
                });

        if (!searchTerm.isBlank()) {
            String quotedSearchTerm = Pattern.quote(searchTerm);
            criteria.and("name").regex(quotedSearchTerm, "i");
        }

        var match = match(criteria);
        var sortByCreationDate = sort(Sort.by(Sort.Order.asc("createdAt")));
        var transformToPage = transformToPage(skip, limit);

        Aggregation aggregation = newAggregation(match, sortByCreationDate, transformToPage);

        return template.aggregate(aggregation, collectionName, PipelinePage.class)
                .next()
                .map(result -> LookupCategoryPage.of(
                        skip,
                        limit,
                        result.getTotal(),
                        result.getMatches().stream().map(serializer::deserialize).toList()
                ));
    }

    @Override
    public Mono<LookupCategory> findById(CategoryId categoryId) {
        return template.findById(categoryId.getValue(), MongoLookupCategory.class, collectionName)
                .map(serializer::deserialize);
    }

    @Override
    protected String stringifyId(CategoryId categoryId) {
        return categoryId.getValue();
    }

    private AggregationOperation transformToPage(long skip, long limit) {
        var countTotal = count().as("total");

        var skipResults = skip(skip);
        var limitResults = limit(limit);

        return facet(countTotal).as("metadata")
                .and(skipResults, limitResults).as("matches");
    }

    @Value
    private static class PipelinePage {

        List<MetaData> metadata;

        List<MongoLookupCategory> matches;

        public long getTotal() {
            return metadata.stream()
                    .findFirst()
                    .map(MetaData::getTotal)
                    .orElse(0L);
        }

        @Value
        private static class MetaData {

            long total;

        }

    }

}
