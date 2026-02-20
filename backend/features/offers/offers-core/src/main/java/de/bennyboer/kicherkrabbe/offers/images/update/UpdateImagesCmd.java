package de.bennyboer.kicherkrabbe.offers.images.update;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.offers.ImageId;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class UpdateImagesCmd implements Command {

    List<ImageId> images;

    public static UpdateImagesCmd of(List<ImageId> images) {
        notNull(images, "Images must be given");

        return new UpdateImagesCmd(images);
    }

}
