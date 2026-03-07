package de.bennyboer.kicherkrabbe.fabrics.persistence.patches;

import de.bennyboer.kicherkrabbe.patching.DatabasePatch;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import reactor.core.publisher.Mono;

import static org.springframework.data.mongodb.core.query.Criteria.where;

public class SetKindInLookupPatch implements DatabasePatch {

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public Mono<Void> apply(ReactiveMongoTemplate template) {
        var query = new Query(where("kind").exists(false));
        var update = new Update().set("kind", "PATTERNED");

        return template.updateMulti(query, update, "fabrics_lookup").then();
    }

}
