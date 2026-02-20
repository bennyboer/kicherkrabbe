package de.bennyboer.kicherkrabbe.offers.persistence;

import de.bennyboer.kicherkrabbe.eventsourcing.EventSerializer;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.money.Currency;
import de.bennyboer.kicherkrabbe.money.Money;
import de.bennyboer.kicherkrabbe.offers.*;
import de.bennyboer.kicherkrabbe.offers.archive.ArchivedEvent;
import de.bennyboer.kicherkrabbe.offers.create.CreatedEvent;
import de.bennyboer.kicherkrabbe.offers.delete.DeletedEvent;
import de.bennyboer.kicherkrabbe.offers.discount.add.DiscountAddedEvent;
import de.bennyboer.kicherkrabbe.offers.discount.remove.DiscountRemovedEvent;
import de.bennyboer.kicherkrabbe.offers.images.update.ImagesUpdatedEvent;
import de.bennyboer.kicherkrabbe.offers.notes.update.NotesUpdatedEvent;
import de.bennyboer.kicherkrabbe.offers.price.update.PriceUpdatedEvent;
import de.bennyboer.kicherkrabbe.offers.publish.PublishedEvent;
import de.bennyboer.kicherkrabbe.offers.reserve.ReservedEvent;
import de.bennyboer.kicherkrabbe.offers.unpublish.UnpublishedEvent;
import de.bennyboer.kicherkrabbe.offers.unreserve.UnreservedEvent;

import java.util.*;

public class OfferEventPayloadSerializer implements EventSerializer {

    @Override
    public Map<String, Object> serialize(Event event) {
        return switch (event) {
            case CreatedEvent e -> {
                var result = new HashMap<String, Object>();
                result.put("productId", e.getProductId().getValue());
                result.put("images", e.getImages().stream().map(ImageId::getValue).toList());
                result.put("notes", serializeNotes(e.getNotes()));
                result.put("price", serializeMoney(e.getPrice()));
                yield Collections.unmodifiableMap(result);
            }
            case DeletedEvent ignored -> Map.of();
            case PublishedEvent ignored -> Map.of();
            case UnpublishedEvent ignored -> Map.of();
            case ReservedEvent ignored -> Map.of();
            case UnreservedEvent ignored -> Map.of();
            case ArchivedEvent ignored -> Map.of();
            case ImagesUpdatedEvent e -> Map.of(
                    "images", e.getImages().stream().map(ImageId::getValue).toList()
            );
            case NotesUpdatedEvent e -> Map.of("notes", serializeNotes(e.getNotes()));
            case PriceUpdatedEvent e -> Map.of("price", serializeMoney(e.getPrice()));
            case DiscountAddedEvent e -> Map.of("discountedPrice", serializeMoney(e.getDiscountedPrice()));
            case DiscountRemovedEvent ignored -> Map.of();
            default -> throw new IllegalStateException("Unexpected event: " + event);
        };
    }

    @Override
    @SuppressWarnings("unchecked")
    public Event deserialize(EventName name, Version eventVersion, Map<String, Object> payload) {
        return switch (name.getValue()) {
            case "CREATED" -> CreatedEvent.of(
                    ProductId.of((String) payload.get("productId")),
                    ((List<String>) payload.get("images")).stream().map(ImageId::of).toList(),
                    deserializeNotes((Map<String, Object>) payload.get("notes")),
                    deserializeMoney((Map<String, Object>) payload.get("price"))
            );
            case "DELETED" -> DeletedEvent.of();
            case "PUBLISHED" -> PublishedEvent.of();
            case "UNPUBLISHED" -> UnpublishedEvent.of();
            case "RESERVED" -> ReservedEvent.of();
            case "UNRESERVED" -> UnreservedEvent.of();
            case "ARCHIVED" -> ArchivedEvent.of();
            case "IMAGES_UPDATED" -> ImagesUpdatedEvent.of(
                    ((List<String>) payload.get("images")).stream().map(ImageId::of).toList()
            );
            case "NOTES_UPDATED" -> NotesUpdatedEvent.of(
                    deserializeNotes((Map<String, Object>) payload.get("notes"))
            );
            case "PRICE_UPDATED" -> PriceUpdatedEvent.of(
                    deserializeMoney((Map<String, Object>) payload.get("price"))
            );
            case "DISCOUNT_ADDED" -> DiscountAddedEvent.of(
                    deserializeMoney((Map<String, Object>) payload.get("discountedPrice"))
            );
            case "DISCOUNT_REMOVED" -> DiscountRemovedEvent.of();
            default -> throw new IllegalStateException("Unexpected event name: " + name);
        };
    }

    private Map<String, Object> serializeNotes(Notes notes) {
        var result = new HashMap<String, Object>();
        result.put("description", notes.getDescription().getValue());
        notes.getContains().ifPresent(n -> result.put("contains", n.getValue()));
        notes.getCare().ifPresent(n -> result.put("care", n.getValue()));
        notes.getSafety().ifPresent(n -> result.put("safety", n.getValue()));
        return result;
    }

    private Notes deserializeNotes(Map<String, Object> notes) {
        return Notes.of(
                Note.of((String) notes.get("description")),
                notes.containsKey("contains") ? Note.of((String) notes.get("contains")) : null,
                notes.containsKey("care") ? Note.of((String) notes.get("care")) : null,
                notes.containsKey("safety") ? Note.of((String) notes.get("safety")) : null
        );
    }

    private Map<String, Object> serializeMoney(Money money) {
        return Map.of(
                "amount", money.getAmount(),
                "currency", money.getCurrency().getShortForm()
        );
    }

    private Money deserializeMoney(Map<String, Object> money) {
        return Money.of(
                ((Number) money.get("amount")).longValue(),
                Currency.fromShortForm((String) money.get("currency"))
        );
    }

}
