package de.bennyboer.kicherkrabbe.notifications.settings;

import lombok.Getter;

@Getter
public class SystemNotificationsAlreadyDisabledException extends RuntimeException {

    public SystemNotificationsAlreadyDisabledException() {
        super("System notifications are already disabled");
    }

}
