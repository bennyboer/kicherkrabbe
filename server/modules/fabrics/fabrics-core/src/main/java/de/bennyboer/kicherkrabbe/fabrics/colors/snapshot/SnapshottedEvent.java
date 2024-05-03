package de.bennyboer.kicherkrabbe.fabrics.colors.snapshot;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.fabrics.colors.ColorName;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;
import java.util.Optional;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class SnapshottedEvent implements Event {

    public static final EventName NAME = EventName.of("SNAPSHOTTED");

    public static final Version VERSION = Version.zero();

    ColorName name;

    int red;

    int green;

    int blue;

    Instant createdAt;

    @Nullable
    Instant deletedAt;

    public static SnapshottedEvent of(
            ColorName name,
            int red,
            int green,
            int blue,
            Instant createdAt,
            @Nullable Instant deletedAt
    ) {
        notNull(name, "Color name must be given");
        notNull(createdAt, "Created at must be given");
        check(red >= 0 && red <= 255, "Red must be between 0 and 255");
        check(green >= 0 && green <= 255, "Green must be between 0 and 255");
        check(blue >= 0 && blue <= 255, "Blue must be between 0 and 255");

        return new SnapshottedEvent(name, red, green, blue, createdAt, deletedAt);
    }

    @Override
    public EventName getEventName() {
        return NAME;
    }

    @Override
    public Version getVersion() {
        return VERSION;
    }

    public Optional<Instant> getDeletedAt() {
        return Optional.ofNullable(deletedAt);
    }

}
