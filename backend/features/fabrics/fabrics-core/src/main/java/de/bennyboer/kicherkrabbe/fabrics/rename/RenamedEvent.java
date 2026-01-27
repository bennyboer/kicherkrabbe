package de.bennyboer.kicherkrabbe.fabrics.rename;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.fabrics.FabricName;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class RenamedEvent implements Event {

    public static final EventName NAME = EventName.of("RENAMED");

    public static final Version VERSION = Version.zero();

    FabricName name;

    public static RenamedEvent of(FabricName name) {
        notNull(name, "Fabric name must be given");

        return new RenamedEvent(name);
    }

    @Override
    public EventName getEventName() {
        return NAME;
    }

    @Override
    public Version getVersion() {
        return VERSION;
    }

}
