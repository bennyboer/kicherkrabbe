package de.bennyboer.kicherkrabbe.notifications.notification;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static java.util.UUID.randomUUID;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class NotificationId {

    String value;

    public static NotificationId of(String value) {
        notNull(value, "Notification ID must be given");
        check(!value.isBlank(), "Notification ID must not be blank");

        return new NotificationId(value);
    }

    public static NotificationId create() {
        return new NotificationId(randomUUID().toString());
    }

    @Override
    public String toString() {
        return "NotificationId(%s)".formatted(value);
    }

}
