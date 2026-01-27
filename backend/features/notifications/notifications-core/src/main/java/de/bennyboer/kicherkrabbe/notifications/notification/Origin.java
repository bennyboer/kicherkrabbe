package de.bennyboer.kicherkrabbe.notifications.notification;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class Origin {

    OriginType type;

    OriginId id;

    public static Origin of(OriginType type, OriginId id) {
        notNull(type, "Origin type must be given");
        notNull(id, "Origin ID must be given");

        return new Origin(type, id);
    }

    @Override
    public String toString() {
        return "Origin(type=%s, id=%s)".formatted(type, id);
    }

}
