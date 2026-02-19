package de.bennyboer.kicherkrabbe.fabrics.update.images;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.fabrics.ImageId;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class UpdateImagesCmd implements Command {

    ImageId image;

    List<ImageId> exampleImages;

    public static UpdateImagesCmd of(ImageId image, List<ImageId> exampleImages) {
        notNull(image, "Image must be given");
        notNull(exampleImages, "Example images must be given");

        return new UpdateImagesCmd(image, exampleImages);
    }

}
