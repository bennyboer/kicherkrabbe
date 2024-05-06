package de.bennyboer.kicherkrabbe.colors.persistence.lookup.mongo;

import de.bennyboer.kicherkrabbe.colors.ColorId;
import de.bennyboer.kicherkrabbe.colors.persistence.lookup.ColorLookupRepo;
import de.bennyboer.kicherkrabbe.colors.persistence.lookup.LookupColor;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.mongo.MongoEventSourcingReadModelRepo;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Flux;

import java.util.regex.Pattern;

import static org.springframework.data.mongodb.core.query.Query.query;

public class MongoColorLookupRepo extends MongoEventSourcingReadModelRepo<ColorId, LookupColor, MongoLookupColor>
        implements ColorLookupRepo {

    public MongoColorLookupRepo(ReactiveMongoTemplate template) {
        this("colors_lookup", template);
    }

    public MongoColorLookupRepo(String collectionName, ReactiveMongoTemplate template) {
        super(collectionName, template, new MongoLookupColorSerializer());
    }

    @Override
    public Flux<LookupColor> find(String searchTerm, long skip, long limit) {
        Criteria criteria = new Criteria();

        if (!searchTerm.isBlank()) {
            String quotedSearchTerm = Pattern.quote(searchTerm);
            criteria.and("name").regex(quotedSearchTerm, "i");
        }

        Sort sortByCreationDate = Sort.by(Sort.Order.asc("createdAt"));

        Query query = query(criteria)
                .skip(Math.toIntExact(skip))
                .limit(Math.toIntExact(limit))
                .with(sortByCreationDate);

        return template.find(query, MongoLookupColor.class, collectionName)
                .map(serializer::deserialize);
    }

    @Override
    protected String stringifyId(ColorId colorId) {
        return colorId.getValue();
    }

}
