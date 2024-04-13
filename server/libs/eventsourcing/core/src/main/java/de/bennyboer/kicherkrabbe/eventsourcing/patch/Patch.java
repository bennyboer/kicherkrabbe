package de.bennyboer.kicherkrabbe.eventsourcing.patch;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;

/**
 * A patch is able to turn an old version of an event into a new one.
 * That way we are able to migrate old events to the current version.
 * For example when a new field is added to an aggregate, we can use a patch
 * to add the field to the old events so that the events do not need to be migrated
 * manually in the database.
 */
public interface Patch {

    Version fromVersion();

    Version toVersion();

    AggregateType aggregateType();

    EventName eventName();

    Event apply(Event event);

}
