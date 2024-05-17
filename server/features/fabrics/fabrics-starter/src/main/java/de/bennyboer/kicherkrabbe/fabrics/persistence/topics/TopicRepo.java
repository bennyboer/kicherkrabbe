package de.bennyboer.kicherkrabbe.fabrics.persistence.topics;

import de.bennyboer.kicherkrabbe.fabrics.TopicId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface TopicRepo {

    Mono<Topic> save(Topic topic);

    Mono<Void> removeById(TopicId id);

    Flux<Topic> findByIds(Collection<TopicId> ids);

}
