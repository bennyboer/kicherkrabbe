package de.bennyboer.kicherkrabbe.highlights.persistence.lookup.mongo;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.mongo.MongoEventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.highlights.HighlightId;
import de.bennyboer.kicherkrabbe.highlights.LinkId;
import de.bennyboer.kicherkrabbe.highlights.LinkType;
import de.bennyboer.kicherkrabbe.highlights.persistence.lookup.HighlightLookupRepo;
import de.bennyboer.kicherkrabbe.highlights.persistence.lookup.LookupHighlight;
import de.bennyboer.kicherkrabbe.highlights.persistence.lookup.LookupHighlightPage;
import lombok.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;
import static org.springframework.data.mongodb.core.query.Criteria.where;

public class MongoHighlightLookupRepo
        extends MongoEventSourcingReadModelRepo<HighlightId, LookupHighlight, MongoLookupHighlight>
        implements HighlightLookupRepo {

    public MongoHighlightLookupRepo(ReactiveMongoTemplate template) {
        this("highlights_lookup", template);
    }

    public MongoHighlightLookupRepo(String collectionName, ReactiveMongoTemplate template) {
        super(collectionName, template, new MongoLookupHighlightSerializer());
    }

    @Override
    public Flux<LookupHighlight> findPublished() {
        var query = new Query(where("published").is(true))
                .with(Sort.by(Sort.Order.asc("sortOrder")));

        return template.find(query, MongoLookupHighlight.class, collectionName)
                .map(serializer::deserialize);
    }

    @Override
    public Mono<LookupHighlightPage> findAll(Collection<HighlightId> highlightIds, long skip, long limit) {
        Set<String> ids = highlightIds.stream()
                .map(HighlightId::getValue)
                .collect(Collectors.toSet());

        Criteria criteria = where("_id").in(ids);

        var match = match(criteria);
        var sortBySortOrder = sort(Sort.by(Sort.Order.asc("sortOrder")));
        var transformToPage = transformToPage(skip, limit);

        Aggregation aggregation = newAggregation(match, sortBySortOrder, transformToPage);

        return template.aggregate(aggregation, collectionName, PipelinePage.class)
                .next()
                .map(result -> LookupHighlightPage.of(
                        skip,
                        limit,
                        result.getTotal(),
                        result.getMatches().stream().map(serializer::deserialize).toList()
                ));
    }

    @Override
    public Mono<LookupHighlight> findById(HighlightId highlightId) {
        return template.findById(highlightId.getValue(), MongoLookupHighlight.class, collectionName)
                .map(serializer::deserialize);
    }

    @Override
    public Flux<LookupHighlight> findByLink(LinkType linkType, LinkId linkId) {
        Criteria criteria = where("links.id").is(linkId.getValue())
                .and("links.type").is(linkType.name());
        Query query = new Query(criteria);

        return template.find(query, MongoLookupHighlight.class, collectionName)
                .map(serializer::deserialize);
    }

    @Override
    protected String stringifyId(HighlightId highlightId) {
        return highlightId.getValue();
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

        List<MongoLookupHighlight> matches;

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
