package de.bennyboer.kicherkrabbe.fabrics.persistence.lookup.mongo;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.mongo.MongoEventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.fabrics.ColorId;
import de.bennyboer.kicherkrabbe.fabrics.FabricId;
import de.bennyboer.kicherkrabbe.fabrics.FabricTypeId;
import de.bennyboer.kicherkrabbe.fabrics.TopicId;
import de.bennyboer.kicherkrabbe.fabrics.persistence.lookup.FabricLookupRepo;
import de.bennyboer.kicherkrabbe.fabrics.persistence.lookup.LookupFabric;
import de.bennyboer.kicherkrabbe.fabrics.persistence.lookup.LookupFabricPage;
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

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

public class MongoFabricLookupRepo extends MongoEventSourcingReadModelRepo<FabricId, LookupFabric, MongoLookupFabric>
        implements FabricLookupRepo {

    public MongoFabricLookupRepo(ReactiveMongoTemplate template) {
        this("fabrics_lookup", template);
    }

    public MongoFabricLookupRepo(
            String collectionName,
            ReactiveMongoTemplate template
    ) {
        super(collectionName, template, new MongoLookupFabricSerializer());
    }

    @Override
    protected String stringifyId(FabricId fabricId) {
        return fabricId.getValue();
    }

    @Override
    public Mono<LookupFabricPage> find(Collection<FabricId> fabricIds, String searchTerm, long skip, long limit) {
        Set<String> ids = fabricIds.stream()
                .map(FabricId::getValue)
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
                .map(result -> LookupFabricPage.of(
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
    public Mono<LookupFabric> findPublished(FabricId id) {
        Criteria criteria = where("_id").is(id.getValue())
                .and("published").is(true);
        Query query = query(criteria);

        return template.findOne(query, MongoLookupFabric.class, collectionName)
                .map(serializer::deserialize);
    }

    @Override
    public Mono<LookupFabricPage> findPublished(
            String searchTerm,
            Set<ColorId> colors,
            Set<TopicId> topics,
            boolean filterAvailability,
            boolean inStock,
            boolean ascending,
            long skip,
            long limit
    ) {
        Set<String> colorIds = colors.stream()
                .map(ColorId::getValue)
                .collect(Collectors.toSet());
        Set<String> topicIds = topics.stream()
                .map(TopicId::getValue)
                .collect(Collectors.toSet());

        Criteria criteria = where("published").is(true);

        if (!searchTerm.isBlank()) {
            String quotedSearchTerm = Pattern.quote(searchTerm);
            criteria = criteria.and("name").regex(quotedSearchTerm, "i");
        }

        if (!colors.isEmpty()) {
            criteria = criteria.and("colorIds").in(colorIds);
        }

        if (!topics.isEmpty()) {
            criteria = criteria.and("topicIds").in(topicIds);
        }

        if (filterAvailability) {
            if (inStock) {
                criteria = criteria.and("availability.inStock").is(true);
            } else {
                criteria = criteria.and("availability.inStock").ne(true);
            }
        }

        AggregationOperation match = match(criteria);
        AggregationOperation sortBy = sort(ascending
                ? Sort.by(Sort.Order.asc("name"))
                : Sort.by(Sort.Order.desc("name")));
        AggregationOperation transformToPage = transformToPage(skip, limit);

        AggregationOptions options = AggregationOptions.builder()
                .collation(Collation.of("de-de"))
                .build();
        Aggregation aggregation = newAggregation(match, sortBy, transformToPage)
                .withOptions(options);

        return template.aggregate(aggregation, collectionName, PipelinePage.class)
                .next()
                .map(result -> LookupFabricPage.of(
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
    public Flux<LookupFabric> findByColor(ColorId colorId) {
        Criteria criteria = where("colorIds").is(colorId.getValue());
        Query query = query(criteria);

        return template.find(query, MongoLookupFabric.class, collectionName)
                .map(serializer::deserialize);
    }

    @Override
    public Flux<LookupFabric> findByTopic(TopicId topicId) {
        Criteria criteria = where("topicIds").is(topicId.getValue());
        Query query = query(criteria);

        return template.find(query, MongoLookupFabric.class, collectionName)
                .map(serializer::deserialize);
    }

    @Override
    public Flux<LookupFabric> findByFabricType(FabricTypeId fabricTypeId) {
        Criteria criteria = where("availability.fabricTypeId").is(fabricTypeId.getValue());
        Query query = query(criteria);

        return template.find(query, MongoLookupFabric.class, collectionName)
                .map(serializer::deserialize);
    }

    @Override
    public Flux<ColorId> findUniqueColors() {
        return template.query(MongoLookupFabric.class)
                .inCollection(collectionName)
                .distinct("colorIds")
                .as(String.class)
                .all()
                .map(ColorId::of);
    }

    @Override
    public Flux<TopicId> findUniqueTopics() {
        return template.query(MongoLookupFabric.class)
                .inCollection(collectionName)
                .distinct("topicIds")
                .as(String.class)
                .all()
                .map(TopicId::of);
    }

    @Override
    public Flux<FabricTypeId> findUniqueFabricTypes() {
        return template.query(MongoLookupFabric.class)
                .inCollection(collectionName)
                .distinct("availability.fabricTypeId")
                .as(String.class)
                .all()
                .map(FabricTypeId::of);
    }

    @Override
    protected Mono<Void> initializeIndices(ReactiveIndexOperations indexOps) {
        IndexDefinition createdAtIndex = new Index().on("createdAt", Sort.Direction.ASC);
        IndexDefinition nameIndex = new Index().on("name", Sort.Direction.ASC);
        IndexDefinition publishedIndex = new Index().on("published", Sort.Direction.ASC);
        IndexDefinition colorsIndex = new Index().on("colors", Sort.Direction.ASC);
        IndexDefinition topicsIndex = new Index().on("topics", Sort.Direction.ASC);
        IndexDefinition availabilityFabricTypeIndex = new Index().on("availability.fabricTypeId", Sort.Direction.ASC);
        IndexDefinition availabilityInStockIndex = new Index().on("availability.inStock", Sort.Direction.ASC);

        return indexOps.ensureIndex(createdAtIndex)
                .then(indexOps.ensureIndex(nameIndex))
                .then(indexOps.ensureIndex(publishedIndex))
                .then(indexOps.ensureIndex(colorsIndex))
                .then(indexOps.ensureIndex(topicsIndex))
                .then(indexOps.ensureIndex(availabilityFabricTypeIndex))
                .then(indexOps.ensureIndex(availabilityInStockIndex))
                .then();
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

        List<MongoLookupFabric> matches;

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

    @Value
    private static class PipelineColors {

        List<String> colorIds;

    }

}
