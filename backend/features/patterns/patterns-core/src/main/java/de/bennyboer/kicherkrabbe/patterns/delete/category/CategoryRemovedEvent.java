package de.bennyboer.kicherkrabbe.patterns.delete.category;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.patterns.PatternCategoryId;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class CategoryRemovedEvent implements Event {

    public static final EventName NAME = EventName.of("CATEGORY_REMOVED");

    public static final Version VERSION = Version.zero();

    PatternCategoryId categoryId;

    public static CategoryRemovedEvent of(PatternCategoryId categoryId) {
        notNull(categoryId, "Removed Category ID must be given");

        return new CategoryRemovedEvent(categoryId);
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
