package de.bennyboer.kicherkrabbe.highlights;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static java.util.UUID.randomUUID;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class HighlightId {

    String value;

    public static HighlightId of(String value) {
        notNull(value, "Highlight ID must be given");
        check(!value.isBlank(), "Highlight ID must not be blank");

        return new HighlightId(value);
    }

    public static HighlightId create() {
        return new HighlightId(randomUUID().toString());
    }

    @Override
    public String toString() {
        return "HighlightId(%s)".formatted(value);
    }

}
