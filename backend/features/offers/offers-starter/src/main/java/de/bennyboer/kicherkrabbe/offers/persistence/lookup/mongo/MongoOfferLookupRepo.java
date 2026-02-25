package de.bennyboer.kicherkrabbe.offers.persistence.lookup.mongo;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.mongo.MongoEventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.offers.OfferCategoryId;
import de.bennyboer.kicherkrabbe.offers.OfferId;
import de.bennyboer.kicherkrabbe.offers.OfferSize;
import de.bennyboer.kicherkrabbe.offers.ProductId;
import de.bennyboer.kicherkrabbe.offers.persistence.lookup.*;
import jakarta.annotation.Nullable;
import static java.util.Objects.requireNonNull;
import lombok.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexDefinition;
import org.springframework.data.mongodb.core.index.ReactiveIndexOperations;
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

public class MongoOfferLookupRepo extends MongoEventSourcingReadModelRepo<OfferId, LookupOffer, MongoLookupOffer>
        implements OfferLookupRepo {

    public MongoOfferLookupRepo(ReactiveMongoTemplate template) {
        this("offers_lookup", template);
    }

    public MongoOfferLookupRepo(String collectionName, ReactiveMongoTemplate template) {
        super(collectionName, template, new MongoLookupOfferSerializer());
    }

    @Override
    protected String stringifyId(OfferId offerId) {
        return offerId.getValue();
    }

    @Override
    protected boolean allowSameVersionUpdate() {
        return true;
    }

    @Override
    public Mono<LookupOfferPage> find(Collection<OfferId> offerIds, String searchTerm, long skip, long limit) {
        Set<String> ids = offerIds.stream()
                .map(OfferId::getValue)
                .collect(Collectors.toSet());

        Criteria criteria = where("_id").in(ids);

        if (searchTerm != null && !searchTerm.isBlank()) {
            String quotedSearchTerm = Pattern.quote(searchTerm);
            criteria.andOperator(new Criteria().orOperator(
                    where("title").regex(quotedSearchTerm, "i"),
                    where("notes.description").regex(quotedSearchTerm, "i"),
                    where("product.number").regex(quotedSearchTerm, "i")
            ));
        }

        var match = match(criteria);
        var sortByCreationDate = sort(Sort.by(Sort.Order.desc("createdAt")));
        var transformToPage = transformToPage(skip, limit);

        Aggregation aggregation = newAggregation(match, sortByCreationDate, transformToPage);

        return template.aggregate(aggregation, collectionName, PipelinePage.class)
                .next()
                .map(result -> LookupOfferPage.of(
                        skip,
                        limit,
                        result.getTotal(),
                        result.getMatches().stream()
                                .map(serializer::deserialize)
                                .toList()
                ));
    }

    @Override
    public Mono<LookupOfferPage> findPublished(PublishedOfferQuery query) {
        requireNonNull(query);

        var searchTerm = query.getSearchTerm();
        var categories = query.getCategories();
        var sizes = query.getSizes();
        var minPrice = query.getMinPrice();
        var maxPrice = query.getMaxPrice();

        Criteria criteria = where("published").is(true)
                .and("archivedAt").isNull();

        if (searchTerm != null && !searchTerm.isBlank()) {
            String quotedSearchTerm = Pattern.quote(searchTerm);
            criteria.andOperator(new Criteria().orOperator(
                    where("title").regex(quotedSearchTerm, "i"),
                    where("notes.description").regex(quotedSearchTerm, "i"),
                    where("product.number").regex(quotedSearchTerm, "i")
            ));
        }

        if (categories != null && !categories.isEmpty()) {
            Set<String> categoryIds = categories.stream()
                    .map(OfferCategoryId::getValue)
                    .collect(Collectors.toSet());
            criteria = criteria.and("categoryIds").in(categoryIds);
        }

        if (sizes != null && !sizes.isEmpty()) {
            Set<String> sizeValues = sizes.stream()
                    .map(OfferSize::getValue)
                    .collect(Collectors.toSet());
            criteria = criteria.and("size").in(sizeValues);
        }

        if (minPrice != null && maxPrice != null) {
            criteria = criteria.and("pricing.effectivePriceAmount").gte(minPrice).lte(maxPrice);
        } else if (minPrice != null) {
            criteria = criteria.and("pricing.effectivePriceAmount").gte(minPrice);
        } else if (maxPrice != null) {
            criteria = criteria.and("pricing.effectivePriceAmount").lte(maxPrice);
        }

        var match = match(criteria);
        var sortOp = sort(resolveSort(query.getSortProperty(), query.getSortDirection()));
        var transformToPage = transformToPage(query.getSkip(), query.getLimit());

        Aggregation aggregation = newAggregation(match, sortOp, transformToPage);

        return template.aggregate(aggregation, collectionName, PipelinePage.class)
                .next()
                .map(result -> LookupOfferPage.of(
                        query.getSkip(),
                        query.getLimit(),
                        result.getTotal(),
                        result.getMatches().stream()
                                .map(serializer::deserialize)
                                .toList()
                ));
    }

    @Override
    public Mono<LookupOffer> findPublished(OfferId id) {
        Criteria criteria = where("_id").is(id.getValue())
                .and("published").is(true)
                .and("archivedAt").isNull();
        Query query = query(criteria);

        return template.findOne(query, MongoLookupOffer.class, collectionName)
                .map(serializer::deserialize);
    }

    @Override
    public Flux<LookupOffer> findByProductId(ProductId productId) {
        Criteria criteria = where("product.id").is(productId.getValue());
        Query query = query(criteria);

        return template.find(query, MongoLookupOffer.class, collectionName)
                .map(serializer::deserialize);
    }

    @Override
    public Flux<LookupOffer> findByCategoryId(OfferCategoryId categoryId) {
        Criteria criteria = where("categoryIds").is(categoryId.getValue());
        Query query = query(criteria);

        return template.find(query, MongoLookupOffer.class, collectionName)
                .map(serializer::deserialize);
    }

    @Override
    public Flux<String> findDistinctPublishedSizes() {
        Criteria criteria = where("published").is(true)
                .and("archivedAt").isNull();

        return template.findDistinct(Query.query(criteria), "size", collectionName, String.class)
                .sort();
    }

    @Override
    protected Mono<Void> initializeIndices(ReactiveIndexOperations indexOps) {
        IndexDefinition publishedOffersIndex = new Index()
                .on("published", Sort.Direction.ASC)
                .on("archivedAt", Sort.Direction.ASC)
                .on("createdAt", Sort.Direction.DESC);
        IndexDefinition productIdIndex = new Index().on("product.id", Sort.Direction.ASC);
        IndexDefinition categoryIdsIndex = new Index().on("categoryIds", Sort.Direction.ASC);
        IndexDefinition sizeIndex = new Index()
                .on("published", Sort.Direction.ASC)
                .on("size", Sort.Direction.ASC);
        IndexDefinition effectivePriceIndex = new Index()
                .on("published", Sort.Direction.ASC)
                .on("pricing.effectivePriceAmount", Sort.Direction.ASC);

        return indexOps.createIndex(publishedOffersIndex)
                .then(indexOps.createIndex(productIdIndex))
                .then(indexOps.createIndex(categoryIdsIndex))
                .then(indexOps.createIndex(sizeIndex))
                .then(indexOps.createIndex(effectivePriceIndex))
                .then();
    }

    private Sort resolveSort(
            @Nullable OfferSortProperty property,
            @Nullable OfferSortDirection direction
    ) {
        var effectiveProperty = property != null ? property : OfferSortProperty.NEWEST;
        var effectiveDirection = direction != null ? direction : OfferSortDirection.DESCENDING;
        var isAscending = effectiveDirection == OfferSortDirection.ASCENDING;

        String field = switch (effectiveProperty) {
            case ALPHABETICAL -> "title";
            case NEWEST -> "createdAt";
            case PRICE -> "pricing.effectivePriceAmount";
        };

        var order = isAscending ? Sort.Order.asc(field) : Sort.Order.desc(field);
        return Sort.by(order);
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

        List<MongoLookupOffer> matches;

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
