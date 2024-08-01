package de.bennyboer.kicherkrabbe.categories.regroup;

import de.bennyboer.kicherkrabbe.categories.CategoryGroup;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class RegroupedEvent implements Event {

    public static final EventName NAME = EventName.of("REGROUPED");

    public static final Version VERSION = Version.zero();

    CategoryGroup group;

    public static RegroupedEvent of(CategoryGroup group) {
        notNull(group, "Category group must be given");

        return new RegroupedEvent(group);
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
