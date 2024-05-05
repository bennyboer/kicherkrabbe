package de.bennyboer.kicherkrabbe.fabrics.update.image;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.fabrics.ImageId;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class UpdateImageCmd implements Command {

    ImageId image;

    public static UpdateImageCmd of(ImageId image) {
        notNull(image, "Image must be given");

        return new UpdateImageCmd(image);
    }

}
