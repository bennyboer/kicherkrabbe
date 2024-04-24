package de.bennyboer.kicherkrabbe.auth.adapters.persistence.lookup.mongo;

import de.bennyboer.kicherkrabbe.auth.adapters.persistence.lookup.CredentialsLookup;
import de.bennyboer.kicherkrabbe.auth.adapters.persistence.lookup.CredentialsLookupRepo;
import de.bennyboer.kicherkrabbe.auth.internal.credentials.CredentialsId;
import de.bennyboer.kicherkrabbe.auth.internal.credentials.Name;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.mongo.MongoEventSourcingReadModelRepo;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexDefinition;
import org.springframework.data.mongodb.core.index.ReactiveIndexOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import reactor.core.publisher.Mono;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.mongodb.core.query.Query.query;

public class MongoCredentialsLookupRepo
        extends MongoEventSourcingReadModelRepo<CredentialsId, CredentialsLookup, MongoCredentialsLookup>
        implements CredentialsLookupRepo {

    public MongoCredentialsLookupRepo(ReactiveMongoTemplate template) {
        super("credentials_lookup", template, new MongoCredentialsLookupSerializer());
    }

    @Override
    public Mono<CredentialsId> findCredentialsIdByName(Name name) {
        Criteria criteria = Criteria.where("name").is(name.getValue());

        return template.findOne(query(criteria), MongoCredentialsLookup.class, collectionName)
                .flatMap(serializer::deserialize)
                .map(CredentialsLookup::getId);
    }

    @Override
    protected String stringifyId(CredentialsId o) {
        return o.toString();
    }

    @Override
    protected Mono<Void> initializeIndices(ReactiveIndexOperations indexOps) {
        IndexDefinition nameIndex = new Index().on("name", ASC);

        return indexOps.ensureIndex(nameIndex).then();
    }

}
