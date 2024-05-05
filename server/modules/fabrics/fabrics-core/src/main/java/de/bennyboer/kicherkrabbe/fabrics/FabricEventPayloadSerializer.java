package de.bennyboer.kicherkrabbe.fabrics;

import de.bennyboer.kicherkrabbe.eventsourcing.EventSerializer;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.fabrics.create.CreatedEvent;
import de.bennyboer.kicherkrabbe.fabrics.delete.DeletedEvent;
import de.bennyboer.kicherkrabbe.fabrics.publish.PublishedEvent;
import de.bennyboer.kicherkrabbe.fabrics.rename.RenamedEvent;
import de.bennyboer.kicherkrabbe.fabrics.snapshot.SnapshottedEvent;
import de.bennyboer.kicherkrabbe.fabrics.unpublish.UnpublishedEvent;
import de.bennyboer.kicherkrabbe.fabrics.update.availability.AvailabilityUpdatedEvent;
import de.bennyboer.kicherkrabbe.fabrics.update.colors.ColorsUpdatedEvent;
import de.bennyboer.kicherkrabbe.fabrics.update.image.ImageUpdatedEvent;
import de.bennyboer.kicherkrabbe.fabrics.update.topics.TopicsUpdatedEvent;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FabricEventPayloadSerializer implements EventSerializer {

    @Override
    public Map<String, Object> serialize(Event event) {
        return switch (event) {
            case CreatedEvent e -> Map.of(
                    "name", e.getName().getValue(),
                    "image", e.getImage().getValue(),
                    "colors", e.getColors().stream().map(ColorId::getValue).toList(),
                    "topics", e.getTopics().stream().map(TopicId::getValue).toList(),
                    "availability", e.getAvailability().stream().map(a -> Map.of(
                            "typeId", a.getTypeId().getValue(),
                            "inStock", a.isInStock()
                    )).toList()
            );
            case SnapshottedEvent e -> {
                Map<String, Object> result = new HashMap<>(Map.of(
                        "name", e.getName().getValue(),
                        "image", e.getImage().getValue(),
                        "colors", e.getColors().stream().map(ColorId::getValue).toList(),
                        "topics", e.getTopics().stream().map(TopicId::getValue).toList(),
                        "availability", e.getAvailability().stream().map(a -> Map.of(
                                "typeId", a.getTypeId().getValue(),
                                "inStock", a.isInStock()
                        )).toList(),
                        "published", e.isPublished(),
                        "createdAt", e.getCreatedAt().toString()
                ));

                e.getDeletedAt().ifPresent(deletedAt -> result.put("deletedAt", deletedAt.toString()));

                yield result;
            }
            case RenamedEvent e -> Map.of("name", e.getName().getValue());
            case PublishedEvent ignored -> Map.of();
            case UnpublishedEvent ignored -> Map.of();
            case ImageUpdatedEvent e -> Map.of("image", e.getImage().getValue());
            case ColorsUpdatedEvent e -> Map.of("colors", e.getColors().stream().map(ColorId::getValue).toList());
            case TopicsUpdatedEvent e -> Map.of("topics", e.getTopics().stream().map(TopicId::getValue).toList());
            case AvailabilityUpdatedEvent e -> Map.of(
                    "availability", e.getAvailability().stream().map(a -> Map.of(
                            "typeId", a.getTypeId().getValue(),
                            "inStock", a.isInStock()
                    )).toList()
            );
            case DeletedEvent ignored -> Map.of();
            default -> throw new IllegalStateException("Unexpected event: " + event);
        };
    }

    @Override
    public Event deserialize(EventName name, Version eventVersion, Map<String, Object> payload) {
        return switch (name.getValue()) {
            case "CREATED" -> CreatedEvent.of(
                    FabricName.of((String) payload.get("name")),
                    ImageId.of((String) payload.get("image")),
                    ((List<String>) payload.get("colors")).stream().map(ColorId::of).collect(Collectors.toSet()),
                    ((List<String>) payload.get("topics")).stream().map(TopicId::of).collect(Collectors.toSet()),
                    ((List<Map<String, Object>>) payload.get("availability")).stream()
                            .map(a -> FabricTypeAvailability.of(
                                    FabricTypeId.of((String) a.get("typeId")),
                                    (boolean) a.get("inStock")
                            ))
                            .collect(Collectors.toSet())
            );
            case "SNAPSHOTTED" -> SnapshottedEvent.of(
                    FabricName.of((String) payload.get("name")),
                    ImageId.of((String) payload.get("image")),
                    ((List<String>) payload.get("colors")).stream().map(ColorId::of).collect(Collectors.toSet()),
                    ((List<String>) payload.get("topics")).stream().map(TopicId::of).collect(Collectors.toSet()),
                    ((List<Map<String, Object>>) payload.get("availability")).stream()
                            .map(a -> FabricTypeAvailability.of(
                                    FabricTypeId.of((String) a.get("typeId")),
                                    (boolean) a.get("inStock")
                            ))
                            .collect(Collectors.toSet()),
                    (boolean) payload.get("published"),
                    Instant.parse((String) payload.get("createdAt")),
                    payload.containsKey("deletedAt") ? Instant.parse((String) payload.get("deletedAt")) : null
            );
            case "RENAMED" -> RenamedEvent.of(FabricName.of((String) payload.get("name")));
            case "PUBLISHED" -> PublishedEvent.of();
            case "UNPUBLISHED" -> UnpublishedEvent.of();
            case "IMAGE_UPDATED" -> ImageUpdatedEvent.of(ImageId.of((String) payload.get("image")));
            case "COLORS_UPDATED" -> ColorsUpdatedEvent.of(
                    ((List<String>) payload.get("colors")).stream().map(ColorId::of).collect(Collectors.toSet())
            );
            case "THEMES_UPDATED" -> TopicsUpdatedEvent.of(
                    ((List<String>) payload.get("topics")).stream().map(TopicId::of).collect(Collectors.toSet())
            );
            case "AVAILABILITY_UPDATED" -> AvailabilityUpdatedEvent.of(
                    ((List<Map<String, Object>>) payload.get("availability")).stream()
                            .map(a -> FabricTypeAvailability.of(
                                    FabricTypeId.of((String) a.get("typeId")),
                                    (boolean) a.get("inStock")
                            ))
                            .collect(Collectors.toSet())
            );
            case "DELETED" -> DeletedEvent.of();
            default -> throw new IllegalStateException("Unexpected event name: " + name);
        };
    }

}
