package de.bennyboer.kicherkrabbe.eventsourcing.event;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;

/**
 * Events in CQRS/Event Sourcing are the result of a command.
 * They are immutable and represent a change to the aggregate.
 */
public interface Event {

    EventName getEventName();

    /**
     * The version of the event.
     * This is not the version of the aggregate, but rather the version of the event itself!
     * We need this to be able to migrate old events to the current version.
     */
    Version getVersion();

    default boolean isSnapshot() {
        return false;
    }

}
