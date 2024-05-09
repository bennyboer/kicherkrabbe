package de.bennyboer.kicherkrabbe.topics.persistence.lookup;

import de.bennyboer.kicherkrabbe.topics.persistence.lookup.inmemory.InMemoryTopicLookupRepo;

public class InMemoryTopicLookupRepoTest extends TopicLookupRepoTest {

    @Override
    protected TopicLookupRepo createRepo() {
        return new InMemoryTopicLookupRepo();
    }

}
