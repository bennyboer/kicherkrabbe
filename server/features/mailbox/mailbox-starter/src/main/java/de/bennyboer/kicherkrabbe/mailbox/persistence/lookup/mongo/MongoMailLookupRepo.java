package de.bennyboer.kicherkrabbe.mailbox.persistence.lookup.mongo;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.mongo.MongoEventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.mailbox.mail.MailId;
import de.bennyboer.kicherkrabbe.mailbox.mail.Status;
import de.bennyboer.kicherkrabbe.mailbox.persistence.lookup.LookupMail;
import de.bennyboer.kicherkrabbe.mailbox.persistence.lookup.LookupMailPage;
import de.bennyboer.kicherkrabbe.mailbox.persistence.lookup.MailLookupRepo;
import jakarta.annotation.Nullable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.ReactiveIndexOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Mono;

import java.util.regex.Pattern;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.mongodb.core.query.Criteria.where;

public class MongoMailLookupRepo
        extends MongoEventSourcingReadModelRepo<MailId, LookupMail, MongoLookupMail>
        implements MailLookupRepo {

    public MongoMailLookupRepo(ReactiveMongoTemplate template) {
        this("mailbox_mails_lookup", template);
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
    public Mono<LookupMailPage> query(String searchTerm, @Nullable Status status, long skip, long limit) {
        Criteria criteria = new Criteria();
        if (status != null) {
            criteria = criteria.and("status").is(status);
        }

        if (!searchTerm.isBlank()) {
            String quotedSearchTerm = Pattern.quote(searchTerm);

            Criteria searchTermCriteria = new Criteria().orOperator(
                    where("subject").regex(quotedSearchTerm, "i"),
                    where("sender.name").regex(quotedSearchTerm, "i"),
                    where("sender.mail").regex(quotedSearchTerm, "i")
            );
            criteria = criteria.andOperator(searchTermCriteria);
        }

        Sort sort = Sort.by(DESC, "receivedAt");
        Query query = Query.query(criteria)
                .with(sort)
                .skip((int) skip)
                .limit((int) limit);

        return template.count(query, MongoLookupMail.class, collectionName)
                .flatMap(total -> template.find(query, MongoLookupMail.class, collectionName)
                        .map(serializer::deserialize)
                        .collectList()
                        .map(mails -> LookupMailPage.of(total, mails)));
    }

    @Override
    protected Mono<Void> initializeIndices(ReactiveIndexOperations indexOps) {
        Index statusReceivedAtIndex = new Index()
                .on("status", ASC)
                .on("receivedAt", DESC);

        return indexOps.ensureIndex(statusReceivedAtIndex)
                .then();
    }

    @Override
    protected String stringifyId(MailId mailId) {
        return mailId.getValue();
    }

}
