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
public class Subject {

    String value;

    public static Subject of(String value) {
        notNull(value, "Subject must be given");
        check(!value.isBlank(), "Subject must not be blank");

        return new Subject(value);
    }

    @Override
    public String toString() {
        return "Subject(%s)".formatted(value);
    }

    public Subject anonymize() {
        return withValue("ANONYMIZED");
    }
    
}
