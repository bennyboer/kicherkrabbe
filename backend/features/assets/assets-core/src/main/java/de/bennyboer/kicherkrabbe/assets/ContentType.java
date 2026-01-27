package de.bennyboer.kicherkrabbe.assets;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class ContentType {

    String value;

    public static ContentType of(String value) {
        notNull(value, "Content type must be given");
        check(!value.isBlank(), "Content type must not be empty");

        return new ContentType(value);
    }

    @Override
    public String toString() {
        return "ContentType(%s)".formatted(value);
    }

}
