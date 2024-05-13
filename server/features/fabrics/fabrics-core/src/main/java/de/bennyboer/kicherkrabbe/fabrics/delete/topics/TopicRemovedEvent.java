package de.bennyboer.kicherkrabbe.fabrics.delete.topics;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.fabrics.TopicId;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class TopicRemovedEvent implements Event {

    public static final EventName NAME = EventName.of("TOPIC_REMOVED");

    public static final Version VERSION = Version.zero();

    TopicId topicId;

    public static TopicRemovedEvent of(TopicId topicId) {
        notNull(topicId, "Topic ID must be given");

        return new TopicRemovedEvent(topicId);
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
