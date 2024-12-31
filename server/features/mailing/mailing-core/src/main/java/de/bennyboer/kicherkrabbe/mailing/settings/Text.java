package de.bennyboer.kicherkrabbe.mailing.settings;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class Text {

    String value;

    public static Text of(String value) {
        notNull(value, "Text must be given");
        check(!value.isBlank(), "Text must not be blank");

        return new Text(value);
    }

    @Override
    public String toString() {
        return "Text(%s)".formatted(value);
    }

}
