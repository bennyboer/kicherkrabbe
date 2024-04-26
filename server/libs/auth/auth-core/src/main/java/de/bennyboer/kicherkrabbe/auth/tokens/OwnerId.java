package de.bennyboer.kicherkrabbe.auth.tokens;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class OwnerId {

    String value;

    public static OwnerId of(String value) {
        notNull(value, "Owner id must be given");
        check(!value.isBlank(), "Owner id must not be blank");

        return new OwnerId(value);
    }

}
