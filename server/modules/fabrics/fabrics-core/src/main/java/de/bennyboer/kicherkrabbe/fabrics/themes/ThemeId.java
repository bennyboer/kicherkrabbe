package de.bennyboer.kicherkrabbe.fabrics.themes;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static java.util.UUID.randomUUID;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class ThemeId {

    String value;

    public static ThemeId of(String value) {
        notNull(value, "Theme ID must be given");
        check(!value.isBlank(), "Theme ID must not be blank");

        return new ThemeId(value);
    }

    public static ThemeId create() {
        return new ThemeId(randomUUID().toString());
    }

    @Override
    public String toString() {
        return "ThemeId(%s)".formatted(value);
    }

}
