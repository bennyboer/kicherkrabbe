package de.bennyboer.kicherkrabbe.highlights.persistence;

import de.bennyboer.kicherkrabbe.eventsourcing.EventSerializer;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.highlights.*;
import de.bennyboer.kicherkrabbe.highlights.create.CreatedEvent;
import de.bennyboer.kicherkrabbe.highlights.delete.DeletedEvent;
import de.bennyboer.kicherkrabbe.highlights.image.ImageUpdatedEvent;
import de.bennyboer.kicherkrabbe.highlights.links.add.LinkAddedEvent;
import de.bennyboer.kicherkrabbe.highlights.links.remove.LinkRemovedEvent;
import de.bennyboer.kicherkrabbe.highlights.links.update.LinkUpdatedEvent;
import de.bennyboer.kicherkrabbe.highlights.publish.PublishedEvent;
import de.bennyboer.kicherkrabbe.highlights.sort.SortOrderUpdatedEvent;
import de.bennyboer.kicherkrabbe.highlights.unpublish.UnpublishedEvent;

import java.util.Map;

public class HighlightEventPayloadSerializer implements EventSerializer {

    @Override
    public Map<String, Object> serialize(Event event) {
        return switch (event) {
            case CreatedEvent e -> Map.of(
                    "imageId", e.getImageId().getValue(),
                    "sortOrder", e.getSortOrder()
            );
            case ImageUpdatedEvent e -> Map.of(
                    "imageId", e.getImageId().getValue()
            );
            case LinkAddedEvent e -> Map.of(
                    "link", serializeLink(e.getLink())
            );
            case LinkUpdatedEvent e -> Map.of(
                    "link", serializeLink(e.getLink())
            );
            case LinkRemovedEvent e -> Map.of(
                    "linkType", e.getLinkType().name(),
                    "linkId", e.getLinkId().getValue()
            );
            case PublishedEvent ignored -> Map.of();
            case UnpublishedEvent ignored -> Map.of();
            case SortOrderUpdatedEvent e -> Map.of(
                    "sortOrder", e.getSortOrder()
            );
            case DeletedEvent ignored -> Map.of();
            default -> throw new IllegalStateException("Unexpected event: " + event);
        };
    }

    @Override
    public Event deserialize(EventName name, Version eventVersion, Map<String, Object> payload) {
        return switch (name.getValue()) {
            case "CREATED" -> CreatedEvent.of(
                    ImageId.of((String) payload.get("imageId")),
                    ((Number) payload.get("sortOrder")).longValue()
            );
            case "IMAGE_UPDATED" -> ImageUpdatedEvent.of(
                    ImageId.of((String) payload.get("imageId"))
            );
            case "LINK_ADDED" -> LinkAddedEvent.of(
                    deserializeLink((Map<String, Object>) payload.get("link"))
            );
            case "LINK_UPDATED" -> LinkUpdatedEvent.of(
                    deserializeLink((Map<String, Object>) payload.get("link"))
            );
            case "LINK_REMOVED" -> LinkRemovedEvent.of(
                    LinkType.valueOf((String) payload.get("linkType")),
                    LinkId.of((String) payload.get("linkId"))
            );
            case "PUBLISHED" -> PublishedEvent.of();
            case "UNPUBLISHED" -> UnpublishedEvent.of();
            case "SORT_ORDER_UPDATED" -> SortOrderUpdatedEvent.of(
                    ((Number) payload.get("sortOrder")).longValue()
            );
            case "DELETED" -> DeletedEvent.of();
            default -> throw new IllegalStateException("Unexpected event name: " + name);
        };
    }

    private Map<String, Object> serializeLink(Link link) {
        return Map.of(
                "type", link.getType().name(),
                "id", link.getId().getValue(),
                "name", link.getName().getValue()
        );
    }

    private Link deserializeLink(Map<String, Object> link) {
        return Link.of(
                LinkType.valueOf((String) link.get("type")),
                LinkId.of((String) link.get("id")),
                LinkName.of((String) link.get("name"))
        );
    }

}
