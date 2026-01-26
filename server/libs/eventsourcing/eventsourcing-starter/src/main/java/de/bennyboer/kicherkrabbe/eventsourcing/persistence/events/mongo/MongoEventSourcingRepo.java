package de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.mongo;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.EventSerializer;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventWithMetadata;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.EventMetadata;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.snapshot.SnapshotAwareEventSerializer;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition;
import org.springframework.data.mongodb.core.index.IndexDefinition;
import org.springframework.data.mongodb.core.index.ReactiveIndexOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

public class MongoEventSourcingRepo implements EventSourcingRepo {

    private final String collection;

    private final ReactiveMongoTemplate template;

    private final EventSerializer serializer;

    public MongoEventSourcingRepo(
            String collection,
            ReactiveMongoTemplate template,
            EventSerializer serializer
    ) {
        this.collection = collection;
        this.template = template;
        this.serializer = SnapshotAwareEventSerializer.wrap(serializer);

        initializeIndices();
    }

    @Override
    public Mono<EventWithMetadata> insert(EventWithMetadata event) {
        MongoEvent mongoEvent = toMongoEvent(event);

        return template.insert(mongoEvent, collection)
                .onErrorMap(DuplicateKeyException.class, e -> new AggregateVersionOutdatedError(
                        event.getMetadata().getAggregateType(),
                        event.getMetadata().getAggregateId(),
                        event.getMetadata().getAggregateVersion()
                ))
                .thenReturn(event);
    }

    @Override
    public Mono<EventWithMetadata> findNearestSnapshotEventByAggregateIdAndType(
            AggregateId aggregateId,
            AggregateType type,
            Version version
    ) {
        Criteria criteria = where("aggregate._id").is(aggregateId.getValue())
                .and("aggregate.type").is(type.getValue())
                .and("aggregate.version").lte(version.getValue())
                .and("snapshot").is(true);

        return template.findOne(query(criteria), MongoEvent.class, collection)
                .map(this::toEventWithMetadata);
    }

    @Override
    public Mono<EventWithMetadata> findLatestSnapshotEventByAggregateIdAndType(
            AggregateId aggregateId,
            AggregateType type
    ) {
        Criteria criteria = where("aggregate._id").is(aggregateId.getValue())
                .and("aggregate.type").is(type.getValue())
                .and("snapshot").is(true);

        Query query = query(criteria)
                .with(Sort.by(DESC, "aggregate.version"));

        return template.findOne(query, MongoEvent.class, collection)
                .map(this::toEventWithMetadata);
    }

    @Override
    public Flux<EventWithMetadata> findEventsByAggregateIdAndType(
            AggregateId aggregateId,
            AggregateType type,
            Version fromVersion
    ) {
        Criteria criteria = where("aggregate._id").is(aggregateId.getValue())
                .and("aggregate.type").is(type.getValue())
                .and("aggregate.version").gte(fromVersion.getValue());

        Query query = query(criteria)
                .with(Sort.by(ASC, "aggregate.version"));

        return template.find(query, MongoEvent.class, collection)
                .map(this::toEventWithMetadata);
    }

    @Override
    public Flux<EventWithMetadata> findEventsByAggregateIdAndTypeUntilVersion(
            AggregateId aggregateId,
            AggregateType type,
            Version fromVersion,
            Version untilVersion
    ) {
        Criteria criteria = where("aggregate._id").is(aggregateId.getValue())
                .and("aggregate.type").is(type.getValue())
                .and("aggregate.version").gte(fromVersion.getValue()).lte(untilVersion.getValue());

        Query query = query(criteria)
                .with(Sort.by(ASC, "aggregate.version"));

        return template.find(query, MongoEvent.class, collection)
                .map(this::toEventWithMetadata);
    }

    @Override
    public Mono<Void> removeEventsByAggregateIdAndTypeUntilVersion(
            AggregateId aggregateId,
            AggregateType aggregateType,
            Version version
    ) {
        Criteria criteria = where("aggregate._id").is(aggregateId.getValue())
                .and("aggregate.type").is(aggregateType.getValue())
                .and("aggregate.version").lte(version.getValue());

        Query query = query(criteria);

        return template.remove(query, MongoEvent.class, collection)
                .then();
    }

    private MongoEvent toMongoEvent(EventWithMetadata event) {
        var result = new MongoEvent();

        result.id = new ObjectId().toHexString();
        result.aggregate = toMongoAggregate(event.getMetadata());
        result.agent = toMongoAgent(event.getMetadata().getAgent());
        result.date = event.getMetadata().getDate();
        result.name = event.getEvent().getEventName().getValue();
        result.version = event.getEvent().getVersion().getValue();
        result.snapshot = event.getEvent().isSnapshot();
        result.payload = serializer.serialize(event.getEvent());

        return result;
    }

    private MongoAggregate toMongoAggregate(EventMetadata metadata) {
        var result = new MongoAggregate();

        result.id = metadata.getAggregateId().getValue();
        result.type = metadata.getAggregateType().getValue();
        result.version = metadata.getAggregateVersion().getValue();

        return result;
    }

    private MongoAgent toMongoAgent(Agent agent) {
        var result = new MongoAgent();

        result.id = agent.getId().getValue();
        result.type = agent.getType().name();

        return result;
    }

    private EventWithMetadata toEventWithMetadata(MongoEvent mongoEvent) {
        Event event = toEvent(mongoEvent);
        EventMetadata metadata = toEventMetadata(mongoEvent);

        return EventWithMetadata.of(event, metadata);
    }

    private Event toEvent(MongoEvent mongoEvent) {
        var eventName = EventName.of(mongoEvent.name);
        var eventVersion = Version.of(mongoEvent.version);

        return serializer.deserialize(eventName, eventVersion, mongoEvent.payload);
    }

    private EventMetadata toEventMetadata(MongoEvent mongoEvent) {
        var aggregateId = AggregateId.of(mongoEvent.aggregate.id);
        var aggregateType = AggregateType.of(mongoEvent.aggregate.type);
        var aggregateVersion = Version.of(mongoEvent.aggregate.version);
        var agent = Agent.of(AgentType.valueOf(mongoEvent.agent.type), AgentId.of(mongoEvent.agent.id));
        var date = mongoEvent.date;
        var isSnapshot = mongoEvent.snapshot;

        return EventMetadata.of(
                aggregateId,
                aggregateType,
                aggregateVersion,
                agent,
                date,
                isSnapshot
        );
    }

    private void initializeIndices() {
        ReactiveIndexOperations indexOps = template.indexOps(collection);

        IndexDefinition versionIndex = new CompoundIndexDefinition(new Document()
                .append("aggregate._id", 1)
                .append("aggregate.type", 1)
                .append("aggregate.version", 1))
                .unique();

        IndexDefinition snapshotIndex = new CompoundIndexDefinition(new Document()
                .append("aggregate._id", 1)
                .append("aggregate.type", 1)
                .append("aggregate.version", 1)
                .append("snapshot", 1));

        Mono.zip(
                indexOps.createIndex(versionIndex),
                indexOps.createIndex(snapshotIndex)
        ).block();
    }

}
