package de.bennyboer.kicherkrabbe.users.persistence.lookup.mongo;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.mongo.MongoEventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.users.Mail;
import de.bennyboer.kicherkrabbe.users.UserId;
import de.bennyboer.kicherkrabbe.users.persistence.lookup.LookupUser;
import de.bennyboer.kicherkrabbe.users.persistence.lookup.UserLookupRepo;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexDefinition;
import org.springframework.data.mongodb.core.index.ReactiveIndexOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Mono;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

public class MongoUserLookupRepo extends MongoEventSourcingReadModelRepo<UserId, LookupUser, MongoLookupUser>
        implements UserLookupRepo {

    public MongoUserLookupRepo(ReactiveMongoTemplate template) {
        this("users_lookup", template);
    }

    public MongoUserLookupRepo(String collectionName, ReactiveMongoTemplate template) {
        super(collectionName, template, new MongoLookupUserSerializer());
    }

    @Override
    protected String stringifyId(UserId id) {
        return id.getValue();
    }

    @Override
    public Mono<LookupUser> findByMail(Mail mail) {
        Criteria criteria = where("mail").is(mail.getValue());
        Query query = query(criteria);

        return template.findOne(query, MongoLookupUser.class, collectionName)
                .map(serializer::deserialize);
    }

    @Override
    public Mono<Long> count() {
        return template.count(query(new Criteria()), collectionName);
    }

    @Override
    protected Mono<Void> initializeIndices(ReactiveIndexOperations indexOps) {
        IndexDefinition mailIndex = new Index().on("mail", ASC);

        return indexOps.ensureIndex(mailIndex).then();
    }

}
