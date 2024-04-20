package de.bennyboer.kicherkrabbe.eventsourcing.example.patches;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.eventsourcing.example.SampleAggregate;
import de.bennyboer.kicherkrabbe.eventsourcing.example.events.CreatedEvent;
import de.bennyboer.kicherkrabbe.eventsourcing.example.events.CreatedEvent2;
import de.bennyboer.kicherkrabbe.eventsourcing.patch.Patch;

public class CreatedEventPatch1 implements Patch {

    @Override
    public Version fromVersion() {
        return CreatedEvent.VERSION;
    }

    @Override
    public Version toVersion() {
        return CreatedEvent2.VERSION;
    }

    @Override
    public AggregateType aggregateType() {
        return SampleAggregate.TYPE;
    }

    @Override
    public EventName eventName() {
        return CreatedEvent.NAME;
    }

    @Override
    public Event apply(Event event) {
        return CreatedEvent2.from((CreatedEvent) event);
    }

}
