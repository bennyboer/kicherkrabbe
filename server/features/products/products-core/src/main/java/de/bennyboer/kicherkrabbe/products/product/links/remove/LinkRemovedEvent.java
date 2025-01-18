package de.bennyboer.kicherkrabbe.products.product.links.remove;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.products.product.LinkId;
import de.bennyboer.kicherkrabbe.products.product.LinkType;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class LinkRemovedEvent implements Event {

    public static final EventName NAME = EventName.of("LINK_REMOVED");

    public static final Version VERSION = Version.zero();

    LinkType linkType;

    LinkId linkId;

    public static LinkRemovedEvent of(LinkType linkType, LinkId linkId) {
        notNull(linkType, "Link type must be given");
        notNull(linkId, "Link ID must be given");

        return new LinkRemovedEvent(linkType, linkId);
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
