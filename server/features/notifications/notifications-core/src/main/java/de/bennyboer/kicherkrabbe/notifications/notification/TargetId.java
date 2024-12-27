package de.bennyboer.kicherkrabbe.notifications.notification;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class TargetId {

    private static final TargetId SYSTEM = new TargetId("SYSTEM");

    String value;

    public static TargetId of(String value) {
        notNull(value, "Target ID must be given");
        check(!value.isBlank(), "Target ID must not be blank");

        return new TargetId(value);
    }

    public static TargetId system() {
        return SYSTEM;
    }

    @Override
    public String toString() {
        return "TargetId(%s)".formatted(value);
    }

}
