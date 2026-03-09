package de.bennyboer.kicherkrabbe.fabrics.persistence;

import de.bennyboer.kicherkrabbe.eventsourcing.EventSerializer;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.fabrics.*;
import de.bennyboer.kicherkrabbe.fabrics.create.CreatedEvent;
import de.bennyboer.kicherkrabbe.fabrics.delete.DeletedEvent;
import de.bennyboer.kicherkrabbe.fabrics.delete.colors.ColorRemovedEvent;
import de.bennyboer.kicherkrabbe.fabrics.delete.fabrictype.FabricTypeRemovedEvent;
import de.bennyboer.kicherkrabbe.fabrics.delete.topics.TopicRemovedEvent;
import de.bennyboer.kicherkrabbe.fabrics.feature.FeaturedEvent;
import de.bennyboer.kicherkrabbe.fabrics.publish.PublishedEvent;
import de.bennyboer.kicherkrabbe.fabrics.rename.RenamedEvent;
import de.bennyboer.kicherkrabbe.fabrics.unfeature.UnfeaturedEvent;
import de.bennyboer.kicherkrabbe.fabrics.unpublish.UnpublishedEvent;
import de.bennyboer.kicherkrabbe.fabrics.update.availability.AvailabilityUpdatedEvent;
import de.bennyboer.kicherkrabbe.fabrics.update.colors.ColorsUpdatedEvent;
import de.bennyboer.kicherkrabbe.fabrics.update.images.ImagesUpdatedEvent;
import de.bennyboer.kicherkrabbe.fabrics.update.topics.TopicsUpdatedEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class FabricEventPayloadSerializer implements EventSerializer {

    @Override
    public Map<String, Object> serialize(Event event) {
        return switch (event) {
            case CreatedEvent e -> {
                var result = new HashMap<String, Object>();
                result.put("name", e.getName().getValue());
                result.put("kind", e.getKind().getValue());
                e.getImage().ifPresent(i -> result.put("image", i.getValue()));
                result.put("colors", e.getColors().stream().map(ColorId::getValue).toList());
                result.put("topics", e.getTopics().stream().map(TopicId::getValue).toList());
                result.put("availability", e.getAvailability().stream().map(a -> Map.of(
                        "typeId", a.getTypeId().getValue(),
                        "inStock", a.isInStock()
                )).toList());
                yield result;
            }
            case RenamedEvent e -> Map.of("name", e.getName().getValue());
            case PublishedEvent ignored -> Map.of();
            case UnpublishedEvent ignored -> Map.of();
            case FeaturedEvent ignored -> Map.of();
            case UnfeaturedEvent ignored -> Map.of();
            case ImagesUpdatedEvent e -> {
                var result = new HashMap<String, Object>();
                e.getImage().ifPresent(i -> result.put("image", i.getValue()));
                result.put("exampleImages", e.getExampleImages().stream().map(ImageId::getValue).toList());
                yield result;
            }
            case ColorsUpdatedEvent e -> Map.of("colors", e.getColors().stream().map(ColorId::getValue).toList());
            case TopicsUpdatedEvent e -> Map.of("topics", e.getTopics().stream().map(TopicId::getValue).toList());
            case AvailabilityUpdatedEvent e -> Map.of(
                    "availability", e.getAvailability().stream().map(a -> Map.of(
                            "typeId", a.getTypeId().getValue(),
                            "inStock", a.isInStock()
                    )).toList()
            );
            case DeletedEvent ignored -> Map.of();
            case ColorRemovedEvent e -> Map.of("colorId", e.getColorId().getValue());
            case TopicRemovedEvent e -> Map.of("topicId", e.getTopicId().getValue());
            case FabricTypeRemovedEvent e -> Map.of("typeId", e.getFabricTypeId().getValue());
            default -> throw new IllegalStateException("Unexpected event: " + event);
        };
    }

    @Override
    @SuppressWarnings("unchecked")
    public Event deserialize(EventName name, Version eventVersion, Map<String, Object> payload) {
        return switch (name.getValue()) {
            case "CREATED" -> CreatedEvent.of(
                    FabricName.of((String) payload.get("name")),
                    FabricKind.of((String) payload.get("kind")),
                    Optional.ofNullable((String) payload.get("image")).map(ImageId::of).orElse(null),
                    ((List<String>) payload.get("colors")).stream().map(ColorId::of).collect(Collectors.toSet()),
                    ((List<String>) payload.get("topics")).stream().map(TopicId::of).collect(Collectors.toSet()),
                    ((List<Map<String, Object>>) payload.get("availability")).stream()
                            .map(a -> FabricTypeAvailability.of(
                                    FabricTypeId.of((String) a.get("typeId")),
                                    (boolean) a.get("inStock")
                            ))
                            .collect(Collectors.toSet())
            );
            case "RENAMED" -> RenamedEvent.of(FabricName.of((String) payload.get("name")));
            case "PUBLISHED" -> PublishedEvent.of();
            case "UNPUBLISHED" -> UnpublishedEvent.of();
            case "FEATURED" -> FeaturedEvent.of();
            case "UNFEATURED" -> UnfeaturedEvent.of();
            case "IMAGES_UPDATED" -> ImagesUpdatedEvent.of(
                    Optional.ofNullable((String) payload.get("image")).map(ImageId::of).orElse(null),
                    ((List<String>) payload.get("exampleImages")).stream().map(ImageId::of).toList()
            );
            case "COLORS_UPDATED" -> ColorsUpdatedEvent.of(
                    ((List<String>) payload.get("colors")).stream().map(ColorId::of).collect(Collectors.toSet())
            );
            case "TOPICS_UPDATED" -> TopicsUpdatedEvent.of(
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
            case "COLOR_REMOVED" -> ColorRemovedEvent.of(ColorId.of((String) payload.get("colorId")));
            case "TOPIC_REMOVED" -> TopicRemovedEvent.of(TopicId.of((String) payload.get("topicId")));
            case "FABRIC_TYPE_REMOVED" -> FabricTypeRemovedEvent.of(FabricTypeId.of((String) payload.get("typeId")));
            default -> throw new IllegalStateException("Unexpected event name: " + name);
        };
    }

}
