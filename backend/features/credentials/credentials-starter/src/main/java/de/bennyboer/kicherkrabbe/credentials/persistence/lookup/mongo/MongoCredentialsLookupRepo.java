package de.bennyboer.kicherkrabbe.credentials.persistence.lookup.mongo;

import de.bennyboer.kicherkrabbe.credentials.CredentialsId;
import de.bennyboer.kicherkrabbe.credentials.Name;
import de.bennyboer.kicherkrabbe.credentials.UserId;
import de.bennyboer.kicherkrabbe.credentials.persistence.lookup.LookupCredentials;
import de.bennyboer.kicherkrabbe.credentials.persistence.lookup.CredentialsLookupRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.mongo.MongoEventSourcingReadModelRepo;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexDefinition;
import org.springframework.data.mongodb.core.index.ReactiveIndexOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.mongodb.core.query.Query.query;

public class MongoCredentialsLookupRepo
        extends MongoEventSourcingReadModelRepo<CredentialsId, LookupCredentials, MongoLookupCredentials>
        implements CredentialsLookupRepo {

    public MongoCredentialsLookupRepo(ReactiveMongoTemplate template) {
        this("credentials_lookup", template);
    }

    public MongoCredentialsLookupRepo(String collectionName, ReactiveMongoTemplate template) {
        super(collectionName, template, new MongoLookupCredentialsSerializer());
    }

    @Override
    public Mono<LookupCredentials> findCredentialsByName(Name name) {
        Criteria criteria = Criteria.where("name").is(name.getValue());

        return template.findOne(query(criteria), MongoLookupCredentials.class, collectionName)
                .map(serializer::deserialize);
    }

    @Override
    public Flux<LookupCredentials> findCredentialsByUserId(UserId userId) {
        Criteria criteria = Criteria.where("userId").is(userId.getValue());

        return template.find(query(criteria), MongoLookupCredentials.class, collectionName)
                .map(serializer::deserialize);
    }

    @Override
    protected String stringifyId(CredentialsId o) {
        return o.getValue();
    }

    @Override
    protected Mono<Void> initializeIndices(ReactiveIndexOperations indexOps) {
        IndexDefinition nameIndex = new Index().on("name", ASC);

        return indexOps.createIndex(nameIndex).then();
    }

}
