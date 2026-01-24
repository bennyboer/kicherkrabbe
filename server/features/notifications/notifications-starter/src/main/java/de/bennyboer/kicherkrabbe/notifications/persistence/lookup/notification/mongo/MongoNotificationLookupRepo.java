package de.bennyboer.kicherkrabbe.notifications.persistence.lookup.notification.mongo;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.mongo.MongoEventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.notifications.DateRangeFilter;
import de.bennyboer.kicherkrabbe.notifications.notification.NotificationId;
import de.bennyboer.kicherkrabbe.notifications.persistence.lookup.notification.LookupNotification;
import de.bennyboer.kicherkrabbe.notifications.persistence.lookup.notification.LookupNotificationPage;
import de.bennyboer.kicherkrabbe.notifications.persistence.lookup.notification.NotificationLookupRepo;
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

public class MongoNotificationLookupRepo
        extends MongoEventSourcingReadModelRepo<NotificationId, LookupNotification, MongoLookupNotification>
        implements NotificationLookupRepo {

    public MongoNotificationLookupRepo(ReactiveMongoTemplate template) {
        this("notifications_lookup", template);
    }

    public MongoNotificationLookupRepo(String collectionName, ReactiveMongoTemplate template) {
        super(collectionName, template, new MongoLookupNotificationSerializer());
    }

    @Override
    public Mono<LookupNotification> findById(NotificationId id) {
        Criteria criteria = Criteria.where("_id").is(id.getValue());
        Query query = Query.query(criteria);

        return template.findOne(query, MongoLookupNotification.class, collectionName)
                .map(serializer::deserialize);
    }

    @Override
    public Mono<LookupNotificationPage> query(DateRangeFilter dateRangeFilter, long skip, long limit) {
        Criteria criteria = new Criteria();

        Instant from = dateRangeFilter.getFrom().orElse(null);
        Instant to = dateRangeFilter.getTo().orElse(null);
        if (from != null && to != null) {
            criteria = criteria.and("sentAt").gte(from).lt(to);
        } else if (from != null) {
            criteria = criteria.and("sentAt").gte(from);
        } else if (to != null) {
            criteria = criteria.and("sentAt").lt(to);
        }

        Query query = Query.query(criteria)
                .with(Sort.by(DESC, "sentAt"))
                .skip((int) skip)
                .limit((int) limit);

        return template.count(Query.query(criteria), MongoLookupNotification.class, collectionName)
                .flatMap(total -> template.find(query, MongoLookupNotification.class, collectionName)
                        .map(serializer::deserialize)
                        .collectList()
                        .map(notifications -> LookupNotificationPage.of(total, notifications)));
    }

    @Override
    public Flux<LookupNotification> findOlderThan(Instant instant) {
        Criteria criteria = Criteria.where("sentAt").lt(instant);
        Query query = Query.query(criteria);

        return template.find(query, MongoLookupNotification.class, collectionName)
                .map(serializer::deserialize);
    }

    @Override
    protected Mono<Void> initializeIndices(ReactiveIndexOperations indexOps) {
        Index sentAtIndex = new Index().on("sentAt", DESC);

        return indexOps.createIndex(sentAtIndex)
                .then();
    }

    @Override
    protected String stringifyId(NotificationId notificationId) {
        return notificationId.getValue();
    }

}
