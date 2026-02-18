package de.bennyboer.kicherkrabbe.assets.persistence.lookup.mongo;

import de.bennyboer.kicherkrabbe.assets.AssetId;
import de.bennyboer.kicherkrabbe.assets.ContentType;
import de.bennyboer.kicherkrabbe.assets.persistence.lookup.AssetLookupRepo;
import de.bennyboer.kicherkrabbe.assets.persistence.lookup.AssetsSortDirection;
import de.bennyboer.kicherkrabbe.assets.persistence.lookup.AssetsSortProperty;
import de.bennyboer.kicherkrabbe.assets.persistence.lookup.LookupAsset;
import de.bennyboer.kicherkrabbe.assets.persistence.lookup.LookupAssetPage;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.mongo.MongoEventSourcingReadModelRepo;
import lombok.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexDefinition;
import org.springframework.data.mongodb.core.index.ReactiveIndexOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;
import static org.springframework.data.mongodb.core.query.Criteria.where;

public class MongoAssetLookupRepo extends MongoEventSourcingReadModelRepo<AssetId, LookupAsset, MongoLookupAsset>
        implements AssetLookupRepo {

    public MongoAssetLookupRepo(ReactiveMongoTemplate template) {
        this("assets_lookup", template);
    }

    public MongoAssetLookupRepo(String collectionName, ReactiveMongoTemplate template) {
        super(collectionName, template, new MongoAssetLookupSerializer());
    }

    @Override
    protected String stringifyId(AssetId assetId) {
        return assetId.getValue();
    }

    @Override
    public Mono<LookupAssetPage> find(
            Collection<AssetId> assetIds,
            Set<ContentType> contentTypes,
            AssetsSortProperty sortProperty,
            AssetsSortDirection sortDirection,
            long skip,
            long limit
    ) {
        Set<String> ids = assetIds.stream()
                .map(AssetId::getValue)
                .collect(Collectors.toSet());

        Criteria criteria = where("_id").in(ids);

        if (!contentTypes.isEmpty()) {
            Set<String> types = contentTypes.stream()
                    .map(ContentType::getValue)
                    .collect(Collectors.toSet());
            criteria.and("contentType").in(types);
        }

        String sortField = switch (sortProperty) {
            case CREATED_AT -> "createdAt";
            case FILE_SIZE -> "fileSize";
        };

        Sort.Direction direction = sortDirection == AssetsSortDirection.ASCENDING
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        var match = match(criteria);
        var sortBy = sort(Sort.by(direction, sortField));
        var transformToPage = transformToPage(skip, limit);

        Aggregation aggregation = newAggregation(match, sortBy, transformToPage);

        return template.aggregate(aggregation, collectionName, PipelinePage.class)
                .next()
                .map(result -> LookupAssetPage.of(
                        skip,
                        limit,
                        result.getTotal(),
                        result.getMatches()
                                .stream()
                                .map(serializer::deserialize)
                                .toList()
                ))
                .defaultIfEmpty(LookupAssetPage.of(skip, limit, 0, List.of()));
    }

    @Override
    public Flux<ContentType> findUniqueContentTypes(Collection<AssetId> assetIds) {
        Set<String> ids = assetIds.stream()
                .map(AssetId::getValue)
                .collect(Collectors.toSet());

        Criteria criteria = where("_id").in(ids);

        Aggregation aggregation = newAggregation(
                match(criteria),
                group("contentType"),
                project().and("_id").as("contentType")
        );

        return template.aggregate(aggregation, collectionName, ContentTypeResult.class)
                .map(result -> ContentType.of(result.getContentType()));
    }

    @Override
    protected Mono<Void> initializeIndices(ReactiveIndexOperations indexOps) {
        IndexDefinition createdAtIndex = new Index().on("createdAt", Sort.Direction.ASC);
        IndexDefinition fileSizeIndex = new Index().on("fileSize", Sort.Direction.ASC);
        IndexDefinition contentTypeIndex = new Index().on("contentType", Sort.Direction.ASC);

        return indexOps.createIndex(createdAtIndex)
                .then(indexOps.createIndex(fileSizeIndex))
                .then(indexOps.createIndex(contentTypeIndex))
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

        List<MongoLookupAsset> matches;

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
    private static class ContentTypeResult {

        String contentType;

    }

}
