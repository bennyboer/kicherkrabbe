package de.bennyboer.kicherkrabbe.notifications.notification;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static de.bennyboer.kicherkrabbe.notifications.notification.TargetType.SYSTEM;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class Target {

    TargetType type;

    TargetId id;

    public static Target of(TargetType type, TargetId id) {
        notNull(type, "Target type must be given");
        notNull(id, "Target ID must be given");

        return new Target(type, id);
    }

    public static Target system() {
        return of(SYSTEM, TargetId.system());
    }

    @Override
    public String toString() {
        return "Target(type=%s, id=%s)".formatted(type, id);
    }

}
