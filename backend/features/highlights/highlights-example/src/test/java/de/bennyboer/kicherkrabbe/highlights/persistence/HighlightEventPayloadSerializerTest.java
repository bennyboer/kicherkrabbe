package de.bennyboer.kicherkrabbe.highlights.persistence;

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
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class HighlightEventPayloadSerializerTest {

    private final HighlightEventPayloadSerializer serializer = new HighlightEventPayloadSerializer();

    @Test
    void shouldSerializeAndDeserializeCreatedEvent() {
        var event = CreatedEvent.of(
                ImageId.of("IMAGE_ID"),
                100L
        );
        var serialized = serializer.serialize(event);

        assertThat(serialized).isEqualTo(Map.of(
                "imageId", "IMAGE_ID",
                "sortOrder", 100L
        ));

        var deserialized = serializer.deserialize(CreatedEvent.NAME, CreatedEvent.VERSION, serialized);

        assertThat(deserialized).isEqualTo(event);
    }

    @Test
    void shouldSerializeAndDeserializeImageUpdatedEvent() {
        var event = ImageUpdatedEvent.of(ImageId.of("NEW_IMAGE_ID"));
        var serialized = serializer.serialize(event);

        assertThat(serialized).isEqualTo(Map.of(
                "imageId", "NEW_IMAGE_ID"
        ));

        var deserialized = serializer.deserialize(ImageUpdatedEvent.NAME, ImageUpdatedEvent.VERSION, serialized);

        assertThat(deserialized).isEqualTo(event);
    }

    @Test
    void shouldSerializeAndDeserializeLinkAddedEvent() {
        var event = LinkAddedEvent.of(
                Link.of(LinkType.PATTERN, LinkId.of("PATTERN_ID"), LinkName.of("Pattern"))
        );
        var serialized = serializer.serialize(event);

        assertThat(serialized).isEqualTo(Map.of(
                "link", Map.of(
                        "type", "PATTERN",
                        "id", "PATTERN_ID",
                        "name", "Pattern"
                )
        ));

        var deserialized = serializer.deserialize(LinkAddedEvent.NAME, LinkAddedEvent.VERSION, serialized);

        assertThat(deserialized).isEqualTo(event);
    }

    @Test
    void shouldSerializeAndDeserializeLinkUpdatedEvent() {
        var event = LinkUpdatedEvent.of(
                Link.of(LinkType.FABRIC, LinkId.of("FABRIC_ID"), LinkName.of("Fabric"))
        );
        var serialized = serializer.serialize(event);

        assertThat(serialized).isEqualTo(Map.of(
                "link", Map.of(
                        "type", "FABRIC",
                        "id", "FABRIC_ID",
                        "name", "Fabric"
                )
        ));

        var deserialized = serializer.deserialize(LinkUpdatedEvent.NAME, LinkUpdatedEvent.VERSION, serialized);

        assertThat(deserialized).isEqualTo(event);
    }

    @Test
    void shouldSerializeAndDeserializeLinkRemovedEvent() {
        var event = LinkRemovedEvent.of(LinkType.PATTERN, LinkId.of("PATTERN_ID"));
        var serialized = serializer.serialize(event);

        assertThat(serialized).isEqualTo(Map.of(
                "linkType", "PATTERN",
                "linkId", "PATTERN_ID"
        ));

        var deserialized = serializer.deserialize(LinkRemovedEvent.NAME, LinkRemovedEvent.VERSION, serialized);

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
    void shouldSerializeAndDeserializeSortOrderUpdatedEvent() {
        var event = SortOrderUpdatedEvent.of(50L);
        var serialized = serializer.serialize(event);

        assertThat(serialized).isEqualTo(Map.of(
                "sortOrder", 50L
        ));

        var deserialized = serializer.deserialize(SortOrderUpdatedEvent.NAME, SortOrderUpdatedEvent.VERSION, serialized);

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

}
