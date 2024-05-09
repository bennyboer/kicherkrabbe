package de.bennyboer.kicherkrabbe.fabrictypes.persistence.lookup.mongo;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.mongo.MongoEventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.fabrictypes.FabricTypeId;
import de.bennyboer.kicherkrabbe.fabrictypes.persistence.lookup.FabricTypeLookupRepo;
import de.bennyboer.kicherkrabbe.fabrictypes.persistence.lookup.LookupFabricType;
import de.bennyboer.kicherkrabbe.fabrictypes.persistence.lookup.LookupFabricTypePage;
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

public class MongoFabricTypeLookupRepo
        extends MongoEventSourcingReadModelRepo<FabricTypeId, LookupFabricType, MongoLookupFabricType>
        implements FabricTypeLookupRepo {

    public MongoFabricTypeLookupRepo(ReactiveMongoTemplate template) {
        this("fabric_types_lookup", template);
    }

    public MongoFabricTypeLookupRepo(String collectionName, ReactiveMongoTemplate template) {
        super(collectionName, template, new MongoLookupFabricTypeSerializer());
    }

    @Override
    public Mono<LookupFabricTypePage> find(
            Collection<FabricTypeId> fabricTypeIds,
            String searchTerm,
            long skip,
            long limit
    ) {
        Set<String> ids = fabricTypeIds.stream()
                .map(FabricTypeId::getValue)
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
                .map(result -> LookupFabricTypePage.of(
                        skip,
                        limit,
                        result.getTotal(),
                        result.getMatches().stream().map(serializer::deserialize).toList()
                ));
    }

    @Override
    protected String stringifyId(FabricTypeId fabricTypeId) {
        return fabricTypeId.getValue();
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

        List<MongoLookupFabricType> matches;

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
