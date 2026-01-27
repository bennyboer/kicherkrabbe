package de.bennyboer.kicherkrabbe.notifications.settings.system.disable;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import lombok.AllArgsConstructor;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class SystemNotificationsDisabledEvent implements Event {

    public static final EventName NAME = EventName.of("SYSTEM_NOTIFICATIONS_DISABLED");

    public static final Version VERSION = Version.zero();

    public static SystemNotificationsDisabledEvent of() {
        return new SystemNotificationsDisabledEvent();
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
