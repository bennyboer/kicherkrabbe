package de.bennyboer.kicherkrabbe.products.product;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class LinkId {

    String value;

    public static LinkId of(String value) {
        notNull(value, "Link ID must be given");
        check(!value.isBlank(), "Link ID must not be blank");

        return new LinkId(value);
    }

    @Override
    public String toString() {
        return "LinkId(%s)".formatted(value);
    }

}
