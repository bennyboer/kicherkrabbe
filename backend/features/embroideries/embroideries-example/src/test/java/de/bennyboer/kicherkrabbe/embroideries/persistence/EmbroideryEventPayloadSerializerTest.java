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
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class EmbroideryEventPayloadSerializerTest {

    private final EmbroideryEventPayloadSerializer serializer = new EmbroideryEventPayloadSerializer();

    @Test
    void shouldSerializeAndDeserializeCreatedEvent() {
        // given: a created event
        var event = CreatedEvent.of(
                EmbroideryName.of("Crab"),
                ImageId.of("IMAGE_ID"),
                Set.of(EmbroideryCategoryId.of("SEA_ID"), EmbroideryCategoryId.of("ANIMALS_ID"))
        );

        // when: serializing the event
        var serialized = serializer.serialize(event);

        // then: the payload contains all fields with sorted categories
        assertThat(serialized).isEqualTo(Map.of(
                "name", "Crab",
                "image", "IMAGE_ID",
                "categories", List.of("ANIMALS_ID", "SEA_ID")
        ));

        // and: the event can be deserialized again
        assertRoundTrip(event);
    }

    @Test
    void shouldSerializeAndDeserializeRenamedEvent() {
        // given: a renamed event
        var event = RenamedEvent.of(EmbroideryName.of("Happy Crab"));

        // when: serializing the event
        var serialized = serializer.serialize(event);

        // then: the payload contains the new name
        assertThat(serialized).isEqualTo(Map.of("name", "Happy Crab"));

        // and: the event can be deserialized again
        assertRoundTrip(event);
    }

    @Test
    void shouldSerializeAndDeserializeImageUpdatedEvent() {
        // given: an image updated event
        var event = ImageUpdatedEvent.of(ImageId.of("NEW_IMAGE_ID"));

        // when: serializing the event
        var serialized = serializer.serialize(event);

        // then: the payload contains the new image
        assertThat(serialized).isEqualTo(Map.of("image", "NEW_IMAGE_ID"));

        // and: the event can be deserialized again
        assertRoundTrip(event);
    }

    @Test
    void shouldSerializeAndDeserializeCategoriesUpdatedEvent() {
        // given: a categories updated event
        var event = CategoriesUpdatedEvent.of(Set.of(EmbroideryCategoryId.of("ANIMALS_ID")));

        // when: serializing the event
        var serialized = serializer.serialize(event);

        // then: the payload contains the categories
        assertThat(serialized).isEqualTo(Map.of("categories", List.of("ANIMALS_ID")));

        // and: the event can be deserialized again
        assertRoundTrip(event);
    }

    @Test
    void shouldSerializeAndDeserializeCategoryRemovedEvent() {
        // given: a category removed event
        var event = CategoryRemovedEvent.of(EmbroideryCategoryId.of("ANIMALS_ID"));

        // when: serializing the event
        var serialized = serializer.serialize(event);

        // then: the payload contains the removed category
        assertThat(serialized).isEqualTo(Map.of("categoryId", "ANIMALS_ID"));

        // and: the event can be deserialized again
        assertRoundTrip(event);
    }

    @Test
    void shouldSerializeAndDeserializeEventsWithoutPayload() {
        // given: events without payload
        List<Event> events = List.of(
                PublishedEvent.of(),
                UnpublishedEvent.of(),
                FeaturedEvent.of(),
                UnfeaturedEvent.of(),
                DeletedEvent.of()
        );

        for (var event : events) {
            // when: serializing the event
            var serialized = serializer.serialize(event);

            // then: the payload is empty
            assertThat(serialized).isEmpty();

            // and: the event can be deserialized again
            assertRoundTrip(event);
        }
    }

    private void assertRoundTrip(Event event) {
        var serialized = serializer.serialize(event);
        var deserialized = serializer.deserialize(event.getEventName(), event.getVersion(), serialized);

        assertThat(deserialized).isEqualTo(event);
    }

}
