package de.bennyboer.kicherkrabbe.patterns.update.variants;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.patterns.PatternVariant;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class VariantsUpdatedEvent implements Event {

    public static final EventName NAME = EventName.of("VARIANTS_UPDATED");

    public static final Version VERSION = Version.zero();

    List<PatternVariant> variants;

    public static VariantsUpdatedEvent of(List<PatternVariant> variants) {
        notNull(variants, "Variants must be given");
        check(!variants.isEmpty(), "Variants must not be empty");

        return new VariantsUpdatedEvent(variants);
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
