package de.bennyboer.kicherkrabbe.topics;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.LoggingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.inmemory.InMemoryEventSourcingRepo;
import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import de.bennyboer.kicherkrabbe.permissions.persistence.inmemory.InMemoryPermissionsRepo;
import de.bennyboer.kicherkrabbe.topics.persistence.lookup.TopicLookupRepo;
import de.bennyboer.kicherkrabbe.topics.persistence.lookup.inmemory.InMemoryTopicLookupRepo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.util.List;

public class TopicsModuleTest {

    private final TopicsModuleConfig config = new TopicsModuleConfig();

    private final TopicService topicService = new TopicService(
            new InMemoryEventSourcingRepo(),
            new LoggingEventPublisher(),
            Clock.systemUTC()
    );

    private final PermissionsService permissionsService = new PermissionsService(
            new InMemoryPermissionsRepo(),
            event -> Mono.empty()
    );

    private final TopicLookupRepo topicLookupRepo = new InMemoryTopicLookupRepo();

    private final TopicsModule module = config.topicsModule(
            topicService,
            permissionsService,
            topicLookupRepo,
            agent -> Flux.empty()
    );

    public void allowUserToCreateTopics(String userId) {
        module.allowUserToCreateTopics(userId).block();
    }

    public String createTopic(String name, Agent agent) {
        String topicId = module.createTopic(name, agent).block();

        module.updateTopicInLookup(topicId).block();
        if (agent.getType() == AgentType.USER) {
            module.allowUserToManageTopic(topicId, agent.getId().getValue()).block();
        }

        return topicId;
    }

    public long updateTopic(String topicId, long version, String name, Agent agent) {
        var updatedVersion = module.updateTopic(topicId, version, name, agent).block();

        module.updateTopicInLookup(topicId).block();

        return updatedVersion;
    }

    public long deleteTopic(String topicId, long version, Agent agent) {
        var updatedVersion = module.deleteTopic(topicId, version, agent).block();

        module.removeTopicFromLookup(topicId).block();
        module.removePermissionsForTopic(topicId).block();

        return updatedVersion;
    }

    public List<TopicDetails> getTopics(Agent agent) {
        return getTopics("", 0, Integer.MAX_VALUE, agent);
    }

    public List<TopicDetails> getTopics(String searchTerm, long skip, long limit, Agent agent) {
        return module.getTopics(searchTerm, skip, limit, agent).block().getResults();
    }

}
