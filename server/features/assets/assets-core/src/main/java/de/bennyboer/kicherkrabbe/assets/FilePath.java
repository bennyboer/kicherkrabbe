package de.bennyboer.kicherkrabbe.assets;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class FilePath {

    String value;

    public static FilePath of(String value) {
        notNull(value, "File path must be given");
        check(!value.isBlank(), "File path must not be empty");

        return new FilePath(value);
    }

    @Override
    public String toString() {
        return "FilePath(%s)".formatted(value);
    }

}
