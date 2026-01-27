package de.bennyboer.kicherkrabbe.mailbox.mail;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@With(PRIVATE)
@AllArgsConstructor(access = PRIVATE)
public class Content {

    String value;

    public static Content of(String value) {
        notNull(value, "Content must be given");
        check(!value.isBlank(), "Content must not be blank");

        return new Content(value);
    }

    @Override
    public String toString() {
        return "Content(%s)".formatted(value);
    }

    public Content anonymize() {
        return withValue("ANONYMIZED");
    }

}
