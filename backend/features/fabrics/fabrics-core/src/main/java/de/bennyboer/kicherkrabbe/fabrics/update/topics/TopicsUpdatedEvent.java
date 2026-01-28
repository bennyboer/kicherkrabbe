package de.bennyboer.kicherkrabbe.fabrics.update.topics;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.fabrics.TopicId;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Set;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class TopicsUpdatedEvent implements Event {

    public static final EventName NAME = EventName.of("TOPICS_UPDATED");

    public static final Version VERSION = Version.zero();

    Set<TopicId> topics;

    public static TopicsUpdatedEvent of(Set<TopicId> topics) {
        notNull(topics, "Topics must be given");

        return new TopicsUpdatedEvent(topics);
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
