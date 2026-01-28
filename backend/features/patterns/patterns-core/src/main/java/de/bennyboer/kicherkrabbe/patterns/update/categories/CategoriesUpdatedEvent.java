package de.bennyboer.kicherkrabbe.patterns.update.categories;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.patterns.PatternCategoryId;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Set;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class CategoriesUpdatedEvent implements Event {

    public static final EventName NAME = EventName.of("CATEGORIES_UPDATED");

    public static final Version VERSION = Version.zero();

    Set<PatternCategoryId> categories;

    public static CategoriesUpdatedEvent of(Set<PatternCategoryId> categories) {
        notNull(categories, "Categories must be given");

        return new CategoriesUpdatedEvent(categories);
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
