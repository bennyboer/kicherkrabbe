package de.bennyboer.kicherkrabbe.fabrics.persistence.topics.inmemory;

import de.bennyboer.kicherkrabbe.fabrics.TopicId;
import de.bennyboer.kicherkrabbe.fabrics.persistence.topics.Topic;
import de.bennyboer.kicherkrabbe.fabrics.persistence.topics.TopicRepo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryTopicRepo implements TopicRepo {

    private final Map<TopicId, Topic> lookup = new ConcurrentHashMap<>();

    @Override
    public Mono<Topic> save(Topic topic) {
        return Mono.fromCallable(() -> {
            lookup.put(topic.getId(), topic);
            return topic;
        });
    }

    @Override
    public Mono<Void> removeById(TopicId id) {
        return Mono.fromCallable(() -> {
            lookup.remove(id);
            return null;
        });
    }

    @Override
    public Flux<Topic> findByIds(Collection<TopicId> ids) {
        return Flux.fromIterable(ids)
                .mapNotNull(lookup::get);
    }

    @Override
    public Flux<Topic> findAll() {
        return Flux.fromIterable(lookup.values());
    }

}
