package de.bennyboer.kicherkrabbe.embroideries.create;

import de.bennyboer.kicherkrabbe.embroideries.EmbroideryCategoryId;
import de.bennyboer.kicherkrabbe.embroideries.EmbroideryName;
import de.bennyboer.kicherkrabbe.embroideries.ImageId;
import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Set;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class CreateCmd implements Command {

    EmbroideryName name;

    ImageId image;

    Set<EmbroideryCategoryId> categories;

    public static CreateCmd of(EmbroideryName name, ImageId image, Set<EmbroideryCategoryId> categories) {
        notNull(name, "Embroidery name must be given");
        notNull(image, "Image must be given");
        notNull(categories, "Categories must be given");

        return new CreateCmd(name, image, categories);
    }

}
