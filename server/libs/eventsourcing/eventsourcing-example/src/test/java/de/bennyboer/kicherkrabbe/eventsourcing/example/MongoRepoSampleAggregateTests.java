package de.bennyboer.kicherkrabbe.eventsourcing.example;

import de.bennyboer.kicherkrabbe.eventsourcing.EventSerializer;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.eventsourcing.example.events.*;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.mongo.MongoEvent;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.mongo.MongoEventSourcingRepo;
import de.bennyboer.kicherkrabbe.testing.persistence.MongoTest;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@MongoTest
public class MongoRepoSampleAggregateTests extends SampleAggregateTests {

    private final ReactiveMongoTemplate template;

    @Autowired
    public MongoRepoSampleAggregateTests(ReactiveMongoTemplate template) {
        super();

        this.template = template;
    }

    @BeforeEach
    public void clear() {
        template.remove(MongoEvent.class).inCollection("sample_events").all().block();
    }

    @Override
    protected EventSourcingRepo createRepo() {
        return new MongoEventSourcingRepo("sample_events", template, new EventSerializer() {

            @Override
            public Map<String, Object> serialize(Event event) {
                return switch (event) {
                    case CreatedEvent e -> Map.of(
                            "title", e.getTitle(),
                            "description", e.getDescription()
                    );
                    case CreatedEvent2 e -> {
                        Map<String, Object> result = new HashMap<>(Map.of(
                                "title", e.getTitle(),
                                "description", e.getDescription()
                        ));

                        e.getDeletedAt().ifPresent(deletedAt -> result.put("deletedAt", deletedAt.toString()));

                        yield result;
                    }
                    case DescriptionUpdatedEvent e -> Map.of(
                            "description", e.getDescription()
                    );
                    case TitleUpdatedEvent e -> Map.of(
                            "title", e.getTitle()
                    );
                    case SnapshottedEvent e -> {
                        Map<String, Object> result = new HashMap<>(Map.of(
                                "title", e.getTitle(),
                                "description", e.getDescription()
                        ));

                        e.getDeletedAt().ifPresent(deletedAt -> result.put("deletedAt", deletedAt.toString()));

                        yield result;
                    }
                    default -> Map.of();
                };
            }

            @Override
            public Event deserialize(EventName name, Version eventVersion, Map<String, Object> payload) {
                if (name.equals(CreatedEvent.NAME)) {
                    String title = (String) payload.get("title");
                    String description = (String) payload.get("description");

                    if (eventVersion.equals(CreatedEvent.VERSION)) {
                        return CreatedEvent.of(title, description);
                    } else {
                        Instant deletedAt = payload.containsKey("deletedAt")
                                ? Instant.parse((String) payload.get("deletedAt"))
                                : null;
                        return CreatedEvent2.of(title, description, deletedAt);
                    }
                } else if (name.equals(DescriptionUpdatedEvent.NAME)) {
                    String description = (String) payload.get("description");
                    return DescriptionUpdatedEvent.of(description);
                } else if (name.equals(TitleUpdatedEvent.NAME)) {
                    String title = (String) payload.get("title");
                    return TitleUpdatedEvent.of(title);
                } else if (name.equals(SnapshottedEvent.NAME)) {
                    String title = (String) payload.get("title");
                    String description = (String) payload.get("description");
                    Instant deletedAt = payload.containsKey("deletedAt")
                            ? Instant.parse((String) payload.get("deletedAt"))
                            : null;

                    return SnapshottedEvent.of(title, description, deletedAt);
                } else if (name.equals(DeletedEvent.NAME)) {
                    return DeletedEvent.of();
                }

                throw new IllegalArgumentException("Unknown event name " + name);
            }

        });
    }

}
