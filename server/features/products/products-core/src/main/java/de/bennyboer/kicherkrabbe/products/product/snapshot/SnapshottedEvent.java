package de.bennyboer.kicherkrabbe.products.product.snapshot;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.products.product.*;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class SnapshottedEvent implements Event {

    public static final EventName NAME = EventName.of("SNAPSHOTTED");

    public static final Version VERSION = Version.zero();

    ProductNumber number;

    List<ImageId> images;

    Links links;

    FabricComposition fabricComposition;

    Notes notes;

    Instant producedAt;

    Instant createdAt;

    @Nullable
    Instant deletedAt;

    public static SnapshottedEvent of(
            ProductNumber number,
            List<ImageId> images,
            Links links,
            FabricComposition fabricComposition,
            Notes notes,
            Instant producedAt,
            Instant createdAt,
            @Nullable Instant deletedAt
    ) {
        notNull(number, "Number must be given");
        notNull(images, "Images must be given");
        notNull(links, "Links must be given");
        notNull(fabricComposition, "Fabric composition must be given");
        notNull(notes, "Notes must be given");
        notNull(producedAt, "Produced at must be given");
        notNull(createdAt, "Created at must be given");

        return new SnapshottedEvent(
                number,
                images,
                links,
                fabricComposition,
                notes,
                producedAt,
                createdAt,
                deletedAt
        );
    }

    @Override
    public EventName getEventName() {
        return NAME;
    }

    @Override
    public Version getVersion() {
        return VERSION;
    }

    @Override
    public boolean isSnapshot() {
        return true;
    }

    public Optional<Instant> getDeletedAt() {
        return Optional.ofNullable(deletedAt);
    }

}
