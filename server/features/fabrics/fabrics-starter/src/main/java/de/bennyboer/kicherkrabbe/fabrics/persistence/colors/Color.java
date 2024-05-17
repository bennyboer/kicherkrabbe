package de.bennyboer.kicherkrabbe.fabrics.persistence.colors;

import de.bennyboer.kicherkrabbe.fabrics.ColorId;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class Color {

    ColorId id;

    ColorName name;

    int red;

    int green;

    int blue;

    public static Color of(ColorId id, ColorName name, int red, int green, int blue) {
        notNull(id, "Color ID must be given");
        notNull(name, "Color name must be given");
        check(red >= 0 && red <= 255, "Red must be between 0 and 255");
        check(green >= 0 && green <= 255, "Green must be between 0 and 255");
        check(blue >= 0 && blue <= 255, "Blue must be between 0 and 255");

        return new Color(id, name, red, green, blue);
    }

}
