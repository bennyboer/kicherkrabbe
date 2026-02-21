package de.bennyboer.kicherkrabbe.offers.persistence;

import de.bennyboer.kicherkrabbe.money.Currency;
import de.bennyboer.kicherkrabbe.money.Money;
import de.bennyboer.kicherkrabbe.offers.*;
import de.bennyboer.kicherkrabbe.offers.archive.ArchivedEvent;
import de.bennyboer.kicherkrabbe.offers.categories.remove.CategoryRemovedEvent;
import de.bennyboer.kicherkrabbe.offers.categories.update.CategoriesUpdatedEvent;
import de.bennyboer.kicherkrabbe.offers.create.CreatedEvent;
import de.bennyboer.kicherkrabbe.offers.delete.DeletedEvent;
import de.bennyboer.kicherkrabbe.offers.discount.add.DiscountAddedEvent;
import de.bennyboer.kicherkrabbe.offers.discount.remove.DiscountRemovedEvent;
import de.bennyboer.kicherkrabbe.offers.images.update.ImagesUpdatedEvent;
import de.bennyboer.kicherkrabbe.offers.notes.update.NotesUpdatedEvent;
import de.bennyboer.kicherkrabbe.offers.price.update.PriceUpdatedEvent;
import de.bennyboer.kicherkrabbe.offers.publish.PublishedEvent;
import de.bennyboer.kicherkrabbe.offers.reserve.ReservedEvent;
import de.bennyboer.kicherkrabbe.offers.size.update.SizeUpdatedEvent;
import de.bennyboer.kicherkrabbe.offers.title.update.TitleUpdatedEvent;
import de.bennyboer.kicherkrabbe.offers.unpublish.UnpublishedEvent;
import de.bennyboer.kicherkrabbe.offers.unreserve.UnreservedEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class OfferEventPayloadSerializerTest {

    private final OfferEventPayloadSerializer serializer = new OfferEventPayloadSerializer();

    @Test
    void shouldSerializeAndDeserializeCreatedEvent() {
        var event = CreatedEvent.of(
                OfferTitle.of("Test Offer"),
                OfferSize.of("L"),
                Set.of(OfferCategoryId.of("CAT_1")),
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

        assertThat(serialized).containsEntry("title", "Test Offer");
        assertThat(serialized).containsEntry("size", "L");
        assertThat(serialized).containsEntry("categoryIds", List.of("CAT_1"));
        assertThat(serialized).containsEntry("productId", "PRODUCT_ID");

        var deserialized = serializer.deserialize(CreatedEvent.NAME, CreatedEvent.VERSION, serialized);
        assertThat(deserialized).isEqualTo(event);
    }

    @Test
    void shouldSerializeAndDeserializeCreatedEventWithNullableNotes() {
        var event = CreatedEvent.of(
                OfferTitle.of("Test"),
                OfferSize.of("M"),
                Set.of(),
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
    void shouldDeserializeOldCreatedEventWithoutTitleSizeCategories() {
        var payload = Map.of(
                "productId", "PRODUCT_ID",
                "images", List.of("IMAGE_ID"),
                "notes", Map.of("description", "Description"),
                "price", Map.of("amount", 999L, "currency", "EUR")
        );

        var deserialized = (CreatedEvent) serializer.deserialize(CreatedEvent.NAME, CreatedEvent.VERSION, payload);
        assertThat(deserialized.getTitle()).isEqualTo(OfferTitle.of("Untitled"));
        assertThat(deserialized.getSize()).isEqualTo(OfferSize.of("N/A"));
        assertThat(deserialized.getCategories()).isEmpty();
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

    @Test
    void shouldSerializeAndDeserializeTitleUpdatedEvent() {
        var event = TitleUpdatedEvent.of(OfferTitle.of("New Title"));
        var serialized = serializer.serialize(event);

        assertThat(serialized).isEqualTo(Map.of("title", "New Title"));

        var deserialized = serializer.deserialize(TitleUpdatedEvent.NAME, TitleUpdatedEvent.VERSION, serialized);
        assertThat(deserialized).isEqualTo(event);
    }

    @Test
    void shouldSerializeAndDeserializeSizeUpdatedEvent() {
        var event = SizeUpdatedEvent.of(OfferSize.of("XL"));
        var serialized = serializer.serialize(event);

        assertThat(serialized).isEqualTo(Map.of("size", "XL"));

        var deserialized = serializer.deserialize(SizeUpdatedEvent.NAME, SizeUpdatedEvent.VERSION, serialized);
        assertThat(deserialized).isEqualTo(event);
    }

    @Test
    void shouldSerializeAndDeserializeCategoriesUpdatedEvent() {
        var event = CategoriesUpdatedEvent.of(Set.of(OfferCategoryId.of("CAT_1"), OfferCategoryId.of("CAT_2")));
        var serialized = serializer.serialize(event);

        var deserialized = serializer.deserialize(CategoriesUpdatedEvent.NAME, CategoriesUpdatedEvent.VERSION, serialized);
        assertThat(deserialized).isEqualTo(event);
    }

    @Test
    void shouldSerializeAndDeserializeCategoryRemovedEvent() {
        var event = CategoryRemovedEvent.of(OfferCategoryId.of("CAT_1"));
        var serialized = serializer.serialize(event);

        assertThat(serialized).isEqualTo(Map.of("categoryId", "CAT_1"));

        var deserialized = serializer.deserialize(CategoryRemovedEvent.NAME, CategoryRemovedEvent.VERSION, serialized);
        assertThat(deserialized).isEqualTo(event);
    }

}
