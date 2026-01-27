package de.bennyboer.kicherkrabbe.eventsourcing.example.events;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import jakarta.annotation.Nullable;
import lombok.Value;

import java.time.Instant;
import java.util.Optional;

import static de.bennyboer.kicherkrabbe.eventsourcing.example.events.CreatedEvent.NAME;

@Value
public class CreatedEvent2 implements Event {

    public static final Version VERSION = Version.of(1);

    String title;

    String description;

    /**
     * Note that it does not really make sense to include this field in the command and created event.
     * It is just here to demonstrate a patch.
     */
    @Nullable
    Instant deletedAt;

    private CreatedEvent2(String title, String description, Instant deletedAt) {
        this.title = title;
        this.description = description;
        this.deletedAt = deletedAt;
    }

    public static CreatedEvent2 of(String title, String description, @Nullable Instant deletedAt) {
        return new CreatedEvent2(title, description, deletedAt);
    }

    public static CreatedEvent2 from(CreatedEvent e) {
        return new CreatedEvent2(e.getTitle(), e.getDescription(), null);
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
