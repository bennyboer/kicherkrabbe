package de.bennyboer.kicherkrabbe.inquiries;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class SenderName {

    String value;

    public static SenderName of(String value) {
        notNull(value, "Sender name must be given");
        check(!value.isBlank(), "Sender name must not be blank");
        check(value.length() <= 200, "Sender name must not exceed 200 characters");

        return new SenderName(value);
    }

    @Override
    public String toString() {
        return "SenderName(%s)".formatted(value);
    }

}
