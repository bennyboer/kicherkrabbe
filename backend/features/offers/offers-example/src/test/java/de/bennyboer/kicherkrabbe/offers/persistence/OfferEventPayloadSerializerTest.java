package de.bennyboer.kicherkrabbe.offers.persistence;

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
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class OfferEventPayloadSerializerTest {

    private final OfferEventPayloadSerializer serializer = new OfferEventPayloadSerializer();

    @Test
    void shouldSerializeAndDeserializeCreatedEvent() {
        var event = CreatedEvent.of(
                ProductId.of("PRODUCT_ID"),
                List.of(ImageId.of("IMAGE_ID_1"), ImageId.of("IMAGE_ID_2")),
                Notes.of(
                        Note.of("Description"),
                        Note.of("Contains"),
                        Note.of("Care"),
                        Note.of("Safety")
                ),
                Money.of(1999L, Currency.euro())
        );
        var serialized = serializer.serialize(event);

        assertThat(serialized).isEqualTo(Map.of(
                "productId", "PRODUCT_ID",
                "images", List.of("IMAGE_ID_1", "IMAGE_ID_2"),
                "notes", Map.of(
                        "description", "Description",
                        "contains", "Contains",
                        "care", "Care",
                        "safety", "Safety"
                ),
                "price", Map.of("amount", 1999L, "currency", "EUR")
        ));

        var deserialized = serializer.deserialize(CreatedEvent.NAME, CreatedEvent.VERSION, serialized);
        assertThat(deserialized).isEqualTo(event);
    }

    @Test
    void shouldSerializeAndDeserializeCreatedEventWithNullableNotes() {
        var event = CreatedEvent.of(
                ProductId.of("PRODUCT_ID"),
                List.of(ImageId.of("IMAGE_ID")),
                Notes.of(Note.of("Description"), null, null, null),
                Money.of(999L, Currency.euro())
        );
        var serialized = serializer.serialize(event);

        var deserialized = serializer.deserialize(CreatedEvent.NAME, CreatedEvent.VERSION, serialized);
        assertThat(deserialized).isEqualTo(event);
    }

    @Test
    void shouldSerializeAndDeserializeDeletedEvent() {
        var event = DeletedEvent.of();
        var serialized = serializer.serialize(event);

        assertThat(serialized).isEmpty();

        var deserialized = serializer.deserialize(DeletedEvent.NAME, DeletedEvent.VERSION, serialized);
        assertThat(deserialized).isEqualTo(event);
    }

    @Test
    void shouldSerializeAndDeserializePublishedEvent() {
        var event = PublishedEvent.of();
        var serialized = serializer.serialize(event);

        assertThat(serialized).isEmpty();

        var deserialized = serializer.deserialize(PublishedEvent.NAME, PublishedEvent.VERSION, serialized);
        assertThat(deserialized).isEqualTo(event);
    }

    @Test
    void shouldSerializeAndDeserializeUnpublishedEvent() {
        var event = UnpublishedEvent.of();
        var serialized = serializer.serialize(event);

        assertThat(serialized).isEmpty();

        var deserialized = serializer.deserialize(UnpublishedEvent.NAME, UnpublishedEvent.VERSION, serialized);
        assertThat(deserialized).isEqualTo(event);
    }

    @Test
    void shouldSerializeAndDeserializeReservedEvent() {
        var event = ReservedEvent.of();
        var serialized = serializer.serialize(event);

        assertThat(serialized).isEmpty();

        var deserialized = serializer.deserialize(ReservedEvent.NAME, ReservedEvent.VERSION, serialized);
        assertThat(deserialized).isEqualTo(event);
    }

    @Test
    void shouldSerializeAndDeserializeUnreservedEvent() {
        var event = UnreservedEvent.of();
        var serialized = serializer.serialize(event);

        assertThat(serialized).isEmpty();

        var deserialized = serializer.deserialize(UnreservedEvent.NAME, UnreservedEvent.VERSION, serialized);
        assertThat(deserialized).isEqualTo(event);
    }

    @Test
    void shouldSerializeAndDeserializeArchivedEvent() {
        var event = ArchivedEvent.of();
        var serialized = serializer.serialize(event);

        assertThat(serialized).isEmpty();

        var deserialized = serializer.deserialize(ArchivedEvent.NAME, ArchivedEvent.VERSION, serialized);
        assertThat(deserialized).isEqualTo(event);
    }

    @Test
    void shouldSerializeAndDeserializeImagesUpdatedEvent() {
        var event = ImagesUpdatedEvent.of(List.of(ImageId.of("IMAGE_ID_1"), ImageId.of("IMAGE_ID_2")));
        var serialized = serializer.serialize(event);

        assertThat(serialized).isEqualTo(Map.of("images", List.of("IMAGE_ID_1", "IMAGE_ID_2")));

        var deserialized = serializer.deserialize(ImagesUpdatedEvent.NAME, ImagesUpdatedEvent.VERSION, serialized);
        assertThat(deserialized).isEqualTo(event);
    }

    @Test
    void shouldSerializeAndDeserializeNotesUpdatedEvent() {
        var event = NotesUpdatedEvent.of(Notes.of(
                Note.of("Description"),
                Note.of("Contains"),
                Note.of("Care"),
                Note.of("Safety")
        ));
        var serialized = serializer.serialize(event);

        assertThat(serialized).isEqualTo(Map.of(
                "notes", Map.of(
                        "description", "Description",
                        "contains", "Contains",
                        "care", "Care",
                        "safety", "Safety"
                )
        ));

        var deserialized = serializer.deserialize(NotesUpdatedEvent.NAME, NotesUpdatedEvent.VERSION, serialized);
        assertThat(deserialized).isEqualTo(event);
    }

    @Test
    void shouldSerializeAndDeserializePriceUpdatedEvent() {
        var event = PriceUpdatedEvent.of(Money.of(2499L, Currency.euro()));
        var serialized = serializer.serialize(event);

        assertThat(serialized).isEqualTo(Map.of("price", Map.of("amount", 2499L, "currency", "EUR")));

        var deserialized = serializer.deserialize(PriceUpdatedEvent.NAME, PriceUpdatedEvent.VERSION, serialized);
        assertThat(deserialized).isEqualTo(event);
    }

    @Test
    void shouldSerializeAndDeserializeDiscountAddedEvent() {
        var event = DiscountAddedEvent.of(Money.of(1499L, Currency.euro()));
        var serialized = serializer.serialize(event);

        assertThat(serialized).isEqualTo(Map.of("discountedPrice", Map.of("amount", 1499L, "currency", "EUR")));

        var deserialized = serializer.deserialize(DiscountAddedEvent.NAME, DiscountAddedEvent.VERSION, serialized);
        assertThat(deserialized).isEqualTo(event);
    }

    @Test
    void shouldSerializeAndDeserializeDiscountRemovedEvent() {
        var event = DiscountRemovedEvent.of();
        var serialized = serializer.serialize(event);

        assertThat(serialized).isEmpty();

        var deserialized = serializer.deserialize(DiscountRemovedEvent.NAME, DiscountRemovedEvent.VERSION, serialized);
        assertThat(deserialized).isEqualTo(event);
    }

}
