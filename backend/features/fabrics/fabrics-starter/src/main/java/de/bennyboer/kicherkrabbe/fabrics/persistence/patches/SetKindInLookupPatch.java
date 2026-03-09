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
        var lookupQuery = new Query(where("kind").exists(false));
        var lookupUpdate = new Update().set("kind", "PATTERNED");

        var eventsQuery = new Query(where("name").is("CREATED").and("payload.kind").exists(false));
        var eventsUpdate = new Update().set("payload.kind", "PATTERNED");

        return template.updateMulti(lookupQuery, lookupUpdate, "fabrics_lookup")
                .then(template.updateMulti(eventsQuery, eventsUpdate, "fabrics_events"))
                .then();
    }

}
