package de.bennyboer.kicherkrabbe.patterns.persistence.lookup.mongo;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.mongo.MongoEventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.patterns.PatternCategoryId;
import de.bennyboer.kicherkrabbe.patterns.PatternId;
import de.bennyboer.kicherkrabbe.patterns.persistence.lookup.LookupPattern;
import de.bennyboer.kicherkrabbe.patterns.persistence.lookup.LookupPatternPage;
import de.bennyboer.kicherkrabbe.patterns.persistence.lookup.PatternLookupRepo;
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

public class MongoPatternLookupRepo
        extends MongoEventSourcingReadModelRepo<PatternId, LookupPattern, MongoLookupPattern>
        implements PatternLookupRepo {

    public MongoPatternLookupRepo(ReactiveMongoTemplate template) {
        this("patterns_lookup", template);
    }

    public MongoPatternLookupRepo(String collectionName, ReactiveMongoTemplate template) {
        super(collectionName, template, new MongoLookupPatternSerializer());
    }

    @Override
    public Mono<LookupPatternPage> find(
            Collection<PatternId> patternIds,
            Set<PatternCategoryId> categories,
            String searchTerm,
            long skip,
            long limit
    ) {
        Set<String> ids = patternIds.stream()
                .map(PatternId::getValue)
                .collect(Collectors.toSet());
        Set<String> categoryIds = categories.stream()
                .map(PatternCategoryId::getValue)
                .collect(Collectors.toSet());

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
                .map(result -> LookupPatternPage.of(
                        skip,
                        limit,
                        result.getTotal(),
                        result.getMatches().stream().map(serializer::deserialize).toList()
                ));
    }

    @Override
    public Flux<LookupPattern> findByCategory(PatternCategoryId categoryId) {
        Criteria criteria = where("categories").is(categoryId.getValue());
        Query query = new Query(criteria);

        return template.find(query, MongoLookupPattern.class, collectionName)
                .map(serializer::deserialize);
    }

    @Override
    public Mono<LookupPattern> findById(PatternId internalPatternId) {
        Criteria criteria = where("_id").is(internalPatternId.getValue());
        Query query = new Query(criteria);

        return template.findOne(query, MongoLookupPattern.class, collectionName)
                .map(serializer::deserialize);
    }

    @Override
    public Flux<PatternCategoryId> findUniqueCategories() {
        return template.query(MongoLookupPattern.class)
                .inCollection(collectionName)
                .distinct("categories")
                .as(String.class)
                .all()
                .map(PatternCategoryId::of);
    }

    @Override
    public Mono<LookupPattern> findPublished(PatternId id) {
        Criteria criteria = where("_id").is(id.getValue())
                .and("published").is(true);
        Query query = new Query(criteria);

        return template.findOne(query, MongoLookupPattern.class, collectionName)
                .map(serializer::deserialize);
    }

    @Override
    public Mono<LookupPatternPage> findPublished(
            String searchTerm,
            Set<PatternCategoryId> categories,
            Set<Long> sizes,
            boolean ascending,
            long skip,
            long limit
    ) {
        Set<String> categoryIds = categories.stream()
                .map(PatternCategoryId::getValue)
                .collect(Collectors.toSet());

        Criteria criteria = where("published").is(true);

        if (!searchTerm.isBlank()) {
            String quotedSearchTerm = Pattern.quote(searchTerm);
            criteria = criteria.and("name").regex(quotedSearchTerm, "i");
        }

        if (!categoryIds.isEmpty()) {
            criteria = criteria.and("categories").in(categoryIds);
        }

        if (!sizes.isEmpty()) {
            Criteria sizeCriteria = new Criteria();
            sizeCriteria = sizeCriteria.orOperator(
                    where("variants.pricedSizeRanges.from").in(sizes),
                    where("variants.pricedSizeRanges.to").in(sizes)
            );

            criteria = criteria.andOperator(sizeCriteria);
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
                .map(result -> LookupPatternPage.of(
                        skip,
                        limit,
                        result.getTotal(),
                        result.getMatches()
                                .stream()
                                .map(serializer::deserialize)
                                .toList()
                ));
    }

    @Override
    protected String stringifyId(PatternId patternId) {
        return patternId.getValue();
    }

    @Override
    protected Mono<Void> initializeIndices(ReactiveIndexOperations indexOps) {
        IndexDefinition categoriesIndex = new Index().on("categories", ASC);

        return indexOps.ensureIndex(categoriesIndex).then();
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

        List<MongoLookupPattern> matches;

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
