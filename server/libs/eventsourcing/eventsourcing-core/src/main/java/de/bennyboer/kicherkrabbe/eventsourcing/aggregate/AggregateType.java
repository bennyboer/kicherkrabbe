package de.bennyboer.kicherkrabbe.eventsourcing.aggregate;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Locale;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class AggregateType {

    String value;

    public static AggregateType of(String value) {
        notNull(value, "AggregateType must be given");
        check(!value.isBlank(), "AggregateType must not be blank");

        value = value.trim()
                .replaceAll("[^a-zA-Z]", "_")
                .toUpperCase(Locale.ROOT);

        return new AggregateType(value);
    }

    @Override
    public String toString() {
        return String.format("AggregateType(%s)", value);
    }

}
