package de.bennyboer.kicherkrabbe.fabrics.persistence.topics;

import de.bennyboer.kicherkrabbe.fabrics.persistence.topics.inmemory.InMemoryTopicRepo;

public class InMemoryTopicRepoTest extends TopicRepoTest {

    @Override
    protected TopicRepo createRepo() {
        return new InMemoryTopicRepo();
    }

}
