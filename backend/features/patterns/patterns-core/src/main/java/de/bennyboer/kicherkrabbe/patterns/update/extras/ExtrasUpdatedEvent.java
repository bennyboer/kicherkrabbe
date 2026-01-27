package de.bennyboer.kicherkrabbe.patterns.update.extras;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.patterns.PatternExtra;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class ExtrasUpdatedEvent implements Event {

    public static final EventName NAME = EventName.of("EXTRAS_UPDATED");

    public static final Version VERSION = Version.zero();

    List<PatternExtra> extras;

    public static ExtrasUpdatedEvent of(List<PatternExtra> extras) {
        notNull(extras, "Extras must be given");

        return new ExtrasUpdatedEvent(extras);
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
