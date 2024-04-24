package de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.mongo;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.EventSourcingReadModelRepo;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.ReactiveIndexOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import reactor.core.publisher.Mono;

public abstract class MongoEventSourcingReadModelRepo<ID, T, S> implements EventSourcingReadModelRepo<ID, T> {

    protected final String collectionName;

    protected final ReactiveMongoTemplate template;

    protected final ReadModelSerializer<T, S> serializer;

    public MongoEventSourcingReadModelRepo(
            String collectionName,
            ReactiveMongoTemplate template,
            ReadModelSerializer<T, S> serializer
    ) {
        this.collectionName = collectionName;
        this.template = template;
        this.serializer = serializer;

        this.initializeIndices(template.indexOps(collectionName)).block();
    }

    @Override
    public Mono<Void> update(T readModel) {
        return serializer.serialize(readModel)
                .flatMap(serialized -> template.save(serialized, collectionName))
                .then();
    }

    @Override
    public Mono<Void> remove(ID id) {
        Criteria criteria = Criteria.where("_id").is(stringifyId(id));

        return template.remove(criteria, collectionName)
                .then();
    }

    protected Mono<Void> initializeIndices(ReactiveIndexOperations indexOps) {
        return Mono.empty();
    }

    protected abstract String stringifyId(ID id);

}
