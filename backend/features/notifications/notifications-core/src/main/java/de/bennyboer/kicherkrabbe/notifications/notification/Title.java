package de.bennyboer.kicherkrabbe.notifications.notification;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@With(PRIVATE)
@AllArgsConstructor(access = PRIVATE)
public class Title {

    String value;

    public static Title of(String value) {
        notNull(value, "Title must be given");
        check(!value.isBlank(), "Title must not be blank");

        return new Title(value);
    }

    @Override
    public String toString() {
        return "Title(%s)".formatted(value);
    }

    public Title anonymize() {
        return withValue("ANONYMIZED");
    }
    
}
