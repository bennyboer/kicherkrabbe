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
public class SenderName {

    String value;

    public static SenderName of(String value) {
        notNull(value, "Sender name must be given");
        check(!value.isBlank(), "Sender name must not be blank");

        return new SenderName(value);
    }

    public SenderName anonymize() {
        return withValue("ANONYMIZED");
    }

    @Override
    public String toString() {
        return "SenderName(%s)".formatted(value);
    }

}
