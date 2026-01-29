package de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.mongo;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.EventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.VersionedReadModel;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.FindAndReplaceOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.ReactiveIndexOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Mono;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

public abstract class MongoEventSourcingReadModelRepo<ID, T extends VersionedReadModel<ID>, S>
        implements EventSourcingReadModelRepo<ID, T> {

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
        String id = stringifyId(readModel.getId());
        long version = readModel.getVersion().getValue();

        Criteria versionCriteria = allowSameVersionUpdate()
                ? where("version").not().gt(version)
                : where("version").not().gte(version);
        Criteria criteria = where("_id").is(id).andOperator(versionCriteria);
        Query query = query(criteria);

        S serialized = serializer.serialize(readModel);

        return template.findAndReplace(
                        query,
                        serialized,
                        FindAndReplaceOptions.options().upsert(),
                        collectionName
                )
                .onErrorResume(DuplicateKeyException.class, e -> Mono.empty())
                .then();
    }

    @Override
    public Mono<Void> remove(ID id) {
        Criteria criteria = where("_id").is(stringifyId(id));
        Query query = query(criteria);

        return template.remove(query, collectionName)
                .then();
    }

    protected Mono<Void> initializeIndices(ReactiveIndexOperations indexOps) {
        return Mono.empty();
    }

    protected boolean allowSameVersionUpdate() {
        return false;
    }

    protected abstract String stringifyId(ID id);

}
