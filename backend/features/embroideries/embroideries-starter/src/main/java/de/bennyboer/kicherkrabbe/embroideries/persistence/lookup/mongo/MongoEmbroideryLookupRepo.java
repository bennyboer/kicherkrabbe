package de.bennyboer.kicherkrabbe.embroideries.persistence.lookup.mongo;

import de.bennyboer.kicherkrabbe.embroideries.EmbroideryAlias;
import de.bennyboer.kicherkrabbe.embroideries.EmbroideryCategoryId;
import de.bennyboer.kicherkrabbe.embroideries.EmbroideryId;
import de.bennyboer.kicherkrabbe.embroideries.persistence.lookup.EmbroideryLookupRepo;
import de.bennyboer.kicherkrabbe.embroideries.persistence.lookup.LookupEmbroidery;
import de.bennyboer.kicherkrabbe.embroideries.persistence.lookup.LookupEmbroideryPage;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.mongo.MongoEventSourcingReadModelRepo;
import lombok.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationOptions;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexDefinition;
import org.springframework.data.mongodb.core.index.ReactiveIndexOperations;
import org.springframework.data.mongodb.core.query.Collation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

public class MongoEmbroideryLookupRepo
        extends MongoEventSourcingReadModelRepo<EmbroideryId, LookupEmbroidery, MongoLookupEmbroidery>
        implements EmbroideryLookupRepo {

    public MongoEmbroideryLookupRepo(ReactiveMongoTemplate template) {
        this("embroideries_lookup", template);
    }

    public MongoEmbroideryLookupRepo(String collectionName, ReactiveMongoTemplate template) {
        super(collectionName, template, new MongoLookupEmbroiderySerializer());
    }

    @Override
    public Mono<LookupEmbroideryPage> find(
            Collection<EmbroideryId> embroideryIds,
            Set<EmbroideryCategoryId> categories,
            String searchTerm,
            long skip,
            long limit
    ) {
        Set<String> ids = embroideryIds.stream()
                .map(EmbroideryId::getValue)
                .collect(Collectors.toSet());
        Set<String> categoryIds = toCategoryIds(categories);

        Criteria criteria = where("_id").in(ids);

        if (!categoryIds.isEmpty()) {
            criteria.and("categories").in(categoryIds);
        }

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
                .map(result -> toPage(result, skip, limit));
    }

    @Override
    public Mono<LookupEmbroidery> findById(EmbroideryId id) {
        Criteria criteria = where("_id").is(id.getValue());
        Query query = query(criteria);

        return template.findOne(query, MongoLookupEmbroidery.class, collectionName)
                .map(serializer::deserialize);
    }

    @Override
    public Mono<LookupEmbroidery> findByAlias(EmbroideryAlias alias) {
        Criteria criteria = where("alias").is(alias.getValue());
        Query query = query(criteria);

        return template.findOne(query, MongoLookupEmbroidery.class, collectionName)
                .map(serializer::deserialize);
    }

    @Override
    public Flux<LookupEmbroidery> findByCategory(EmbroideryCategoryId categoryId) {
        Criteria criteria = where("categories").is(categoryId.getValue());
        Query query = query(criteria);

        return template.find(query, MongoLookupEmbroidery.class, collectionName)
                .map(serializer::deserialize);
    }

    @Override
    public Flux<EmbroideryCategoryId> findUniqueCategories() {
        Criteria hasAtLeastOneCategory = where("categories").ne(Set.of());
        Query query = query(hasAtLeastOneCategory);

        return template.query(MongoLookupEmbroidery.class)
                .inCollection(collectionName)
                .distinct("categories")
                .matching(query)
                .as(String.class)
                .all()
                .map(EmbroideryCategoryId::of);
    }

    @Override
    public Mono<LookupEmbroidery> findPublished(EmbroideryId id) {
        Criteria criteria = where("_id").is(id.getValue())
                .and("published").is(true);
        Query query = query(criteria);

        return template.findOne(query, MongoLookupEmbroidery.class, collectionName)
                .map(serializer::deserialize);
    }

    @Override
    public Mono<LookupEmbroidery> findPublishedByAlias(EmbroideryAlias alias) {
        Criteria criteria = where("alias").is(alias.getValue())
                .and("published").is(true);
        Query query = query(criteria);

        return template.findOne(query, MongoLookupEmbroidery.class, collectionName)
                .map(serializer::deserialize);
    }

    @Override
    public Mono<LookupEmbroideryPage> findPublished(
            String searchTerm,
            Set<EmbroideryCategoryId> categories,
            boolean ascending,
            long skip,
            long limit
    ) {
        Set<String> categoryIds = toCategoryIds(categories);

        Criteria criteria = where("published").is(true);

        if (!searchTerm.isBlank()) {
            String quotedSearchTerm = Pattern.quote(searchTerm);
            criteria = criteria.and("name").regex(quotedSearchTerm, "i");
        }

        if (!categoryIds.isEmpty()) {
            criteria = criteria.and("categories").in(categoryIds);
        }

        AggregationOperation match = match(criteria);
        AggregationOperation sortBy = sort(ascending
                ? Sort.by(Sort.Order.asc("name"))
                : Sort.by(Sort.Order.desc("name")));
        AggregationOperation transformToPage = transformToPage(skip, limit);

        AggregationOptions options = AggregationOptions.builder()
                .collation(Collation.of("de").numericOrderingEnabled())
                .build();
        Aggregation aggregation = newAggregation(match, sortBy, transformToPage)
                .withOptions(options);

        return template.aggregate(aggregation, collectionName, PipelinePage.class)
                .next()
                .map(result -> toPage(result, skip, limit));
    }

    @Override
    public Flux<LookupEmbroidery> findFeatured() {
        Criteria criteria = where("published").is(true)
                .and("featured").is(true);
        Query query = query(criteria);

        return template.find(query, MongoLookupEmbroidery.class, collectionName)
                .map(serializer::deserialize);
    }

    @Override
    protected String stringifyId(EmbroideryId embroideryId) {
        return embroideryId.getValue();
    }

    @Override
    protected Mono<Void> initializeIndices(ReactiveIndexOperations indexOps) {
        IndexDefinition categoriesIndex = new Index().on("categories", ASC);
        IndexDefinition aliasIndex = new Index().on("alias", ASC).unique();
        IndexDefinition featuredIndex = new Index().on("published", ASC).on("featured", ASC);

        return indexOps.createIndex(categoriesIndex)
                .then(indexOps.createIndex(aliasIndex))
                .then(indexOps.createIndex(featuredIndex))
                .then();
    }

    private Set<String> toCategoryIds(Set<EmbroideryCategoryId> categories) {
        return categories.stream()
                .map(EmbroideryCategoryId::getValue)
                .collect(Collectors.toSet());
    }

    private LookupEmbroideryPage toPage(PipelinePage result, long skip, long limit) {
        return LookupEmbroideryPage.of(
                skip,
                limit,
                result.getTotal(),
                result.getMatches()
                        .stream()
                        .map(serializer::deserialize)
                        .toList()
        );
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

        List<MongoLookupEmbroidery> matches;

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
