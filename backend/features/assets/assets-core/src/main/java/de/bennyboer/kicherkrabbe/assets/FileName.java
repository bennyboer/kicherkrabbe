package de.bennyboer.kicherkrabbe.assets;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class FileName {

    String value;

    public static FileName of(String value) {
        notNull(value, "File name must be given");
        check(!value.isBlank(), "File name must not be empty");

        return new FileName(value);
    }

    @Override
    public String toString() {
        return "FileName(%s)".formatted(value);
    }

}
