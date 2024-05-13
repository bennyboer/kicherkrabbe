package de.bennyboer.kicherkrabbe.fabrics.delete.fabrictype;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.fabrics.FabricTypeId;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class FabricTypeRemovedEvent implements Event {

    public static final EventName NAME = EventName.of("FABRIC_TYPE_REMOVED");

    public static final Version VERSION = Version.zero();

    FabricTypeId fabricTypeId;

    public static FabricTypeRemovedEvent of(FabricTypeId fabricTypeId) {
        notNull(fabricTypeId, "Fabric type ID must be given");

        return new FabricTypeRemovedEvent(fabricTypeId);
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
