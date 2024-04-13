package de.bennyboer.kicherkrabbe.eventsourcing.aggregate;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class AggregateId {

    String value;

    public static AggregateId of(String value) {
        notNull(value, "AggregateId must be given");
        check(!value.isBlank(), "AggregateId must not be blank");

        return new AggregateId(value);
    }

    @Override
    public String toString() {
        return String.format("AggregateId(%s)", value);
    }

}
