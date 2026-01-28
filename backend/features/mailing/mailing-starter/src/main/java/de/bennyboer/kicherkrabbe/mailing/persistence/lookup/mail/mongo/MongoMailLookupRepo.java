package de.bennyboer.kicherkrabbe.mailing.persistence.lookup.mail.mongo;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.mongo.MongoEventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.mailing.mail.MailId;
import de.bennyboer.kicherkrabbe.mailing.persistence.lookup.mail.LookupMail;
import de.bennyboer.kicherkrabbe.mailing.persistence.lookup.mail.LookupMailPage;
import de.bennyboer.kicherkrabbe.mailing.persistence.lookup.mail.MailLookupRepo;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.ReactiveIndexOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.mongodb.core.query.Criteria.where;

public class MongoMailLookupRepo
        extends MongoEventSourcingReadModelRepo<MailId, LookupMail, MongoLookupMail>
        implements MailLookupRepo {

    public MongoMailLookupRepo(ReactiveMongoTemplate template) {
        this("mailing_mails_lookup", template);
    }

    public MongoMailLookupRepo(String collectionName, ReactiveMongoTemplate template) {
        super(collectionName, template, new MongoLookupMailSerializer());
    }

    @Override
    public Mono<LookupMail> findById(MailId id) {
        Criteria criteria = where("_id").is(id.getValue());
        Query query = Query.query(criteria);

        return template.findOne(query, MongoLookupMail.class, collectionName)
                .map(serializer::deserialize);
    }

    @Override
    public Mono<Long> countAfter(Instant instant) {
        Criteria criteria = where("sentAt").gt(instant);
        Query query = Query.query(criteria);

        return template.count(query, MongoLookupMail.class, collectionName);
    }

    @Override
    public Mono<LookupMailPage> query(long skip, long limit) {
        Sort sort = Sort.by(DESC, "sentAt");
        Query query = Query.query(new Criteria())
                .with(sort)
                .skip((int) skip)
                .limit((int) limit);

        return template.count(Query.query(new Criteria()), MongoLookupMail.class, collectionName)
                .flatMap(total -> template.find(query, MongoLookupMail.class, collectionName)
                        .map(serializer::deserialize)
                        .collectList()
                        .map(mails -> LookupMailPage.of(total, mails)));
    }

    @Override
    public Flux<LookupMail> findOlderThan(Instant instant) {
        Criteria criteria = where("sentAt").lt(instant);
        Query query = Query.query(criteria);

        return template.find(query, MongoLookupMail.class, collectionName)
                .map(serializer::deserialize);
    }

    @Override
    protected Mono<Void> initializeIndices(ReactiveIndexOperations indexOps) {
        Index sentAt = new Index().on("sentAt", DESC);

        return indexOps.createIndex(sentAt)
                .then();
    }

    @Override
    protected String stringifyId(MailId mailId) {
        return mailId.getValue();
    }

}
