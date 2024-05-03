package de.bennyboer.kicherkrabbe.fabrics.types.create;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.fabrics.types.FabricTypeName;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class CreatedEvent implements Event {

    public static final EventName NAME = EventName.of("CREATED");

    public static final Version VERSION = Version.zero();

    FabricTypeName name;

    public static CreatedEvent of(FabricTypeName name) {
        notNull(name, "Fabric type name must be given");

        return new CreatedEvent(name);
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
