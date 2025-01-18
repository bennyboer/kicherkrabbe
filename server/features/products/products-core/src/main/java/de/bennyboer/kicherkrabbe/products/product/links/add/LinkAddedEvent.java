package de.bennyboer.kicherkrabbe.products.product.links.add;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.products.product.Link;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class LinkAddedEvent implements Event {

    public static final EventName NAME = EventName.of("LINK_ADDED");

    public static final Version VERSION = Version.zero();

    Link link;

    public static LinkAddedEvent of(Link link) {
        notNull(link, "Link must be given");

        return new LinkAddedEvent(link);
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
