package de.bennyboer.kicherkrabbe.patterns.update.number;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.patterns.PatternNumber;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class NumberUpdatedEvent implements Event {

    public static final EventName NAME = EventName.of("NUMBER_UPDATED");

    public static final Version VERSION = Version.zero();

    @Nullable
    PatternNumber number;

    public static NumberUpdatedEvent of(PatternNumber number) {
        notNull(number, "Pattern number must be given");
        
        return new NumberUpdatedEvent(number);
    }

    @Override
    public EventName getEventName() {
        return NAME;
    }

    @Override
    public Version getVersion() {
        return VERSION;
    }

}
