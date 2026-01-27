package de.bennyboer.kicherkrabbe.eventsourcing.event.snapshot;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Map;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class SnapshotEvent implements Event {

    public static final EventName NAME = EventName.of("SNAPSHOT");

    public static final Version VERSION = Version.zero();

    Map<String, Object> state;

    public static SnapshotEvent of(Map<String, Object> state) {
        notNull(state, "State must be given");

        return new SnapshotEvent(state);
    }

    @Override
    public EventName getEventName() {
        return NAME;
    }

    @Override
    public Version getVersion() {
        return VERSION;
    }

    @Override
    public boolean isSnapshot() {
        return true;
    }

}
