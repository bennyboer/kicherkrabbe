package de.bennyboer.kicherkrabbe.highlights.persistence.lookup.links.mongo;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.mongo.MongoEventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.highlights.Link;
import de.bennyboer.kicherkrabbe.highlights.LinkId;
import de.bennyboer.kicherkrabbe.highlights.LinkType;
import de.bennyboer.kicherkrabbe.highlights.persistence.lookup.links.LinkLookupRepo;
import de.bennyboer.kicherkrabbe.highlights.persistence.lookup.links.LinkPage;
import de.bennyboer.kicherkrabbe.highlights.persistence.lookup.links.LookupLink;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.ReactiveIndexOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Mono;

import java.util.regex.Pattern;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

public class MongoLinkLookupRepo
        extends MongoEventSourcingReadModelRepo<String, LookupLink, MongoLookupLink>
        implements LinkLookupRepo {

    public MongoLinkLookupRepo(ReactiveMongoTemplate template) {
        this("highlights_links_lookup", template);
    }

    public MongoLinkLookupRepo(
            String collectionName,
            ReactiveMongoTemplate template
    ) {
        super(collectionName, template, new MongoLookupLinkSerializer());
    }

    @Override
    protected String stringifyId(String linkId) {
        return linkId;
    }

    @Override
    public Mono<LinkPage> find(String searchTerm, long skip, long limit) {
        Criteria criteria = new Criteria();

        if (!searchTerm.isBlank()) {
            String quotedSearchTerm = Pattern.quote(searchTerm);

            Criteria searchTermCriteria = where("name").regex(quotedSearchTerm, "i");
            criteria = criteria.andOperator(searchTermCriteria);
        }

        Query query = query(criteria)
                .with(Sort.by(ASC, "name"))
                .skip((int) skip)
                .limit((int) limit);

        return template.count(Query.query(criteria), MongoLookupLink.class, collectionName)
                .flatMap(total -> template.find(query, MongoLookupLink.class, collectionName)
                        .map(serializer::deserialize)
                        .map(LookupLink::toLink)
                        .collectList()
                        .map(links -> LinkPage.of(total, links)));
    }

    @Override
    public Mono<Link> findOne(LinkType type, LinkId linkId) {
        Criteria criteria = where("type").is(type)
                .and("linkId").is(linkId.getValue());
        Query query = query(criteria);

        return template.findOne(query, MongoLookupLink.class, collectionName)
                .map(serializer::deserialize)
                .map(LookupLink::toLink);
    }

    @Override
    public Mono<Void> remove(LinkType type, LinkId id) {
        Criteria criteria = where("type").is(type)
                .and("linkId").is(id.getValue());
        Query query = query(criteria);

        return template.remove(query, MongoLookupLink.class, collectionName)
                .then();
    }

    @Override
    protected Mono<Void> initializeIndices(ReactiveIndexOperations indexOps) {
        var typeAndIdIndex = new Index().on("type", ASC)
                .on("linkId", ASC)
                .unique();

        return indexOps.createIndex(typeAndIdIndex)
                .then();
    }

}
