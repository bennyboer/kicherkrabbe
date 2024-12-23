package de.bennyboer.kicherkrabbe.topics;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.LoggingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.inmemory.InMemoryEventSourcingRepo;
import org.junit.jupiter.api.Test;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TopicServiceTest {

    private final EventSourcingRepo repo = new InMemoryEventSourcingRepo();

    private final LoggingEventPublisher eventPublisher = new LoggingEventPublisher();

    private final TopicService topicService = new TopicService(repo, eventPublisher, Clock.systemUTC());

    @Test
    void shouldCreateTopic() {
        // given: a name to create a topic for
        var name = TopicName.of("Autumn");

        // when: creating a topic
        var id = create(name);

        // then: the topic is created
        var topic = get(id);
        assertThat(topic.getId()).isEqualTo(id);
        assertThat(topic.getVersion()).isEqualTo(Version.zero());
        assertThat(topic.getName()).isEqualTo(name);
        assertThat(topic.isNotDeleted()).isTrue();
    }

    @Test
    void shouldUpdateTopic() {
        // given: a topic
        var id = create(TopicName.of("Summer"));

        // when: updating the topic
        var updatedVersion = update(id, Version.zero(), TopicName.of("Fall"));

        // then: the topic is updated
        var topic = get(id);
        assertThat(topic.getId()).isEqualTo(id);
        assertThat(topic.getVersion()).isEqualTo(updatedVersion);
        assertThat(topic.getName()).isEqualTo(TopicName.of("Fall"));
        assertThat(topic.isNotDeleted()).isTrue();
    }

    @Test
    void shouldNotUpdateTopicGivenAnOutdatedVersion() {
        // given: a topic
        var id = create(TopicName.of("Winter"));

        // and: the topic is updated
        update(id, Version.zero(), TopicName.of("Spring"));

        // when: updating the topic with an outdated version; then: an error is raised
        assertThatThrownBy(() -> update(id, Version.zero(), TopicName.of("Summer")))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldDeleteTopic() {
        // given: a topic
        var id = create(TopicName.of("Winter"));

        // when: deleting the topic
        delete(id, Version.zero());

        // then: the topic is deleted
        assertThat(get(id)).isNull();
    }

    @Test
    void shouldNotDeleteTopicGivenAnOutdatedVersion() {
        // given: a topic
        var id = create(TopicName.of("Spring"));

        // and: the topic is updated
        update(id, Version.zero(), TopicName.of("Summer"));

        // when: deleting the topic with an outdated version; then: an error is raised
        assertThatThrownBy(() -> delete(id, Version.zero()))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldSnapshotEvery100Events() {
        // given: a topic
        var id = create(TopicName.of("Spring"));

        // when: updating the topic 200 times
        var version = Version.zero();
        for (int i = 0; i < 200; i++) {
            version = update(id, version, TopicName.of("Summer " + i));
        }

        // then: the topic is updated
        var topic = get(id);
        assertThat(topic.getVersion()).isEqualTo(Version.of(202));
        assertThat(topic.getName()).isEqualTo(TopicName.of("Summer 199"));

        // and: there are exactly 2 snapshot events in the repository
        var events = repo.findEventsByAggregateIdAndType(
                AggregateId.of(id.getValue()),
                Topic.TYPE,
                Version.zero()
        ).collectList().block();
        var snapshotEvents = events.stream().filter(e -> e.getMetadata().isSnapshot()).toList();
        assertThat(snapshotEvents).hasSize(2);
        assertThat(snapshotEvents.getFirst().getMetadata().getAggregateVersion()).isEqualTo(Version.of(100));
        assertThat(snapshotEvents.getLast().getMetadata().getAggregateVersion()).isEqualTo(Version.of(200));
    }

    private Topic get(TopicId id) {
        return topicService.get(id).block();
    }

    private TopicId create(TopicName name) {
        return topicService.create(name, Agent.system()).block().getId();
    }

    private Version update(TopicId id, Version version, TopicName name) {
        return topicService.update(id, version, name, Agent.system()).block();
    }

    private void delete(TopicId id, Version version) {
        topicService.delete(id, version, Agent.system()).block();
    }

}
