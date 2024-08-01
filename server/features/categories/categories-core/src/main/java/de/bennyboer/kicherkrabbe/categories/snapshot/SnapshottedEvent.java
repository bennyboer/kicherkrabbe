package de.bennyboer.kicherkrabbe.categories.snapshot;

import de.bennyboer.kicherkrabbe.categories.CategoryGroup;
import de.bennyboer.kicherkrabbe.categories.CategoryName;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;
import java.util.Optional;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class SnapshottedEvent implements Event {

    public static final EventName NAME = EventName.of("SNAPSHOTTED");

    public static final Version VERSION = Version.zero();

    CategoryName name;

    CategoryGroup group;

    Instant createdAt;

    @Nullable
    Instant deletedAt;

    public static SnapshottedEvent of(
            CategoryName name,
            CategoryGroup group,
            Instant createdAt,
            @Nullable Instant deletedAt
    ) {
        notNull(name, "Category name must be given");
        notNull(createdAt, "Created at must be given");

        return new SnapshottedEvent(name, group, createdAt, deletedAt);
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
