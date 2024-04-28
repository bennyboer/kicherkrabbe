package de.bennyboer.kicherkrabbe.permissions;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class HolderId {

    String value;

    public static HolderId of(String value) {
        notNull(value, "Holder ID must be given");
        check(!value.isBlank(), "Holder ID must not be blank");

        return new HolderId(value);
    }

    @Override
    public String toString() {
        return String.format("HolderId(%s)", value);
    }

}
