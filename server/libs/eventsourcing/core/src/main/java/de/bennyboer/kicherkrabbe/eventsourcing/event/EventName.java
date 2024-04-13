package de.bennyboer.kicherkrabbe.eventsourcing.event;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Locale;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class EventName {

    String value;

    public static EventName of(String name) {
        notNull(name, "Event name must be given");
        check(!name.isBlank(), "Event name must not be blank");

        name = name.trim()
                .replaceAll("[^a-zA-Z]", "_")
                .toUpperCase(Locale.ROOT);

        return new EventName(name);
    }

    @Override
    public String toString() {
        return String.format("EventName(%s)", value);
    }

}
