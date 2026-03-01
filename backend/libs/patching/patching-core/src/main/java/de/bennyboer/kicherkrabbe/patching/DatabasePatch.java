package de.bennyboer.kicherkrabbe.patching;

import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Mono;

public interface DatabasePatch {

    int getVersion();

    Mono<Void> apply(ReactiveMongoTemplate template);

}
