package de.bennyboer.kicherkrabbe.messaging.outbox.persistence.mongo;

import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutboxEntry;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutboxEntryLock;
import de.bennyboer.kicherkrabbe.messaging.outbox.persistence.MessagingOutboxRepo;
import de.bennyboer.kicherkrabbe.messaging.outbox.persistence.mongo.transformer.MongoMessagingOutboxEntryTransformer;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.ReactiveIndexOperations;
import org.springframework.data.mongodb.core.query.Update;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.Instant;
import java.util.Collection;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.core.query.Update.update;

public class MongoMessagingOutboxRepo implements MessagingOutboxRepo {

    private final String collection;

    private final ReactiveMongoTemplate template;

    public MongoMessagingOutboxRepo(String collection, ReactiveMongoTemplate template) {
        this.collection = collection;
        this.template = template;

        initializeIndices();
    }

    @Override
    public Mono<Void> save(Collection<MessagingOutboxEntry> entries) {
        return Flux.fromIterable(entries)
                .map(MongoMessagingOutboxEntryTransformer::toMongoMessagingOutboxEntry)
                .flatMap(entry -> template.save(entry, collection))
                .then();
    }

    @Override
    public Mono<Void> insert(Collection<MessagingOutboxEntry> entries) {
        return Flux.fromIterable(entries)
                .map(MongoMessagingOutboxEntryTransformer::toMongoMessagingOutboxEntry)
                .collectList()
                .flatMapMany(entryList -> template.insert(entryList, collection))
                .then();
    }

    @Override
    public Mono<Void> lockNextPublishableEntries(MessagingOutboxEntryLock lock, int maxEntries, Clock clock) {
        return findNextPublishableEntries(maxEntries)
                .map(entry -> entry.id)
                .collectList()
                .flatMap(ids -> lockEntries(ids, lock, clock));
    }

    @Override
    public Flux<MessagingOutboxEntry> findLockedEntries(MessagingOutboxEntryLock lock) {
        var criteria = where("lock").is(lock.getValue());
        var query = query(criteria)
                .with(Sort.by(Sort.Order.asc("createdAt")));

        return template.find(query, MongoMessagingOutboxEntry.class, collection)
                .map(MongoMessagingOutboxEntryTransformer::toMessagingOutboxEntry);
    }

    @Override
    public Mono<Void> unlockEntriesOlderThan(Instant date) {
        var criteria = where("lockedAt").lt(date);
        var update = new Update()
                .unset("lockedAt")
                .unset("lock");

        return template.updateMulti(query(criteria), update, MongoMessagingOutboxEntry.class, collection)
                .then();
    }

    @Override
    public Mono<Void> removeAcknowledgedEntriesOlderThan(Instant date) {
        var criteria = where("acknowledgedAt").lt(date);
        var query = query(criteria);

        return template.remove(query, MongoMessagingOutboxEntry.class, collection)
                .then();
    }

    @Override
    public Flux<MessagingOutboxEntry> findFailedEntriesOlderThan(Instant date) {
        var criteria = where("failedAt").lt(date);
        var query = query(criteria)
                .with(Sort.by(Sort.Order.asc("createdAt")));

        return template.find(query, MongoMessagingOutboxEntry.class, collection)
                .map(MongoMessagingOutboxEntryTransformer::toMessagingOutboxEntry);
    }

    private Flux<MongoMessagingOutboxEntry> findNextPublishableEntries(int maxEntries) {
        var criteria = where("lockedAt").is(null)
                .and("failedAt").is(null)
                .and("acknowledgedAt").is(null);
        var query = query(criteria)
                .with(Sort.by(Sort.Order.asc("createdAt")))
                .limit(maxEntries);

        return template.find(query, MongoMessagingOutboxEntry.class, collection);
    }

    private Mono<Void> lockEntries(Collection<String> ids, MessagingOutboxEntryLock lock, Clock clock) {
        Instant now = clock.instant();

        var criteria = where("id").in(ids);
        var update = update("lockedAt", now)
                .set("lock", lock.getValue());

        return template.updateMulti(query(criteria), update, MongoMessagingOutboxEntry.class, collection)
                .then();
    }

    private void initializeIndices() {
        ReactiveIndexOperations indexOps = template.indexOps(collection);

        var publishableIndex = new CompoundIndexDefinition(new Document()
                .append("lockedAt", 1)
                .append("failedAt", 1)
                .append("acknowledgedAt", 1));
        var lockedAtIndex = new Index().on("lockedAt", Sort.Direction.ASC);
        var lockIndex = new Index().on("lock", Sort.Direction.ASC);
        var acknowledgedIndex = new Index().on("acknowledgedAt", Sort.Direction.ASC);

        Mono.zip(
                indexOps.ensureIndex(publishableIndex),
                indexOps.ensureIndex(lockedAtIndex),
                indexOps.ensureIndex(lockIndex),
                indexOps.ensureIndex(acknowledgedIndex)
        ).block();
    }

}
