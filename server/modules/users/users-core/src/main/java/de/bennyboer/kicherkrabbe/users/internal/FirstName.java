package de.bennyboer.kicherkrabbe.users.internal;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@With(PRIVATE)
@AllArgsConstructor(access = PRIVATE)
public class FirstName {

    private static final String ANONYMIZED = "ANONYMIZED";

    String value;

    public static FirstName of(String value) {
        notNull(value, "First name must be given");
        check(!value.isBlank(), "First name must not be blank");

        return new FirstName(value);
    }

    public FirstName anonymize() {
        return withValue(ANONYMIZED);
    }

    @Override
    public String toString() {
        return "FirstName(%s)".formatted(value);
    }

}
