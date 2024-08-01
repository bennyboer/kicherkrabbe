package de.bennyboer.kicherkrabbe.categories;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class CategoryName {

    String value;

    public static CategoryName of(String value) {
        notNull(value, "Category name must be given");
        check(!value.isBlank(), "Category name must not be blank");

        return new CategoryName(value);
    }

    @Override
    public String toString() {
        return "CategoryName(%s)".formatted(value);
    }

}
