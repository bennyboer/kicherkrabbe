package de.bennyboer.kicherkrabbe.fabrics.create;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.fabrics.*;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class CreateCmd implements Command {

    FabricName name;

    FabricKind kind;

    @Nullable
    ImageId image;

    @Nullable
    List<ImageId> exampleImages;

    Set<ColorId> colors;

    Set<TopicId> topics;

    Set<FabricTypeAvailability> availability;

    public static CreateCmd of(
            FabricName name,
            FabricKind kind,
            @Nullable ImageId image,
            @Nullable List<ImageId> exampleImages,
            Set<ColorId> colors,
            Set<TopicId> topics,
            Set<FabricTypeAvailability> availability
    ) {
        notNull(name, "Fabric name must be given");
        notNull(kind, "Fabric kind must be given");
        notNull(colors, "Colors must be given");
        notNull(topics, "Topics must be given");
        notNull(availability, "Availability must be given");

        return new CreateCmd(name, kind, image, exampleImages, colors, topics, availability);
    }

    public Optional<ImageId> getImage() {
        return Optional.ofNullable(image);
    }

    public List<ImageId> getExampleImages() {
        return Optional.ofNullable(exampleImages).orElseGet(List::of);
    }

}
