package de.bennyboer.kicherkrabbe.colors.persistence.lookup;

import de.bennyboer.kicherkrabbe.colors.ColorId;
import de.bennyboer.kicherkrabbe.colors.ColorName;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class LookupColor {

    ColorId id;

    Version version;

    ColorName name;

    int red;

    int green;

    int blue;

    Instant createdAt;

    public static LookupColor of(
            ColorId id,
            Version version,
            ColorName name,
            int red,
            int green,
            int blue,
            Instant createdAt
    ) {
        notNull(id, "Color ID must be given");
        notNull(version, "Version must be given");
        notNull(name, "Color name must be given");
        notNull(createdAt, "Created at must be given");
        check(red >= 0 && red <= 255, "Red must be between 0 and 255");
        check(green >= 0 && green <= 255, "Green must be between 0 and 255");
        check(blue >= 0 && blue <= 255, "Blue must be between 0 and 255");

        return new LookupColor(id, version, name, red, green, blue, createdAt);
    }

}
