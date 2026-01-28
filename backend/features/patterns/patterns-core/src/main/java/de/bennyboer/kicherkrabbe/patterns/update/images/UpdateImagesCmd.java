package de.bennyboer.kicherkrabbe.patterns.update.images;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.patterns.ImageId;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class UpdateImagesCmd implements Command {

    List<ImageId> images;

    public static UpdateImagesCmd of(List<ImageId> images) {
        notNull(images, "Images must be given");
        check(!images.isEmpty(), "Images must not be empty");

        return new UpdateImagesCmd(images);
    }

}
