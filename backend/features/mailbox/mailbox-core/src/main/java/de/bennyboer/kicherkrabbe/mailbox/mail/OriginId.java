package de.bennyboer.kicherkrabbe.mailbox.mail;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class OriginId {

    String value;

    public static OriginId of(String value) {
        notNull(value, "Origin ID must be given");
        check(!value.isBlank(), "Origin ID must not be blank");

        return new OriginId(value);
    }

    @Override
    public String toString() {
        return "OriginId(%s)".formatted(value);
    }

}
