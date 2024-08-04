package de.bennyboer.kicherkrabbe.categories.regroup;

import de.bennyboer.kicherkrabbe.categories.CategoryGroup;
import de.bennyboer.kicherkrabbe.categories.CategoryName;
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

    /*
    While it is not necessary to include the name of the category in the event payload,
    some dependencies may require it to be present. For example the patterns module holds a read model
    to categories of the group CLOTHING. If a category is regrouped from NONE to CLOTHING, the patterns module
    needs to know the name of the category to update its read model.
     */
    CategoryName name;

    CategoryGroup group;

    public static RegroupedEvent of(CategoryName name, CategoryGroup group) {
        notNull(name, "Category name must be given");
        notNull(group, "Category group must be given");

        return new RegroupedEvent(name, group);
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
