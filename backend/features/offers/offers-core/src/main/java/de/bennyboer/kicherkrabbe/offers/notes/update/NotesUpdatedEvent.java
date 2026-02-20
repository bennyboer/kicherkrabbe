package de.bennyboer.kicherkrabbe.offers.notes.update;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.offers.Notes;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class NotesUpdatedEvent implements Event {

    public static final EventName NAME = EventName.of("NOTES_UPDATED");

    public static final Version VERSION = Version.zero();

    Notes notes;

    public static NotesUpdatedEvent of(Notes notes) {
        notNull(notes, "Notes must be given");

        return new NotesUpdatedEvent(notes);
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
