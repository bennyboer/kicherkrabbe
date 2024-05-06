package de.bennyboer.kicherkrabbe.colors.persistence.lookup.mongo;

import de.bennyboer.kicherkrabbe.colors.ColorId;
import de.bennyboer.kicherkrabbe.colors.persistence.lookup.ColorLookupRepo;
import de.bennyboer.kicherkrabbe.colors.persistence.lookup.LookupColor;
import de.bennyboer.kicherkrabbe.colors.persistence.lookup.LookupColorPage;
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

public class MongoColorLookupRepo extends MongoEventSourcingReadModelRepo<ColorId, LookupColor, MongoLookupColor>
        implements ColorLookupRepo {

    public MongoColorLookupRepo(ReactiveMongoTemplate template) {
        this("colors_lookup", template);
    }

    public MongoColorLookupRepo(String collectionName, ReactiveMongoTemplate template) {
        super(collectionName, template, new MongoLookupColorSerializer());
    }

    @Override
    public Mono<LookupColorPage> find(Collection<ColorId> colorIds, String searchTerm, long skip, long limit) {
        Set<String> ids = colorIds.stream()
                .map(ColorId::getValue)
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
                .map(result -> LookupColorPage.of(
                        skip,
                        limit,
                        result.getTotal(),
                        result.getMatches().stream().map(serializer::deserialize).toList()
                ));
    }

    @Override
    protected String stringifyId(ColorId colorId) {
        return colorId.getValue();
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

        List<MongoLookupColor> matches;

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
