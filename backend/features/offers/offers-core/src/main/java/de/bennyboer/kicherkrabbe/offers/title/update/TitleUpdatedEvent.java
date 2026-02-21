package de.bennyboer.kicherkrabbe.offers.title.update;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.offers.OfferTitle;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class TitleUpdatedEvent implements Event {

    public static final EventName NAME = EventName.of("TITLE_UPDATED");

    public static final Version VERSION = Version.zero();

    OfferTitle title;

    public static TitleUpdatedEvent of(OfferTitle title) {
        notNull(title, "Title must be given");

        return new TitleUpdatedEvent(title);
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
