package de.bennyboer.kicherkrabbe.inquiries;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class Subject {

    String value;

    public static Subject of(String value) {
        notNull(value, "Subject must be given");
        check(!value.isBlank(), "Subject must not be blank");
        check(value.length() <= 200, "Subject must not exceed 200 characters");

        return new Subject(value);
    }

    public Subject anonymize() {
        return Subject.of("ANONYMIZED");
    }

    @Override
    public String toString() {
        return "Subject(%s)".formatted(value);
    }

}
