package de.bennyboer.kicherkrabbe.products.product.fabric.composition.update;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.products.product.FabricComposition;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class FabricCompositionUpdatedEvent implements Event {

    public static final EventName NAME = EventName.of("FABRIC_COMPOSITION_UPDATED");

    public static final Version VERSION = Version.zero();

    FabricComposition fabricComposition;

    public static FabricCompositionUpdatedEvent of(FabricComposition fabricComposition) {
        notNull(fabricComposition, "Fabric composition must be given");

        return new FabricCompositionUpdatedEvent(fabricComposition);
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
