package de.bennyboer.kicherkrabbe.fabrics.update.images;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.fabrics.ImageId;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;
import java.util.Optional;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class UpdateImagesCmd implements Command {

    @Nullable
    ImageId image;

    List<ImageId> exampleImages;

    public static UpdateImagesCmd of(@Nullable ImageId image, List<ImageId> exampleImages) {
        notNull(exampleImages, "Example images must be given");

        return new UpdateImagesCmd(image, exampleImages);
    }

    public Optional<ImageId> getImage() {
        return Optional.ofNullable(image);
    }

}
