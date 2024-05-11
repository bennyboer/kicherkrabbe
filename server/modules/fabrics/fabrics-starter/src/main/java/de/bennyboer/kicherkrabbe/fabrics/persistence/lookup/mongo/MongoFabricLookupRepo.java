package de.bennyboer.kicherkrabbe.fabrics.persistence.lookup.mongo;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.mongo.MongoEventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.fabrics.FabricId;
import de.bennyboer.kicherkrabbe.fabrics.persistence.lookup.FabricLookupRepo;
import de.bennyboer.kicherkrabbe.fabrics.persistence.lookup.LookupFabric;
import de.bennyboer.kicherkrabbe.fabrics.persistence.lookup.LookupFabricPage;
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

}
