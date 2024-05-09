package de.bennyboer.kicherkrabbe.topics.persistence.lookup;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.topics.TopicId;
import de.bennyboer.kicherkrabbe.topics.TopicName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class TopicLookupRepoTest {

    private TopicLookupRepo repo;

    protected abstract TopicLookupRepo createRepo();

    @BeforeEach
    public void setUp() {
        repo = createRepo();
    }

    @Test
    void shouldUpdateTopic() {
        // given: a topic to update
        var topic = LookupTopic.of(
                TopicId.create(),
                Version.zero(),
                TopicName.of("Winter"),
                Instant.parse("2024-03-12T12:30:00.00Z")
        );

        // when: updating the topic
        update(topic);

        // then: the topic is updated
        var topics = find(Set.of(topic.getId()));
        assertThat(topics).containsExactly(topic);
    }

    @Test
    void shouldRemoveTopic() {
        // given: some topics
        var topic1 = LookupTopic.of(
                TopicId.create(),
                Version.zero(),
                TopicName.of("Winter"),
                Instant.parse("2024-03-12T12:30:00.00Z")
        );
        var topic2 = LookupTopic.of(
                TopicId.create(),
                Version.zero(),
                TopicName.of("Fall"),
                Instant.parse("2024-03-12T12:30:00.00Z")
        );
        update(topic1);
        update(topic2);

        // when: removing a topic
        remove(topic1.getId());

        // then: the topic is removed
        var topics = find(Set.of(topic1.getId(), topic2.getId()));
        assertThat(topics).containsExactly(topic2);
    }

    @Test
    void shouldFindTopics() {
        // given: some topics
        var topic1 = LookupTopic.of(
                TopicId.create(),
                Version.zero(),
                TopicName.of("Winter"),
                Instant.parse("2024-03-12T13:00:00.00Z")
        );
        var topic2 = LookupTopic.of(
                TopicId.create(),
                Version.zero(),
                TopicName.of("Fall"),
                Instant.parse("2024-03-12T12:30:00.00Z")
        );
        update(topic1);
        update(topic2);

        // when: finding topics
        var topics = find(Set.of(topic1.getId(), topic2.getId()));

        // then: the topics are found sorted by creation date
        assertThat(topics).containsExactly(topic2, topic1);
    }

    @Test
    void shouldFindTopicsBySearchTerm() {
        // given: some topics
        var topic1 = LookupTopic.of(
                TopicId.create(),
                Version.zero(),
                TopicName.of("Winter"),
                Instant.parse("2024-03-12T13:00:00.00Z")
        );
        var topic2 = LookupTopic.of(
                TopicId.create(),
                Version.zero(),
                TopicName.of("Fall"),
                Instant.parse("2024-03-12T09:30:00.00Z")
        );
        var topic3 = LookupTopic.of(
                TopicId.create(),
                Version.zero(),
                TopicName.of("Spring"),
                Instant.parse("2024-03-12T11:00:00.00Z")
        );
        update(topic1);
        update(topic2);
        update(topic3);

        // when: finding topics by search term
        var topicIds = Set.of(topic1.getId(), topic2.getId(), topic3.getId());
        var topics = find(topicIds, "r");

        // then: the topics are found by search term
        assertThat(topics).containsExactly(topic3, topic1);

        // when: finding topics by another search term
        topics = find(topicIds, "fa");

        // then: the topics are found by another search term
        assertThat(topics).containsExactly(topic2);

        // when: finding topics by another search term
        topics = find(topicIds, "    ");

        // then: the topics are found by another search term
        assertThat(topics).containsExactly(topic2, topic3, topic1);

        // when: finding topics by another search term
        topics = find(topicIds, "blblblbll");

        // then: the topics are found by another search term
        assertThat(topics).isEmpty();
    }

    @Test
    void shouldFindTopicsWithPaging() {
        // given: some topics
        var topic1 = LookupTopic.of(
                TopicId.create(),
                Version.zero(),
                TopicName.of("Winter"),
                Instant.parse("2024-03-12T13:00:00.00Z")
        );
        var topic2 = LookupTopic.of(
                TopicId.create(),
                Version.zero(),
                TopicName.of("Fall"),
                Instant.parse("2024-03-12T12:30:00.00Z")
        );
        var topic3 = LookupTopic.of(
                TopicId.create(),
                Version.zero(),
                TopicName.of("Spring"),
                Instant.parse("2024-03-12T11:00:00.00Z")
        );
        update(topic1);
        update(topic2);
        update(topic3);

        // when: finding topics with paging
        var topicIds = Set.of(topic1.getId(), topic2.getId(), topic3.getId());
        var topics = find(topicIds, 1, 1);

        // then: the topics are found with paging
        assertThat(topics).containsExactly(topic2);

        // when: finding topics with paging
        topics = find(topicIds, 2, 1);

        // then: the topics are found with paging
        assertThat(topics).containsExactly(topic1);

        // when: finding topics with paging
        topics = find(topicIds, 3, 1);

        // then: the topics are found with paging
        assertThat(topics).isEmpty();

        // when: finding topics with paging
        topics = find(topicIds, 0, 2);

        // then: the topics are found with paging
        assertThat(topics).containsExactly(topic3, topic2);
    }

    @Test
    void shouldFindWithSearchTermAndPaging() {
        // given: some topics
        var topic1 = LookupTopic.of(
                TopicId.create(),
                Version.zero(),
                TopicName.of("Winter"),
                Instant.parse("2024-03-12T13:00:00.00Z")
        );
        var topic2 = LookupTopic.of(
                TopicId.create(),
                Version.zero(),
                TopicName.of("Fall"),
                Instant.parse("2024-03-12T09:30:00.00Z")
        );
        var topic3 = LookupTopic.of(
                TopicId.create(),
                Version.zero(),
                TopicName.of("Spring"),
                Instant.parse("2024-03-12T11:00:00.00Z")
        );
        update(topic1);
        update(topic2);
        update(topic3);

        // when: finding topics with search term and paging
        var topicIds = Set.of(topic1.getId(), topic2.getId(), topic3.getId());
        var page = findPage(topicIds, "r", 0, 1);

        // then: the topics are found with search term and paging
        assertThat(page.getResults()).containsExactly(topic3);
        assertThat(page.getTotal()).isEqualTo(2);

        // when: finding topics with search term and paging
        page = findPage(topicIds, "fa", 1, 1);

        // then: the topics are found with search term and paging
        assertThat(page.getResults()).isEmpty();
        assertThat(page.getTotal()).isEqualTo(1);
    }

    private void update(LookupTopic topic) {
        repo.update(topic).block();
    }

    private void remove(TopicId topicId) {
        repo.remove(topicId).block();
    }

    private List<LookupTopic> find(Collection<TopicId> topicIds) {
        return find(topicIds, "", 0, Integer.MAX_VALUE);
    }

    private List<LookupTopic> find(Collection<TopicId> topicIds, String searchTerm) {
        return find(topicIds, searchTerm, 0, Integer.MAX_VALUE);
    }

    private List<LookupTopic> find(Collection<TopicId> topicIds, long skip, long limit) {
        return find(topicIds, "", skip, limit);
    }

    private List<LookupTopic> find(Collection<TopicId> topicIds, String searchTerm, long skip, long limit) {
        return repo.find(topicIds, searchTerm, skip, limit).block().getResults();
    }

    private LookupTopicPage findPage(Collection<TopicId> topicIds, String searchTerm, long skip, long limit) {
        return repo.find(topicIds, searchTerm, skip, limit).block();
    }

}
