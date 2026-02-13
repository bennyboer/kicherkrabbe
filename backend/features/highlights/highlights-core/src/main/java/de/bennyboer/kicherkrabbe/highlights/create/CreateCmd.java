package de.bennyboer.kicherkrabbe.highlights.create;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.highlights.ImageId;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class CreateCmd implements Command {

    ImageId imageId;

    long sortOrder;

    public static CreateCmd of(ImageId imageId, long sortOrder) {
        notNull(imageId, "Image ID must be given");

        return new CreateCmd(imageId, sortOrder);
    }

}
