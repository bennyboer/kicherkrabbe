package de.bennyboer.kicherkrabbe.offers;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class Note {

    String value;

    public static Note of(String value) {
        notNull(value, "Note must be given");
        check(!value.isBlank(), "Note must not be blank");

        return new Note(value);
    }

    @Override
    public String toString() {
        return "Note(%s)".formatted(value);
    }

}
