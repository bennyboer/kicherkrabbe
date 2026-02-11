package de.bennyboer.kicherkrabbe.highlights.image;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.highlights.ImageId;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class UpdateImageCmd implements Command {

    ImageId imageId;

    public static UpdateImageCmd of(ImageId imageId) {
        notNull(imageId, "Image ID must be given");

        return new UpdateImageCmd(imageId);
    }

}
