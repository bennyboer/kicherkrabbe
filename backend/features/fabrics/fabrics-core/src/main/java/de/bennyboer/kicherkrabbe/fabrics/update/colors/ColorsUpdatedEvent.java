package de.bennyboer.kicherkrabbe.fabrics.update.colors;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.fabrics.ColorId;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Set;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class ColorsUpdatedEvent implements Event {

    public static final EventName NAME = EventName.of("COLORS_UPDATED");

    public static final Version VERSION = Version.zero();

    Set<ColorId> colors;

    public static ColorsUpdatedEvent of(Set<ColorId> colors) {
        notNull(colors, "Colors must be given");

        return new ColorsUpdatedEvent(colors);
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
