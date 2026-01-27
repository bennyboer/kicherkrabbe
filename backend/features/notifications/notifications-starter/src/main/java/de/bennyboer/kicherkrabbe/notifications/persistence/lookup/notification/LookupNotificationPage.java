package de.bennyboer.kicherkrabbe.notifications.persistence.lookup.notification;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class LookupNotificationPage {

    long total;

    List<LookupNotification> notifications;

    public static LookupNotificationPage of(long total, List<LookupNotification> notifications) {
        check(total >= 0, "total must be greater or equal to 0");
        notNull(notifications, "Notifications must be given");

        return new LookupNotificationPage(total, notifications);
    }

}
