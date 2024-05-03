package de.bennyboer.kicherkrabbe.fabrics.themes.update;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.fabrics.themes.ThemeName;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class UpdatedEvent implements Event {

    public static final EventName NAME = EventName.of("UPDATED");

    public static final Version VERSION = Version.zero();

    ThemeName name;

    public static UpdatedEvent of(ThemeName name) {
        notNull(name, "Theme name must be given");

        return new UpdatedEvent(name);
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
