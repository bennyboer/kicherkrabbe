package de.bennyboer.kicherkrabbe.products.persistence;

import de.bennyboer.kicherkrabbe.eventsourcing.EventSerializer;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.products.product.*;
import de.bennyboer.kicherkrabbe.products.product.create.CreatedEvent;
import de.bennyboer.kicherkrabbe.products.product.delete.DeletedEvent;
import de.bennyboer.kicherkrabbe.products.product.fabric.composition.update.FabricCompositionUpdatedEvent;
import de.bennyboer.kicherkrabbe.products.product.images.update.ImagesUpdatedEvent;
import de.bennyboer.kicherkrabbe.products.product.links.add.LinkAddedEvent;
import de.bennyboer.kicherkrabbe.products.product.links.remove.LinkRemovedEvent;
import de.bennyboer.kicherkrabbe.products.product.notes.update.NotesUpdatedEvent;
import de.bennyboer.kicherkrabbe.products.product.produced.update.ProducedAtUpdatedEvent;
import de.bennyboer.kicherkrabbe.products.product.snapshot.SnapshottedEvent;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class ProductEventPayloadSerializer implements EventSerializer {

    @Override
    public Map<String, Object> serialize(Event event) {
        return switch (event) {
            case CreatedEvent e -> Map.of(
                    "number", e.getNumber().getValue(),
                    "images", serializeImages(e.getImages()),
                    "links", serializeLinks(e.getLinks()),
                    "fabricComposition", serializeFabricComposition(e.getFabricComposition()),
                    "notes", serializeNotes(e.getNotes()),
                    "producedAt", e.getProducedAt().toString()
            );
            case SnapshottedEvent e -> {
                Map<String, Object> result = new HashMap<>(Map.of(
                        "number", e.getNumber().getValue(),
                        "images", serializeImages(e.getImages()),
                        "links", serializeLinks(e.getLinks()),
                        "fabricComposition", serializeFabricComposition(e.getFabricComposition()),
                        "notes", serializeNotes(e.getNotes()),
                        "producedAt", e.getProducedAt().toString(),
                        "createdAt", e.getCreatedAt().toString()
                ));

                e.getDeletedAt().ifPresent(deletedAt -> result.put("deletedAt", deletedAt.toString()));

                yield result;
            }
            case FabricCompositionUpdatedEvent e -> Map.of(
                    "fabricComposition", serializeFabricComposition(e.getFabricComposition())
            );
            case NotesUpdatedEvent e -> Map.of(
                    "notes", serializeNotes(e.getNotes())
            );
            case ImagesUpdatedEvent e -> Map.of(
                    "images", serializeImages(e.getImages())
            );
            case ProducedAtUpdatedEvent e -> Map.of(
                    "producedAt", e.getProducedAt().toString()
            );
            case LinkAddedEvent e -> serializeLink(e.getLink());
            case LinkRemovedEvent e -> Map.of(
                    "type", serializeLinkType(e.getLinkType()),
                    "id", e.getLinkId().getValue()
            );
            case DeletedEvent ignored -> Map.of();
            default -> throw new IllegalStateException("Unexpected event: " + event);
        };
    }

    @Override
    public Event deserialize(EventName name, Version eventVersion, Map<String, Object> payload) {
        return switch (name.getValue()) {
            case "CREATED" -> CreatedEvent.of(
                    ProductNumber.of((String) payload.get("number")),
                    deserializeImages((List<String>) payload.get("images")),
                    deserializeLinks((List<Map<String, Object>>) payload.get("links")),
                    deserializeFabricComposition((List<Map<String, Object>>) payload.get("fabricComposition")),
                    deserializeNotes((Map<String, Object>) payload.get("notes")),
                    Instant.parse((String) payload.get("producedAt"))
            );
            case "SNAPSHOTTED" -> SnapshottedEvent.of(
                    ProductNumber.of((String) payload.get("number")),
                    deserializeImages((List<String>) payload.get("images")),
                    deserializeLinks((List<Map<String, Object>>) payload.get("links")),
                    deserializeFabricComposition((List<Map<String, Object>>) payload.get("fabricComposition")),
                    deserializeNotes((Map<String, Object>) payload.get("notes")),
                    Instant.parse((String) payload.get("producedAt")),
                    Instant.parse((String) payload.get("createdAt")),
                    payload.containsKey("deletedAt") ? Instant.parse((String) payload.get("deletedAt")) : null
            );
            case "FABRIC_COMPOSITION_UPDATED" -> FabricCompositionUpdatedEvent.of(
                    deserializeFabricComposition((List<Map<String, Object>>) payload.get("fabricComposition"))
            );
            case "NOTES_UPDATED" -> NotesUpdatedEvent.of(
                    deserializeNotes((Map<String, Object>) payload.get("notes"))
            );
            case "IMAGES_UPDATED" -> ImagesUpdatedEvent.of(
                    deserializeImages((List<String>) payload.get("images"))
            );
            case "PRODUCED_AT_UPDATED" -> ProducedAtUpdatedEvent.of(
                    Instant.parse((String) payload.get("producedAt"))
            );
            case "LINK_ADDED" -> LinkAddedEvent.of(
                    deserializeLink(payload)
            );
            case "LINK_REMOVED" -> LinkRemovedEvent.of(
                    deserializeLinkType((String) payload.get("type")),
                    LinkId.of((String) payload.get("id"))
            );
            case "DELETED" -> DeletedEvent.of();
            default -> throw new IllegalStateException("Unexpected event name: " + name);
        };
    }

    private List<Map<String, Object>> serializeLinks(Links links) {
        return links.getLinks()
                .stream()
                .map(this::serializeLink)
                .toList();
    }

    private Links deserializeLinks(List<Map<String, Object>> payload) {
        var links = payload.stream()
                .map(this::deserializeLink)
                .collect(Collectors.toSet());

        return Links.of(links);
    }

    private Map<String, Object> serializeNotes(Notes notes) {
        return Map.of(
                "contains", notes.getContains().getValue(),
                "care", notes.getCare().getValue(),
                "safety", notes.getSafety().getValue()
        );
    }

    private Notes deserializeNotes(Map<String, Object> payload) {
        return Notes.of(
                Note.of((String) payload.get("contains")),
                Note.of((String) payload.get("care")),
                Note.of((String) payload.get("safety"))
        );
    }

    private List<Map<String, Object>> serializeFabricComposition(FabricComposition composition) {
        return composition.getItems()
                .stream()
                .sorted(Comparator.comparing((FabricCompositionItem item) -> item.getPercentage().getValue()).reversed())
                .map(this::serializeFabricCompositionItem)
                .toList();
    }

    private FabricComposition deserializeFabricComposition(List<Map<String, Object>> itemsPayload) {
        Set<FabricCompositionItem> items = itemsPayload.stream()
                .map(this::deserializeFabricCompositionItem)
                .collect(Collectors.toSet());

        return FabricComposition.of(items);
    }

    private Map<String, Object> serializeFabricCompositionItem(FabricCompositionItem item) {
        return Map.of(
                "fabricType", serializeFabricType(item.getFabricType()),
                "percentage", item.getPercentage().getValue()
        );
    }

    private FabricCompositionItem deserializeFabricCompositionItem(Map<String, Object> payload) {
        FabricType fabricType = deserializeFabricType((String) payload.get("fabricType"));
        LowPrecisionFloat percentage = LowPrecisionFloat.of((long) payload.get("percentage"));

        return FabricCompositionItem.of(fabricType, percentage);
    }

    private List<String> serializeImages(List<ImageId> images) {
        return images.stream()
                .map(ImageId::getValue)
                .toList();
    }

    private List<ImageId> deserializeImages(List<String> images) {
        return images.stream()
                .map(ImageId::of)
                .toList();
    }

    private Map<String, Object> serializeLink(Link link) {
        return Map.of(
                "type", serializeLinkType(link.getType()),
                "id", link.getId().getValue(),
                "name", link.getName().getValue()
        );
    }

    private Link deserializeLink(Map<String, Object> payload) {
        return Link.of(
                deserializeLinkType((String) payload.get("type")),
                LinkId.of((String) payload.get("id")),
                LinkName.of((String) payload.get("name"))
        );
    }

    private String serializeLinkType(LinkType type) {
        return switch (type) {
            case PATTERN -> "PATTERN";
            case FABRIC -> "FABRIC";
        };
    }

    private LinkType deserializeLinkType(String type) {
        return switch (type) {
            case "PATTERN" -> LinkType.PATTERN;
            case "FABRIC" -> LinkType.FABRIC;
            default -> throw new IllegalStateException("Unexpected link type: " + type);
        };
    }

    private String serializeFabricType(FabricType type) {
        return type.name();
    }

    private FabricType deserializeFabricType(String type) {
        return FabricType.valueOf(type);
    }

}
