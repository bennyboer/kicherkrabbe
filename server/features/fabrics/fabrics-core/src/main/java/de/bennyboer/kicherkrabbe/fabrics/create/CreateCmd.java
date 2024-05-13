package de.bennyboer.kicherkrabbe.fabrics.create;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.fabrics.*;
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

    Set<TopicId> topics;

    Set<FabricTypeAvailability> availability;

    public static CreateCmd of(
            FabricName name,
            ImageId image,
            Set<ColorId> colors,
            Set<TopicId> topics,
            Set<FabricTypeAvailability> availability
    ) {
        notNull(name, "Fabric name must be given");
        notNull(image, "Image must be given");
        notNull(colors, "Colors must be given");
        notNull(topics, "Topics must be given");
        notNull(availability, "Availability must be given");

        return new CreateCmd(name, image, colors, topics, availability);
    }

}
