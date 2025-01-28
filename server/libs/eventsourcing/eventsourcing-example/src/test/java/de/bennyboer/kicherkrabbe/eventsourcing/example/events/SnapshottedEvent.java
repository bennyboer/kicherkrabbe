package de.bennyboer.kicherkrabbe.eventsourcing.example.events;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import jakarta.annotation.Nullable;
import lombok.Value;

import java.time.Instant;
import java.util.Optional;

@Value
public class SnapshottedEvent implements Event {

    public static final EventName NAME = EventName.of("SNAPSHOTTED");

    public static final Version VERSION = Version.zero();

    String title;

    String description;

    Instant createdAt;

    @Nullable
    Instant deletedAt;

    private SnapshottedEvent(String title, String description, Instant createdAt, @Nullable Instant deletedAt) {
        this.title = title;
        this.description = description;
        this.createdAt = createdAt;
        this.deletedAt = deletedAt;
    }

    public static SnapshottedEvent of(String title, String description, Instant createdAt, @Nullable Instant deletedAt) {
        return new SnapshottedEvent(title, description, createdAt, deletedAt);
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
