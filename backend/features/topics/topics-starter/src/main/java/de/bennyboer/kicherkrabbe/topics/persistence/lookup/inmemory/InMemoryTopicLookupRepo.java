package de.bennyboer.kicherkrabbe.topics.persistence.lookup.inmemory;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.inmemory.InMemoryEventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.topics.TopicId;
import de.bennyboer.kicherkrabbe.topics.persistence.lookup.LookupTopic;
import de.bennyboer.kicherkrabbe.topics.persistence.lookup.LookupTopicPage;
import de.bennyboer.kicherkrabbe.topics.persistence.lookup.TopicLookupRepo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Comparator;
import java.util.Locale;

public class InMemoryTopicLookupRepo extends InMemoryEventSourcingReadModelRepo<TopicId, LookupTopic>
        implements TopicLookupRepo {

    @Override
    protected TopicId getId(LookupTopic readModel) {
        return readModel.getId();
    }

    @Override
    public Mono<LookupTopicPage> find(Collection<TopicId> topicIds, String searchTerm, long skip, long limit) {
        return getAll()
                .filter(topic -> topicIds.contains(topic.getId()))
                .filter(topic -> {
                    if (searchTerm.isBlank()) {
                        return true;
                    }

                    return topic.getName()
                            .getValue()
                            .toLowerCase(Locale.ROOT)
                            .contains(searchTerm.toLowerCase(Locale.ROOT));
                })
                .sort(Comparator.comparing(LookupTopic::getCreatedAt))
                .collectList()
                .flatMap(topics -> {
                    long total = topics.size();

                    return Flux.fromIterable(topics)
                            .skip(skip)
                            .take(limit)
                            .collectList()
                            .map(results -> LookupTopicPage.of(skip, limit, total, results));
                });
    }

}
