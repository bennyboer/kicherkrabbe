package de.bennyboer.kicherkrabbe.eventsourcing.example;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.inmemory.InMemoryEventSourcingRepo;

public class InMemoryRepoSampleAggregateTests extends SampleAggregateTests {

    @Override
    protected EventSourcingRepo createRepo() {
        return new InMemoryEventSourcingRepo();
    }

}
