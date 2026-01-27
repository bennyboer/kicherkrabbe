package de.bennyboer.kicherkrabbe.notifications.notification;

import lombok.Getter;

@Getter
public class SystemNotificationsDisabledException extends RuntimeException {

    public SystemNotificationsDisabledException() {
        super("System notifications are disabled");
    }

}
