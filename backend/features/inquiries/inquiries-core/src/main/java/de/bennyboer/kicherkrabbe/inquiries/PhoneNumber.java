package de.bennyboer.kicherkrabbe.inquiries;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class PhoneNumber {

    String value;

    public static PhoneNumber of(String value) {
        notNull(value, "Phone number must be given");
        check(!value.isBlank(), "Phone number must not be blank");
        check(value.length() <= 30, "Phone number must not exceed 30 characters");
        check(value.matches("[0-9+ ]+"), "Phone number must only contain digits, '+' and spaces");

        return new PhoneNumber(value);
    }

    @Override
    public String toString() {
        return "PhoneNumber(%s)".formatted(value);
    }

}
