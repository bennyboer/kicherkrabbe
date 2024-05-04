package de.bennyboer.kicherkrabbe.fabrics.aggregate.update.themes;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.fabrics.themes.ThemeId;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Set;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class ThemesUpdatedEvent implements Event {

    public static final EventName NAME = EventName.of("THEMES_UPDATED");

    public static final Version VERSION = Version.zero();

    Set<ThemeId> themes;

    public static ThemesUpdatedEvent of(Set<ThemeId> themes) {
        notNull(themes, "Themes must be given");

        return new ThemesUpdatedEvent(themes);
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
