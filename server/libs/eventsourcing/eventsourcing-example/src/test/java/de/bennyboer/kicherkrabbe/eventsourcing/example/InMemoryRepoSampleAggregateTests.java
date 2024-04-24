package de.bennyboer.kicherkrabbe.eventsourcing.example;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.inmemory.InMemoryEventSourcingRepo;

public class InMemoryRepoSampleAggregateTests extends SampleAggregateTests {

    @Override
    protected EventSourcingRepo createRepo() {
        return new InMemoryEventSourcingRepo();
    }

}
