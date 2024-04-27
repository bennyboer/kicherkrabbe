package de.bennyboer.kicherkrabbe.users.internal;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@With(PRIVATE)
@AllArgsConstructor(access = PRIVATE)
public class FullName {

    FirstName firstName;

    LastName lastName;

    public static FullName of(FirstName firstName, LastName lastName) {
        notNull(firstName, "First name must be given");
        notNull(lastName, "Last name must be given");

        return new FullName(firstName, lastName);
    }

    @Override
    public String toString() {
        return "FullName(%s, %s)".formatted(firstName.getValue(), lastName.getValue());
    }

    public FullName anonymize() {
        return withFirstName(firstName.anonymize())
                .withLastName(lastName.anonymize());
    }

}
