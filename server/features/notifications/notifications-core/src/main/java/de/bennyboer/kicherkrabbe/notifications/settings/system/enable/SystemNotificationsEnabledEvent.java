package de.bennyboer.kicherkrabbe.notifications.settings.system.enable;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import lombok.AllArgsConstructor;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class SystemNotificationsEnabledEvent implements Event {

    public static final EventName NAME = EventName.of("SYSTEM_NOTIFICATIONS_ENABLED");

    public static final Version VERSION = Version.zero();

    public static SystemNotificationsEnabledEvent of() {
        return new SystemNotificationsEnabledEvent();
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
