package de.bennyboer.kicherkrabbe.patterns.update.attribution;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.patterns.PatternAttribution;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class AttributionUpdatedEvent implements Event {

    public static final EventName NAME = EventName.of("ATTRIBUTION_UPDATED");

    public static final Version VERSION = Version.zero();

    PatternAttribution attribution;

    public static AttributionUpdatedEvent of(PatternAttribution attribution) {
        notNull(attribution, "Pattern attribution must be given");

        return new AttributionUpdatedEvent(attribution);
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
