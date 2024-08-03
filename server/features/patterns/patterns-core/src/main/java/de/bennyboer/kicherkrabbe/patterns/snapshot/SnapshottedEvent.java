package de.bennyboer.kicherkrabbe.patterns.snapshot;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.patterns.*;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class SnapshottedEvent implements Event {

    public static final EventName NAME = EventName.of("SNAPSHOTTED");

    public static final Version VERSION = Version.zero();

    PatternName name;

    PatternAttribution attribution;

    List<ImageId> images;

    List<PatternVariant> variants;

    List<PatternExtra> extras;

    Instant createdAt;

    @Nullable
    Instant deletedAt;

    public static SnapshottedEvent of(
            PatternName name,
            PatternAttribution attribution,
            List<ImageId> images,
            List<PatternVariant> variants,
            List<PatternExtra> extras,
            Instant createdAt,
            @Nullable Instant deletedAt
    ) {
        notNull(name, "Pattern name must be given");
        notNull(attribution, "Attribution must be given");
        notNull(images, "Images must be given");
        notNull(variants, "Variants must be given");
        notNull(extras, "Extras must be given");
        notNull(createdAt, "Created at must be given");
        check(!images.isEmpty(), "Images must not be empty");
        check(!variants.isEmpty(), "Variants must not be empty");

        return new SnapshottedEvent(
                name,
                attribution,
                images,
                variants,
                extras,
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
