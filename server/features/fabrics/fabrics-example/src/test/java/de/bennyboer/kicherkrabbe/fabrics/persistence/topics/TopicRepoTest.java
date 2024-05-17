package de.bennyboer.kicherkrabbe.fabrics.persistence.topics;

import de.bennyboer.kicherkrabbe.fabrics.TopicId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class TopicRepoTest {

    private TopicRepo repo;

    protected abstract TopicRepo createRepo();

    @BeforeEach
    public void setUp() {
        repo = createRepo();
    }

    @Test
    void shouldSaveTopic() {
        // given: a topic to save
        var topic = Topic.of(TopicId.of("TOPIC_ID"), TopicName.of("Topic Name"));

        // when: saving the topic
        save(topic);

        // then: the topic is saved
        var saved = findById(topic.getId());
        assertThat(saved).isEqualTo(topic);
    }

    @Test
    void shouldFindTopicById() {
        // given: some topics
        var topic1 = Topic.of(TopicId.of("TOPIC_ID_1"), TopicName.of("Topic Name 1"));
        var topic2 = Topic.of(TopicId.of("TOPIC_ID_2"), TopicName.of("Topic Name 2"));
        save(topic1);
        save(topic2);

        // when: finding the first topic by id
        var found1 = findById(topic1.getId());

        // then: the first topic is found
        assertThat(found1).isEqualTo(topic1);

        // when: finding the second topic by id
        var found2 = findById(topic2.getId());

        // then: the second topic is found
        assertThat(found2).isEqualTo(topic2);
    }

    @Test
    void shouldRemoveTopicById() {
        // given: some topics
        var topic1 = Topic.of(TopicId.of("TOPIC_ID_1"), TopicName.of("Topic Name 1"));
        var topic2 = Topic.of(TopicId.of("TOPIC_ID_2"), TopicName.of("Topic Name 2"));
        save(topic1);
        save(topic2);

        // when: removing the first topic by id
        removeById(topic1.getId());

        // then: the first topic is removed
        var found1 = findById(topic1.getId());
        assertThat(found1).isNull();

        // and: the second topic is still there
        var found2 = findById(topic2.getId());
        assertThat(found2).isEqualTo(topic2);

        // when: removing the second topic by id
        removeById(topic2.getId());

        // then: the second topic is removed
        var found3 = findById(topic2.getId());
        assertThat(found3).isNull();
    }

    @Test
    void shouldFindTopicsByIds() {
        // given: some topics
        var topic1 = Topic.of(TopicId.of("TOPIC_ID_1"), TopicName.of("Topic Name 1"));
        var topic2 = Topic.of(TopicId.of("TOPIC_ID_2"), TopicName.of("Topic Name 2"));
        var topic3 = Topic.of(TopicId.of("TOPIC_ID_3"), TopicName.of("Topic Name 3"));
        save(topic1);
        save(topic2);
        save(topic3);

        // when: finding the topics by ids
        var found = findByIds(List.of(topic1.getId(), topic3.getId()));

        // then: the topics are found
        assertThat(found).containsExactlyInAnyOrder(topic1, topic3);
    }

    private void save(Topic topic) {
        repo.save(topic).block();
    }

    private Topic findById(TopicId id) {
        return repo.findByIds(List.of(id)).blockFirst();
    }

    private void removeById(TopicId id) {
        repo.removeById(id).block();
    }

    private List<Topic> findByIds(Collection<TopicId> ids) {
        return repo.findByIds(ids).collectList().block();
    }

}
