package de.bennyboer.kicherkrabbe.patterns.update.description;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.patterns.PatternDescription;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Optional;

import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class DescriptionUpdatedEvent implements Event {

    public static final EventName NAME = EventName.of("DESCRIPTION_UPDATED");

    public static final Version VERSION = Version.zero();

    @Nullable
    PatternDescription description;

    public static DescriptionUpdatedEvent of(@Nullable PatternDescription description) {
        return new DescriptionUpdatedEvent(description);
    }

    @Override
    public EventName getEventName() {
        return NAME;
    }

    @Override
    public Version getVersion() {
        return VERSION;
    }

    public Optional<PatternDescription> getDescription() {
        return Optional.ofNullable(description);
    }

}
