package de.bennyboer.kicherkrabbe.fabrics.delete.colors;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.fabrics.ColorId;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class ColorRemovedEvent implements Event {

    public static final EventName NAME = EventName.of("COLOR_REMOVED");

    public static final Version VERSION = Version.zero();

    ColorId colorId;

    public static ColorRemovedEvent of(ColorId colorId) {
        notNull(colorId, "Color ID must be given");

        return new ColorRemovedEvent(colorId);
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
