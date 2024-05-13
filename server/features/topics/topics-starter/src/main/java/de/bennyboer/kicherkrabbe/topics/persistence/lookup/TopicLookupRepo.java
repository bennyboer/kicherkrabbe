package de.bennyboer.kicherkrabbe.topics.persistence.lookup;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.EventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.topics.TopicId;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface TopicLookupRepo extends EventSourcingReadModelRepo<TopicId, LookupTopic> {

    Mono<LookupTopicPage> find(Collection<TopicId> topicIds, String searchTerm, long skip, long limit);

}
