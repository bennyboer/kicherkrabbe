package de.bennyboer.kicherkrabbe.categories;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static java.util.UUID.randomUUID;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class CategoryId {

    String value;

    public static CategoryId of(String value) {
        notNull(value, "Category ID must be given");
        check(!value.isBlank(), "Category ID must not be blank");

        return new CategoryId(value);
    }

    public static CategoryId create() {
        return new CategoryId(randomUUID().toString());
    }

    @Override
    public String toString() {
        return "CategoryId(%s)".formatted(value);
    }

}
