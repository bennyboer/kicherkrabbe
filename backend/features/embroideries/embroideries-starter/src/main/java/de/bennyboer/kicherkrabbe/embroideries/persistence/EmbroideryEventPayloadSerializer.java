package de.bennyboer.kicherkrabbe.embroideries.persistence;

import de.bennyboer.kicherkrabbe.embroideries.EmbroideryCategoryId;
import de.bennyboer.kicherkrabbe.embroideries.EmbroideryName;
import de.bennyboer.kicherkrabbe.embroideries.ImageId;
import de.bennyboer.kicherkrabbe.embroideries.create.CreatedEvent;
import de.bennyboer.kicherkrabbe.embroideries.delete.DeletedEvent;
import de.bennyboer.kicherkrabbe.embroideries.delete.category.CategoryRemovedEvent;
import de.bennyboer.kicherkrabbe.embroideries.feature.FeaturedEvent;
import de.bennyboer.kicherkrabbe.embroideries.publish.PublishedEvent;
import de.bennyboer.kicherkrabbe.embroideries.rename.RenamedEvent;
import de.bennyboer.kicherkrabbe.embroideries.unfeature.UnfeaturedEvent;
import de.bennyboer.kicherkrabbe.embroideries.unpublish.UnpublishedEvent;
import de.bennyboer.kicherkrabbe.embroideries.update.categories.CategoriesUpdatedEvent;
import de.bennyboer.kicherkrabbe.embroideries.update.image.ImageUpdatedEvent;
import de.bennyboer.kicherkrabbe.eventsourcing.EventSerializer;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class EmbroideryEventPayloadSerializer implements EventSerializer {

    @Override
    public Map<String, Object> serialize(Event event) {
        return switch (event) {
            case CreatedEvent e -> Map.of(
                    "name", e.getName().getValue(),
                    "image", e.getImage().getValue(),
                    "categories", serializeCategories(e.getCategories())
            );
            case RenamedEvent e -> Map.of(
                    "name", e.getName().getValue()
            );
            case ImageUpdatedEvent e -> Map.of(
                    "image", e.getImage().getValue()
            );
            case CategoriesUpdatedEvent e -> Map.of(
                    "categories", serializeCategories(e.getCategories())
            );
            case CategoryRemovedEvent e -> Map.of(
                    "categoryId", e.getCategoryId().getValue()
            );
            case PublishedEvent ignored -> Map.of();
            case UnpublishedEvent ignored -> Map.of();
            case FeaturedEvent ignored -> Map.of();
            case UnfeaturedEvent ignored -> Map.of();
            case DeletedEvent ignored -> Map.of();
            default -> throw new IllegalStateException("Unexpected event: " + event.getEventName().getValue());
        };
    }

    @Override
    @SuppressWarnings("unchecked")
    public Event deserialize(EventName name, Version eventVersion, Map<String, Object> payload) {
        return switch (name.getValue()) {
            case "CREATED" -> CreatedEvent.of(
                    EmbroideryName.of((String) payload.get("name")),
                    ImageId.of((String) payload.get("image")),
                    deserializeCategories((Collection<String>) payload.get("categories"))
            );
            case "RENAMED" -> RenamedEvent.of(EmbroideryName.of((String) payload.get("name")));
            case "IMAGE_UPDATED" -> ImageUpdatedEvent.of(ImageId.of((String) payload.get("image")));
            case "CATEGORIES_UPDATED" -> CategoriesUpdatedEvent.of(
                    deserializeCategories((Collection<String>) payload.get("categories"))
            );
            case "CATEGORY_REMOVED" -> CategoryRemovedEvent.of(
                    EmbroideryCategoryId.of((String) payload.get("categoryId"))
            );
            case "PUBLISHED" -> PublishedEvent.of();
            case "UNPUBLISHED" -> UnpublishedEvent.of();
            case "FEATURED" -> FeaturedEvent.of();
            case "UNFEATURED" -> UnfeaturedEvent.of();
            case "DELETED" -> DeletedEvent.of();
            default -> throw new IllegalStateException("Unexpected event name: " + name.getValue());
        };
    }

    private List<String> serializeCategories(Set<EmbroideryCategoryId> categories) {
        return categories.stream()
                .map(EmbroideryCategoryId::getValue)
                .sorted()
                .toList();
    }

    private Set<EmbroideryCategoryId> deserializeCategories(Collection<String> categories) {
        return categories.stream()
                .map(EmbroideryCategoryId::of)
                .collect(Collectors.toSet());
    }

}
