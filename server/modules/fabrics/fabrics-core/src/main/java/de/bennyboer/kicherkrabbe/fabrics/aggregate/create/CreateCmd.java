package de.bennyboer.kicherkrabbe.fabrics.aggregate.create;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.fabrics.aggregate.FabricName;
import de.bennyboer.kicherkrabbe.fabrics.aggregate.FabricTypeAvailability;
import de.bennyboer.kicherkrabbe.fabrics.aggregate.ImageId;
import de.bennyboer.kicherkrabbe.fabrics.colors.ColorId;
import de.bennyboer.kicherkrabbe.fabrics.themes.ThemeId;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Set;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class CreateCmd implements Command {

    FabricName name;

    ImageId image;

    Set<ColorId> colors;

    Set<ThemeId> themes;

    Set<FabricTypeAvailability> availability;

    public static CreateCmd of(
            FabricName name,
            ImageId image,
            Set<ColorId> colors,
            Set<ThemeId> themes,
            Set<FabricTypeAvailability> availability
    ) {
        notNull(name, "Fabric name must be given");
        notNull(image, "Image must be given");
        notNull(colors, "Colors must be given");
        notNull(themes, "Themes must be given");
        notNull(availability, "Availability must be given");

        return new CreateCmd(name, image, colors, themes, availability);
    }

}
