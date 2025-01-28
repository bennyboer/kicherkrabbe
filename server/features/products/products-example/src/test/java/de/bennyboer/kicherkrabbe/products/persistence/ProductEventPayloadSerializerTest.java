package de.bennyboer.kicherkrabbe.products.persistence;

import de.bennyboer.kicherkrabbe.products.product.*;
import de.bennyboer.kicherkrabbe.products.product.create.CreatedEvent;
import de.bennyboer.kicherkrabbe.products.product.delete.DeletedEvent;
import de.bennyboer.kicherkrabbe.products.product.fabric.composition.update.FabricCompositionUpdatedEvent;
import de.bennyboer.kicherkrabbe.products.product.images.update.ImagesUpdatedEvent;
import de.bennyboer.kicherkrabbe.products.product.links.add.LinkAddedEvent;
import de.bennyboer.kicherkrabbe.products.product.links.remove.LinkRemovedEvent;
import de.bennyboer.kicherkrabbe.products.product.links.update.LinkUpdatedEvent;
import de.bennyboer.kicherkrabbe.products.product.notes.update.NotesUpdatedEvent;
import de.bennyboer.kicherkrabbe.products.product.produced.update.ProducedAtUpdatedEvent;
import de.bennyboer.kicherkrabbe.products.product.snapshot.SnapshottedEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ProductEventPayloadSerializerTest {

    private final ProductEventPayloadSerializer serializer = new ProductEventPayloadSerializer();

    @Test
    void shouldSerializeAndDeserializeCreatedEvent() {
        // when: a created event is serialized
        var event = CreatedEvent.of(
                ProductNumber.of("0000000001"),
                List.of(ImageId.of("IMAGE_ID_1"), ImageId.of("IMAGE_ID_2")),
                Links.of(Set.of(
                        Link.of(LinkType.PATTERN, LinkId.of("PATTERN_ID"), LinkName.of("Pattern")),
                        Link.of(LinkType.FABRIC, LinkId.of("FABRIC_ID"), LinkName.of("Fabric"))
                )),
                FabricComposition.of(Set.of(
                        FabricCompositionItem.of(FabricType.COTTON, LowPrecisionFloat.of(8000L)),
                        FabricCompositionItem.of(FabricType.POLYESTER, LowPrecisionFloat.of(2000L))
                )),
                Notes.of(
                        Note.of("Contains"),
                        Note.of("Care"),
                        Note.of("Safety")
                ),
                Instant.parse("2024-12-08T12:30:00.000Z")
        );
        var serialized = serializer.serialize(event);

        // then: the serialized form is correct
        assertThat(serialized)
                .usingRecursiveComparison()
                .ignoringCollectionOrderInFields("links", "images", "fabricComposition")
                .isEqualTo(Map.of(
                        "number", "0000000001",
                        "images", List.of("IMAGE_ID_1", "IMAGE_ID_2"),
                        "links", List.of(
                                Map.of(
                                        "type", "PATTERN",
                                        "id", "PATTERN_ID",
                                        "name", "Pattern"
                                ),
                                Map.of(
                                        "type", "FABRIC",
                                        "id", "FABRIC_ID",
                                        "name", "Fabric"
                                )
                        ),
                        "fabricComposition", List.of(
                                Map.of(
                                        "fabricType", "COTTON",
                                        "percentage", 8000L
                                ),
                                Map.of(
                                        "fabricType", "POLYESTER",
                                        "percentage", 2000L
                                )
                        ),
                        "notes", Map.of(
                                "contains", "Contains",
                                "care", "Care",
                                "safety", "Safety"
                        ),
                        "producedAt", "2024-12-08T12:30:00Z"
                ));

        // when: the serialized form is deserialized
        var deserialized = serializer.deserialize(CreatedEvent.NAME, CreatedEvent.VERSION, serialized);

        // then: the deserialized form is correct
        assertThat(deserialized).isEqualTo(event);
    }

    @Test
    void shouldSerializeAndDeserializeSnapshottedEvent() {
        // when: a snapshotted event is serialized
        var event = SnapshottedEvent.of(
                ProductNumber.of("0000000001"),
                List.of(ImageId.of("IMAGE_ID_1"), ImageId.of("IMAGE_ID_2")),
                Links.of(Set.of(
                        Link.of(LinkType.PATTERN, LinkId.of("PATTERN_ID"), LinkName.of("Pattern")),
                        Link.of(LinkType.FABRIC, LinkId.of("FABRIC_ID"), LinkName.of("Fabric"))
                )),
                FabricComposition.of(Set.of(
                        FabricCompositionItem.of(FabricType.COTTON, LowPrecisionFloat.of(8000L)),
                        FabricCompositionItem.of(FabricType.POLYESTER, LowPrecisionFloat.of(2000L))
                )),
                Notes.of(
                        Note.of("Contains"),
                        Note.of("Care"),
                        Note.of("Safety")
                ),
                Instant.parse("2024-12-08T12:30:00.000Z"),
                Instant.parse("2024-12-08T12:31:00.000Z"),
                null
        );
        var serialized = serializer.serialize(event);

        // then: the serialized form is correct
        assertThat(serialized)
                .usingRecursiveComparison()
                .ignoringCollectionOrderInFields("links", "images", "fabricComposition")
                .isEqualTo(Map.of(
                        "number", "0000000001",
                        "images", List.of("IMAGE_ID_1", "IMAGE_ID_2"),
                        "links", List.of(
                                Map.of(
                                        "type", "PATTERN",
                                        "id", "PATTERN_ID",
                                        "name", "Pattern"
                                ),
                                Map.of(
                                        "type", "FABRIC",
                                        "id", "FABRIC_ID",
                                        "name", "Fabric"
                                )
                        ),
                        "fabricComposition", List.of(
                                Map.of(
                                        "fabricType", "COTTON",
                                        "percentage", 8000L
                                ),
                                Map.of(
                                        "fabricType", "POLYESTER",
                                        "percentage", 2000L
                                )
                        ),
                        "notes", Map.of(
                                "contains", "Contains",
                                "care", "Care",
                                "safety", "Safety"
                        ),
                        "producedAt", "2024-12-08T12:30:00Z",
                        "createdAt", "2024-12-08T12:31:00Z"
                ));

        // when: a snapshotted event with deleted at date is serialized
        var event2 = SnapshottedEvent.of(
                ProductNumber.of("0000000001"),
                List.of(ImageId.of("IMAGE_ID_1"), ImageId.of("IMAGE_ID_2")),
                Links.of(Set.of(
                        Link.of(LinkType.PATTERN, LinkId.of("PATTERN_ID"), LinkName.of("Pattern")),
                        Link.of(LinkType.FABRIC, LinkId.of("FABRIC_ID"), LinkName.of("Fabric"))
                )),
                FabricComposition.of(Set.of(
                        FabricCompositionItem.of(FabricType.COTTON, LowPrecisionFloat.of(8000L)),
                        FabricCompositionItem.of(FabricType.POLYESTER, LowPrecisionFloat.of(2000L))
                )),
                Notes.of(
                        Note.of("Contains"),
                        Note.of("Care"),
                        Note.of("Safety")
                ),
                Instant.parse("2024-12-08T12:30:00.000Z"),
                Instant.parse("2024-12-08T12:31:00.000Z"),
                Instant.parse("2024-12-08T12:32:00.000Z")
        );
        var serialized2 = serializer.serialize(event2);

        // then: the serialized form is correct
        assertThat(serialized2)
                .usingRecursiveComparison()
                .ignoringCollectionOrderInFields("links", "images", "fabricComposition")
                .isEqualTo(Map.of(
                        "number", "0000000001",
                        "images", List.of("IMAGE_ID_1", "IMAGE_ID_2"),
                        "links", List.of(
                                Map.of(
                                        "type", "PATTERN",
                                        "id", "PATTERN_ID",
                                        "name", "Pattern"
                                ),
                                Map.of(
                                        "type", "FABRIC",
                                        "id", "FABRIC_ID",
                                        "name", "Fabric"
                                )
                        ),
                        "fabricComposition", List.of(
                                Map.of(
                                        "fabricType", "COTTON",
                                        "percentage", 8000L
                                ),
                                Map.of(
                                        "fabricType", "POLYESTER",
                                        "percentage", 2000L
                                )
                        ),
                        "notes", Map.of(
                                "contains", "Contains",
                                "care", "Care",
                                "safety", "Safety"
                        ),
                        "producedAt", "2024-12-08T12:30:00Z",
                        "createdAt", "2024-12-08T12:31:00Z",
                        "deletedAt", "2024-12-08T12:32:00Z"
                ));

        // when: the serialized form is deserialized
        var deserialized = serializer.deserialize(SnapshottedEvent.NAME, SnapshottedEvent.VERSION, serialized);
        var deserialized2 = serializer.deserialize(SnapshottedEvent.NAME, SnapshottedEvent.VERSION, serialized2);

        // then: the deserialized form is correct
        assertThat(deserialized).isEqualTo(event);
        assertThat(deserialized2).isEqualTo(event2);
    }

    @Test
    void shouldSerializeAndDeserializeFabricCompositionUpdatedEvent() {
        // when: a fabric composition updated event is serialized
        var event = FabricCompositionUpdatedEvent.of(FabricComposition.of(Set.of(
                FabricCompositionItem.of(FabricType.COTTON, LowPrecisionFloat.of(8000L)),
                FabricCompositionItem.of(FabricType.POLYESTER, LowPrecisionFloat.of(2000L))
        )));
        var serialized = serializer.serialize(event);

        // then: the serialized form is correct
        assertThat(serialized).isEqualTo(Map.of(
                "fabricComposition", List.of(
                        Map.of(
                                "fabricType", "COTTON",
                                "percentage", 8000L
                        ),
                        Map.of(
                                "fabricType", "POLYESTER",
                                "percentage", 2000L
                        )
                )
        ));

        // when: the serialized form is deserialized
        var deserialized = serializer.deserialize(FabricCompositionUpdatedEvent.NAME, FabricCompositionUpdatedEvent.VERSION, serialized);

        // then: the deserialized form is correct
        assertThat(deserialized).isEqualTo(event);
    }

    @Test
    void shouldSerializeAndDeserializeImagesUpdatedEvent() {
        // when: an images updated event is serialized
        var event = ImagesUpdatedEvent.of(List.of(
                ImageId.of("IMAGE_ID_1"),
                ImageId.of("IMAGE_ID_2")
        ));
        var serialized = serializer.serialize(event);

        // then: the serialized form is correct
        assertThat(serialized).isEqualTo(Map.of(
                "images", List.of("IMAGE_ID_1", "IMAGE_ID_2")
        ));

        // when: the serialized form is deserialized
        var deserialized = serializer.deserialize(ImagesUpdatedEvent.NAME, ImagesUpdatedEvent.VERSION, serialized);

        // then: the deserialized form is correct
        assertThat(deserialized).isEqualTo(event);
    }

    @Test
    void shouldSerializeAndDeserializeLinkAddedEvent() {
        // when: a link added event is serialized
        var event = LinkAddedEvent.of(
                Link.of(LinkType.PATTERN, LinkId.of("PATTERN_ID"), LinkName.of("Pattern"))
        );
        var serialized = serializer.serialize(event);

        // then: the serialized form is correct
        assertThat(serialized).isEqualTo(Map.of(
                "type", "PATTERN",
                "id", "PATTERN_ID",
                "name", "Pattern"
        ));

        // when: the serialized form is deserialized
        var deserialized = serializer.deserialize(LinkAddedEvent.NAME, LinkAddedEvent.VERSION, serialized);

        // then: the deserialized form is correct
        assertThat(deserialized).isEqualTo(event);
    }

    @Test
    void shouldSerializeAndDeserializeLinkUpdatedEvent() {
        // when: a link updated event is serialized
        var event = LinkUpdatedEvent.of(
                Link.of(LinkType.PATTERN, LinkId.of("PATTERN_ID"), LinkName.of("Pattern"))
        );
        var serialized = serializer.serialize(event);

        // then: the serialized form is correct
        assertThat(serialized).isEqualTo(Map.of(
                "type", "PATTERN",
                "id", "PATTERN_ID",
                "name", "Pattern"
        ));

        // when: the serialized form is deserialized
        var deserialized = serializer.deserialize(LinkUpdatedEvent.NAME, LinkUpdatedEvent.VERSION, serialized);

        // then: the deserialized form is correct
        assertThat(deserialized).isEqualTo(event);
    }

    @Test
    void shouldSerializeAndDeserializeLinkRemovedEvent() {
        // when: a link removed event is serialized
        var event = LinkRemovedEvent.of(LinkType.PATTERN, LinkId.of("PATTERN_ID"));
        var serialized = serializer.serialize(event);

        // then: the serialized form is correct
        assertThat(serialized).isEqualTo(Map.of(
                "type", "PATTERN",
                "id", "PATTERN_ID"
        ));

        // when: the serialized form is deserialized
        var deserialized = serializer.deserialize(LinkRemovedEvent.NAME, LinkRemovedEvent.VERSION, serialized);

        // then: the deserialized form is correct
        assertThat(deserialized).isEqualTo(event);
    }

    @Test
    void shouldSerializeAndDeserializeNotesUpdatedEvent() {
        // when: a notes updated event is serialized
        var event = NotesUpdatedEvent.of(
                Notes.of(
                        Note.of("Contains"),
                        Note.of("Care"),
                        Note.of("Safety")
                )
        );
        var serialized = serializer.serialize(event);

        // then: the serialized form is correct
        assertThat(serialized).isEqualTo(Map.of(
                "notes", Map.of(
                        "contains", "Contains",
                        "care", "Care",
                        "safety", "Safety"
                )
        ));

        // when: the serialized form is deserialized
        var deserialized = serializer.deserialize(NotesUpdatedEvent.NAME, NotesUpdatedEvent.VERSION, serialized);

        // then: the deserialized form is correct
        assertThat(deserialized).isEqualTo(event);
    }

    @Test
    void shouldSerializeAndDeserializeProducedAtUpdatedEvent() {
        // when: a produced at updated event is serialized
        var event = ProducedAtUpdatedEvent.of(Instant.parse("2024-12-08T12:30:00.000Z"));
        var serialized = serializer.serialize(event);

        // then: the serialized form is correct
        assertThat(serialized).isEqualTo(Map.of(
                "producedAt", "2024-12-08T12:30:00Z"
        ));

        // when: the serialized form is deserialized
        var deserialized = serializer.deserialize(ProducedAtUpdatedEvent.NAME, ProducedAtUpdatedEvent.VERSION, serialized);

        // then: the deserialized form is correct
        assertThat(deserialized).isEqualTo(event);
    }

    @Test
    void shouldSerializeAndDeserializeDeletedEvent() {
        // when: a deleted event is serialized
        var event = DeletedEvent.of();
        var serialized = serializer.serialize(event);

        // then: the serialized form is correct
        assertThat(serialized).isEmpty();

        // when: the serialized form is deserialized
        var deserialized = serializer.deserialize(DeletedEvent.NAME, DeletedEvent.VERSION, serialized);

        // then: the deserialized form is correct
        assertThat(deserialized).isEqualTo(event);
    }

}
