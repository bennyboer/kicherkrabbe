package de.bennyboer.kicherkrabbe.notifications.settings;

import lombok.Getter;

@Getter
public class SystemNotificationsAlreadyEnabledException extends RuntimeException {

    public SystemNotificationsAlreadyEnabledException() {
        super("System notifications are already enabled");
    }

}
