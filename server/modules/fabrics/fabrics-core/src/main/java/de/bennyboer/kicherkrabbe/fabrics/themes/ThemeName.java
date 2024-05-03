package de.bennyboer.kicherkrabbe.fabrics.themes;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class ThemeName {

    String value;

    public static ThemeName of(String value) {
        notNull(value, "Theme name must be given");
        check(!value.isBlank(), "Theme name must not be blank");

        return new ThemeName(value);
    }

    @Override
    public String toString() {
        return "ThemeName(%s)".formatted(value);
    }

}
