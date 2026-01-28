package de.bennyboer.kicherkrabbe.users;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@With(PRIVATE)
@AllArgsConstructor(access = PRIVATE)
public class LastName {

    private static final String ANONYMIZED = "ANONYMIZED";

    String value;

    public static LastName of(String value) {
        notNull(value, "Last name must be given");
        check(!value.isBlank(), "Last name must not be blank");

        return new LastName(value);
    }

    public LastName anonymize() {
        return withValue(ANONYMIZED);
    }

    @Override
    public String toString() {
        return "LastName(%s)".formatted(value);
    }

}
