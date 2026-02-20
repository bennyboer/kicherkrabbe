package de.bennyboer.kicherkrabbe.offers;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class LinkName {

    String value;

    public static LinkName of(String value) {
        notNull(value, "Link name must be given");
        check(!value.isBlank(), "Link name must not be blank");

        return new LinkName(value);
    }

    @Override
    public String toString() {
        return "LinkName(%s)".formatted(value);
    }

}
