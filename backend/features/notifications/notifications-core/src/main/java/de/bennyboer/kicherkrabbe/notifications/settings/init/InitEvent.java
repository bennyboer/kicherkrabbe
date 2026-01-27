package de.bennyboer.kicherkrabbe.notifications.settings.init;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.notifications.settings.SystemSettings;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class InitEvent implements Event {

    public static final EventName NAME = EventName.of("INITIALIZED");

    public static final Version VERSION = Version.zero();

    SystemSettings systemSettings;

    public static InitEvent of(SystemSettings systemSettings) {
        notNull(systemSettings, "System settings must be given");

        return new InitEvent(systemSettings);
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
