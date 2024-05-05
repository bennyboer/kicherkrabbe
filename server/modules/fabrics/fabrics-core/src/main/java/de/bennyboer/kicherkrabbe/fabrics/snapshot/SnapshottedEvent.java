package de.bennyboer.kicherkrabbe.fabrics.snapshot;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.fabrics.*;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class SnapshottedEvent implements Event {

    public static final EventName NAME = EventName.of("SNAPSHOTTED");

    public static final Version VERSION = Version.zero();

    FabricName name;

    ImageId image;

    Set<ColorId> colors;

    Set<TopicId> topics;

    Set<FabricTypeAvailability> availability;

    boolean published;

    Instant createdAt;

    @Nullable
    Instant deletedAt;

    public static SnapshottedEvent of(
            FabricName name,
            ImageId image,
            Set<ColorId> colors,
            Set<TopicId> topics,
            Set<FabricTypeAvailability> availability,
            boolean published,
            Instant createdAt,
            @Nullable Instant deletedAt
    ) {
        notNull(name, "Fabric name must be given");
        notNull(image, "Image must be given");
        notNull(colors, "Colors must be given");
        notNull(topics, "Topics must be given");
        notNull(availability, "Availability must be given");
        notNull(createdAt, "Created at must be given");

        return new SnapshottedEvent(name, image, colors, topics, availability, published, createdAt, deletedAt);
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
