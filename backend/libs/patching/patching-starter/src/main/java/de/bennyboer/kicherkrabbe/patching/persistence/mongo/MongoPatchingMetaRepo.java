package de.bennyboer.kicherkrabbe.patching.persistence.mongo;

import de.bennyboer.kicherkrabbe.patching.InstanceId;
import de.bennyboer.kicherkrabbe.patching.PatchingInProgressException;
import de.bennyboer.kicherkrabbe.patching.PatchingMeta;
import lombok.AllArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.Duration;
import java.util.Optional;

@AllArgsConstructor
public class MongoPatchingMetaRepo {

    private static final String COLLECTION_NAME = "patching_meta";
    private static final String DOC_ID = "patching_meta";

    private final ReactiveMongoTemplate template;

    public Mono<PatchingMeta> tryAcquireLock(InstanceId instanceId, Duration lockTimeout, Clock clock) {
        var now = clock.instant();
        var lockExpiry = now.minus(lockTimeout);

        var query = Query.query(
                new Criteria().andOperator(
                        Criteria.where("_id").is(DOC_ID),
                        new Criteria().orOperator(
                                Criteria.where("lockedBy").isNull(),
                                Criteria.where("lockedAt").lt(lockExpiry)
                        )
                )
        );

        var update = new Update()
                .set("lockedBy", instanceId.getValue())
                .set("lockedAt", now)
                .setOnInsert("version", 0);

        var options = FindAndModifyOptions.options()
                .returnNew(true)
                .upsert(true);

        return template.findAndModify(query, update, options, MongoPatchingMeta.class, COLLECTION_NAME)
                .map(this::fromMongo)
                .switchIfEmpty(Mono.error(new PatchingInProgressException()))
                .onErrorResume(DuplicateKeyException.class, ignored -> Mono.error(new PatchingInProgressException()));
    }

    public Mono<Void> releaseLock(InstanceId instanceId) {
        var query = Query.query(
                Criteria.where("_id").is(DOC_ID)
                        .and("lockedBy").is(instanceId.getValue())
        );

        var update = new Update()
                .set("lockedBy", null)
                .set("lockedAt", null);

        return template.updateFirst(query, update, COLLECTION_NAME).then();
    }

    public Mono<Void> updateVersion(int newVersion, InstanceId instanceId) {
        var query = Query.query(
                Criteria.where("_id").is(DOC_ID)
                        .and("lockedBy").is(instanceId.getValue())
        );

        var update = new Update().set("version", newVersion);

        return template.updateFirst(query, update, COLLECTION_NAME)
                .handle((result, sink) -> {
                    if (result.getModifiedCount() == 0) {
                        sink.error(new IllegalStateException("Failed to update version - lock not held"));
                    }
                });
    }

    public Mono<PatchingMeta> findMeta() {
        var query = Query.query(Criteria.where("_id").is(DOC_ID));

        return template.findOne(query, MongoPatchingMeta.class, COLLECTION_NAME)
                .map(this::fromMongo);
    }

    private PatchingMeta fromMongo(MongoPatchingMeta doc) {
        var lockedBy = Optional.ofNullable(doc.lockedBy).map(InstanceId::of).orElse(null);
        return PatchingMeta.of(doc.version, lockedBy);
    }

}
