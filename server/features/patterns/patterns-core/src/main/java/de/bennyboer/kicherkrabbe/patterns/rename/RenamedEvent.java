package de.bennyboer.kicherkrabbe.patterns.rename;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.patterns.PatternName;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class RenamedEvent implements Event {

    public static final EventName NAME = EventName.of("RENAMED");

    public static final Version VERSION = Version.zero();

    PatternName name;

    public static RenamedEvent of(PatternName name) {
        notNull(name, "Pattern name must be given");

        return new RenamedEvent(name);
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
