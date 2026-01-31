package de.bennyboer.kicherkrabbe.fabrics.persistence.lookup;

import de.bennyboer.kicherkrabbe.fabrics.*;
import de.bennyboer.kicherkrabbe.fabrics.samples.SampleLookupFabric;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class FabricLookupRepoTest {

    private FabricLookupRepo repo;

    protected abstract FabricLookupRepo createRepo();

    @BeforeEach
    public void setUp() {
        repo = createRepo();
    }

    @Test
    void shouldUpdateFabric() {
        var fabric = SampleLookupFabric.builder()
                .name(FabricName.of("Ice bear party"))
                .image(ImageId.of("ICE_BEAR_IMAGE_ID"))
                .color(ColorId.of("BLUE_ID"))
                .color(ColorId.of("WHITE_ID"))
                .topic(TopicId.of("Winter"))
                .topic(TopicId.of("Animals"))
                .availability(FabricTypeAvailability.of(FabricTypeId.of("JERSEY_ID"), true))
                .availability(FabricTypeAvailability.of(FabricTypeId.of("COTTON_ID"), false))
                .build()
                .toModel();

        update(fabric);

        var fabrics = find(Set.of(fabric.getId()));
        assertThat(fabrics).containsExactly(fabric);
    }

    @Test
    void shouldRemoveFabric() {
        var fabric1 = SampleLookupFabric.builder()
                .name(FabricName.of("Ice bear party"))
                .image(ImageId.of("ICE_BEAR_IMAGE_ID"))
                .color(ColorId.of("BLUE_ID"))
                .color(ColorId.of("WHITE_ID"))
                .topic(TopicId.of("Winter"))
                .topic(TopicId.of("Animals"))
                .availability(FabricTypeAvailability.of(FabricTypeId.of("JERSEY_ID"), true))
                .availability(FabricTypeAvailability.of(FabricTypeId.of("COTTON_ID"), false))
                .published(true)
                .build()
                .toModel();
        var fabric2 = SampleLookupFabric.builder()
                .name(FabricName.of("Colorful"))
                .image(ImageId.of("COLORFUL_IMAGE_ID"))
                .color(ColorId.of("RED_ID"))
                .color(ColorId.of("YELLOW_ID"))
                .topic(TopicId.of("Summer"))
                .topic(TopicId.of("Colors"))
                .availability(FabricTypeAvailability.of(FabricTypeId.of("JERSEY_ID"), false))
                .availability(FabricTypeAvailability.of(FabricTypeId.of("COTTON_ID"), true))
                .build()
                .toModel();
        update(fabric1);
        update(fabric2);

        remove(fabric1.getId());

        var fabrics = find(Set.of(fabric1.getId(), fabric2.getId()));
        assertThat(fabrics).containsExactly(fabric2);
    }

    @Test
    void shouldFindFabrics() {
        var fabric1 = SampleLookupFabric.builder()
                .name(FabricName.of("Ice bear party"))
                .image(ImageId.of("ICE_BEAR_IMAGE_ID"))
                .color(ColorId.of("BLUE_ID"))
                .color(ColorId.of("WHITE_ID"))
                .topic(TopicId.of("Winter"))
                .topic(TopicId.of("Animals"))
                .availability(FabricTypeAvailability.of(FabricTypeId.of("JERSEY_ID"), true))
                .availability(FabricTypeAvailability.of(FabricTypeId.of("COTTON_ID"), false))
                .published(true)
                .createdAt(Instant.parse("2024-03-12T13:00:00.00Z"))
                .build()
                .toModel();
        var fabric2 = SampleLookupFabric.builder()
                .name(FabricName.of("Colorful"))
                .image(ImageId.of("COLORFUL_IMAGE_ID"))
                .color(ColorId.of("RED_ID"))
                .color(ColorId.of("YELLOW_ID"))
                .topic(TopicId.of("Summer"))
                .topic(TopicId.of("Colors"))
                .availability(FabricTypeAvailability.of(FabricTypeId.of("JERSEY_ID"), false))
                .availability(FabricTypeAvailability.of(FabricTypeId.of("COTTON_ID"), true))
                .createdAt(Instant.parse("2024-03-12T12:30:00.00Z"))
                .build()
                .toModel();
        update(fabric1);
        update(fabric2);

        var fabrics = find(Set.of(fabric1.getId(), fabric2.getId()));

        assertThat(fabrics).containsExactly(fabric2, fabric1);
    }

    @Test
    void shouldFindFabricsBySearchTerm() {
        var fabric1 = SampleLookupFabric.builder()
                .name(FabricName.of("Ice bear party"))
                .image(ImageId.of("ICE_BEAR_IMAGE_ID"))
                .color(ColorId.of("BLUE_ID"))
                .color(ColorId.of("WHITE_ID"))
                .topic(TopicId.of("Winter"))
                .topic(TopicId.of("Animals"))
                .availability(FabricTypeAvailability.of(FabricTypeId.of("JERSEY_ID"), true))
                .availability(FabricTypeAvailability.of(FabricTypeId.of("COTTON_ID"), false))
                .published(true)
                .createdAt(Instant.parse("2024-03-12T13:00:00.00Z"))
                .build()
                .toModel();
        var fabric2 = SampleLookupFabric.builder()
                .name(FabricName.of("Colorful"))
                .image(ImageId.of("COLORFUL_IMAGE_ID"))
                .color(ColorId.of("RED_ID"))
                .color(ColorId.of("YELLOW_ID"))
                .topic(TopicId.of("Summer"))
                .topic(TopicId.of("Colors"))
                .availability(FabricTypeAvailability.of(FabricTypeId.of("JERSEY_ID"), false))
                .availability(FabricTypeAvailability.of(FabricTypeId.of("COTTON_ID"), true))
                .createdAt(Instant.parse("2024-03-12T09:30:00.00Z"))
                .build()
                .toModel();
        var fabric3 = SampleLookupFabric.builder()
                .name(FabricName.of("Owls"))
                .image(ImageId.of("OWL_IMAGE_ID"))
                .color(ColorId.of("BROWN_ID"))
                .color(ColorId.of("GREEN_ID"))
                .topic(TopicId.of("Night"))
                .topic(TopicId.of("Animals"))
                .availability(FabricTypeAvailability.of(FabricTypeId.of("JERSEY_ID"), false))
                .availability(FabricTypeAvailability.of(FabricTypeId.of("COTTON_ID"), true))
                .createdAt(Instant.parse("2024-03-12T11:00:00.00Z"))
                .build()
                .toModel();
        update(fabric1);
        update(fabric2);
        update(fabric3);

        var fabricIds = Set.of(fabric1.getId(), fabric2.getId(), fabric3.getId());
        var fabrics = find(fabricIds, "o");
        assertThat(fabrics).containsExactly(fabric2, fabric3);

        fabrics = find(fabricIds, "r");
        assertThat(fabrics).containsExactly(fabric2, fabric1);

        fabrics = find(fabricIds, "    ");
        assertThat(fabrics).containsExactly(fabric2, fabric3, fabric1);

        fabrics = find(fabricIds, "blblblbll");
        assertThat(fabrics).isEmpty();
    }

    @Test
    void shouldFindFabricsWithPaging() {
        var fabric1 = SampleLookupFabric.builder()
                .name(FabricName.of("Ice bear party"))
                .image(ImageId.of("ICE_BEAR_IMAGE_ID"))
                .color(ColorId.of("BLUE_ID"))
                .color(ColorId.of("WHITE_ID"))
                .topic(TopicId.of("Winter"))
                .topic(TopicId.of("Animals"))
                .availability(FabricTypeAvailability.of(FabricTypeId.of("JERSEY_ID"), true))
                .availability(FabricTypeAvailability.of(FabricTypeId.of("COTTON_ID"), false))
                .createdAt(Instant.parse("2024-03-12T13:00:00.00Z"))
                .build()
                .toModel();
        var fabric2 = SampleLookupFabric.builder()
                .name(FabricName.of("Colorful"))
                .image(ImageId.of("COLORFUL_IMAGE_ID"))
                .color(ColorId.of("RED_ID"))
                .color(ColorId.of("YELLOW_ID"))
                .topic(TopicId.of("Summer"))
                .topic(TopicId.of("Colors"))
                .availability(FabricTypeAvailability.of(FabricTypeId.of("JERSEY_ID"), false))
                .availability(FabricTypeAvailability.of(FabricTypeId.of("COTTON_ID"), true))
                .published(true)
                .createdAt(Instant.parse("2024-03-12T09:30:00.00Z"))
                .build()
                .toModel();
        var fabric3 = SampleLookupFabric.builder()
                .name(FabricName.of("Owls"))
                .image(ImageId.of("OWL_IMAGE_ID"))
                .color(ColorId.of("BROWN_ID"))
                .color(ColorId.of("GREEN_ID"))
                .topic(TopicId.of("Night"))
                .topic(TopicId.of("Animals"))
                .availability(FabricTypeAvailability.of(FabricTypeId.of("JERSEY_ID"), false))
                .availability(FabricTypeAvailability.of(FabricTypeId.of("COTTON_ID"), true))
                .published(true)
                .createdAt(Instant.parse("2024-03-12T11:00:00.00Z"))
                .build()
                .toModel();
        update(fabric1);
        update(fabric2);
        update(fabric3);

        var fabricIds = Set.of(fabric1.getId(), fabric2.getId(), fabric3.getId());

        assertThat(find(fabricIds, 1, 1)).containsExactly(fabric3);
        assertThat(find(fabricIds, 2, 1)).containsExactly(fabric1);
        assertThat(find(fabricIds, 3, 1)).isEmpty();
        assertThat(find(fabricIds, 0, 2)).containsExactly(fabric2, fabric3);
    }

    @Test
    void shouldFindWithSearchTermAndPaging() {
        var fabric1 = SampleLookupFabric.builder()
                .name(FabricName.of("Ice bear party"))
                .image(ImageId.of("ICE_BEAR_IMAGE_ID"))
                .color(ColorId.of("BLUE_ID"))
                .color(ColorId.of("WHITE_ID"))
                .topic(TopicId.of("Winter"))
                .topic(TopicId.of("Animals"))
                .availability(FabricTypeAvailability.of(FabricTypeId.of("JERSEY_ID"), true))
                .availability(FabricTypeAvailability.of(FabricTypeId.of("COTTON_ID"), false))
                .published(true)
                .createdAt(Instant.parse("2024-03-12T13:00:00.00Z"))
                .build()
                .toModel();
        var fabric2 = SampleLookupFabric.builder()
                .name(FabricName.of("Colorful"))
                .image(ImageId.of("COLORFUL_IMAGE_ID"))
                .color(ColorId.of("RED_ID"))
                .color(ColorId.of("YELLOW_ID"))
                .topic(TopicId.of("Summer"))
                .topic(TopicId.of("Colors"))
                .availability(FabricTypeAvailability.of(FabricTypeId.of("JERSEY_ID"), false))
                .availability(FabricTypeAvailability.of(FabricTypeId.of("COTTON_ID"), true))
                .published(true)
                .createdAt(Instant.parse("2024-03-12T09:30:00.00Z"))
                .build()
                .toModel();
        var fabric3 = SampleLookupFabric.builder()
                .name(FabricName.of("Owls"))
                .image(ImageId.of("OWL_IMAGE_ID"))
                .color(ColorId.of("BROWN_ID"))
                .color(ColorId.of("GREEN_ID"))
                .topic(TopicId.of("Night"))
                .topic(TopicId.of("Animals"))
                .availability(FabricTypeAvailability.of(FabricTypeId.of("JERSEY_ID"), false))
                .availability(FabricTypeAvailability.of(FabricTypeId.of("COTTON_ID"), true))
                .createdAt(Instant.parse("2024-03-12T11:00:00.00Z"))
                .build()
                .toModel();
        update(fabric1);
        update(fabric2);
        update(fabric3);

        var fabricIds = Set.of(fabric1.getId(), fabric2.getId(), fabric3.getId());
        var page = findPage(fabricIds, "r", 0, 1);
        assertThat(page.getResults()).containsExactly(fabric2);
        assertThat(page.getTotal()).isEqualTo(2);

        page = findPage(fabricIds, "color", 1, 1);
        assertThat(page.getResults()).isEmpty();
        assertThat(page.getTotal()).isEqualTo(1);
    }

    @Test
    void shouldFindPublishedFabric() {
        var fabric1 = SampleLookupFabric.builder()
                .name(FabricName.of("Ice bear party"))
                .image(ImageId.of("ICE_BEAR_IMAGE_ID"))
                .color(ColorId.of("BLUE_ID"))
                .color(ColorId.of("WHITE_ID"))
                .topic(TopicId.of("Winter"))
                .topic(TopicId.of("Animals"))
                .availability(FabricTypeAvailability.of(FabricTypeId.of("JERSEY_ID"), true))
                .availability(FabricTypeAvailability.of(FabricTypeId.of("COTTON_ID"), false))
                .published(true)
                .createdAt(Instant.parse("2024-03-12T13:00:00.00Z"))
                .build()
                .toModel();
        var fabric2 = SampleLookupFabric.builder()
                .name(FabricName.of("Colorful"))
                .image(ImageId.of("COLORFUL_IMAGE_ID"))
                .color(ColorId.of("RED_ID"))
                .color(ColorId.of("YELLOW_ID"))
                .topic(TopicId.of("Summer"))
                .topic(TopicId.of("Colors"))
                .availability(FabricTypeAvailability.of(FabricTypeId.of("JERSEY_ID"), false))
                .availability(FabricTypeAvailability.of(FabricTypeId.of("COTTON_ID"), true))
                .createdAt(Instant.parse("2024-03-12T09:30:00.00Z"))
                .build()
                .toModel();
        update(fabric1);
        update(fabric2);

        assertThat(findPublished(fabric1.getId())).isEqualTo(fabric1);
        assertThat(findPublished(fabric2.getId())).isNull();
    }

    @Test
    void shouldFindPublishedFabrics() {
        var fabric1 = SampleLookupFabric.builder()
                .name(FabricName.of("Ice bear party"))
                .image(ImageId.of("ICE_BEAR_IMAGE_ID"))
                .color(ColorId.of("BLUE_ID"))
                .color(ColorId.of("WHITE_ID"))
                .topic(TopicId.of("Winter"))
                .topic(TopicId.of("Animals"))
                .availability(FabricTypeAvailability.of(FabricTypeId.of("JERSEY_ID"), true))
                .availability(FabricTypeAvailability.of(FabricTypeId.of("COTTON_ID"), false))
                .published(true)
                .createdAt(Instant.parse("2024-03-12T13:00:00.00Z"))
                .build()
                .toModel();
        var fabric2 = SampleLookupFabric.builder()
                .name(FabricName.of("Colorful"))
                .image(ImageId.of("COLORFUL_IMAGE_ID"))
                .color(ColorId.of("RED_ID"))
                .color(ColorId.of("YELLOW_ID"))
                .topic(TopicId.of("Summer"))
                .topic(TopicId.of("Colors"))
                .availability(FabricTypeAvailability.of(FabricTypeId.of("JERSEY_ID"), false))
                .availability(FabricTypeAvailability.of(FabricTypeId.of("COTTON_ID"), false))
                .published(true)
                .createdAt(Instant.parse("2024-03-12T09:30:00.00Z"))
                .build()
                .toModel();
        var fabric3 = SampleLookupFabric.builder()
                .name(FabricName.of("Owls"))
                .image(ImageId.of("OWL_IMAGE_ID"))
                .color(ColorId.of("BROWN_ID"))
                .color(ColorId.of("GREEN_ID"))
                .topic(TopicId.of("Night"))
                .topic(TopicId.of("Animals"))
                .availability(FabricTypeAvailability.of(FabricTypeId.of("JERSEY_ID"), false))
                .availability(FabricTypeAvailability.of(FabricTypeId.of("COTTON_ID"), true))
                .createdAt(Instant.parse("2024-03-12T11:00:00.00Z"))
                .build()
                .toModel();
        update(fabric1);
        update(fabric2);
        update(fabric3);

        var result = findPublished("", Set.of(), Set.of(), false, false, true, 0, 10);
        assertThat(result.getResults()).containsExactly(fabric2, fabric1);

        result = findPublished("", Set.of(), Set.of(), false, false, false, 0, 10);
        assertThat(result.getResults()).containsExactly(fabric1, fabric2);

        result = findPublished("o", Set.of(), Set.of(), false, false, true, 0, 10);
        assertThat(result.getResults()).containsExactly(fabric2);

        result = findPublished("", Set.of(ColorId.of("BLUE_ID")), Set.of(), false, false, true, 0, 10);
        assertThat(result.getResults()).containsExactly(fabric1);

        result = findPublished("", Set.of(), Set.of(TopicId.of("Summer")), false, false, true, 0, 10);
        assertThat(result.getResults()).containsExactly(fabric2);

        result = findPublished("", Set.of(), Set.of(), true, true, true, 0, 10);
        assertThat(result.getResults()).containsExactly(fabric1);

        result = findPublished("", Set.of(), Set.of(), true, false, true, 0, 10);
        assertThat(result.getResults()).containsExactly(fabric2);

        result = findPublished("", Set.of(), Set.of(), false, false, true, 0, 1);
        assertThat(result.getResults()).containsExactly(fabric2);

        result = findPublished("", Set.of(), Set.of(), false, false, true, 1, 1);
        assertThat(result.getResults()).containsExactly(fabric1);
    }

    @Test
    void shouldFindFabricsByColor() {
        var colorId1 = ColorId.of("COLOR_ID_1");
        var colorId2 = ColorId.of("COLOR_ID_2");
        var colorId3 = ColorId.of("COLOR_ID_3");

        var fabric1 = SampleLookupFabric.builder()
                .name(FabricName.of("Fabric 1"))
                .image(ImageId.of("IMAGE_ID_1"))
                .color(colorId1)
                .published(true)
                .createdAt(Instant.parse("2024-03-12T13:00:00.00Z"))
                .build()
                .toModel();
        var fabric2 = SampleLookupFabric.builder()
                .name(FabricName.of("Fabric 2"))
                .image(ImageId.of("IMAGE_ID_2"))
                .color(colorId2)
                .published(true)
                .createdAt(Instant.parse("2024-03-12T09:30:00.00Z"))
                .build()
                .toModel();
        var fabric3 = SampleLookupFabric.builder()
                .name(FabricName.of("Fabric 3"))
                .image(ImageId.of("IMAGE_ID_3"))
                .color(colorId3)
                .color(colorId2)
                .published(true)
                .createdAt(Instant.parse("2024-03-12T11:00:00.00Z"))
                .build()
                .toModel();
        update(fabric1);
        update(fabric2);
        update(fabric3);

        assertThat(findByColor(colorId2)).containsExactlyInAnyOrder(fabric2, fabric3);
        assertThat(findByColor(colorId1)).containsExactly(fabric1);
        assertThat(findByColor(ColorId.of("COLOR_ID_4"))).isEmpty();
    }

    @Test
    void shouldFindFabricsByTopic() {
        var topicId1 = TopicId.of("TOPIC_ID_1");
        var topicId2 = TopicId.of("TOPIC_ID_2");
        var topicId3 = TopicId.of("TOPIC_ID_3");

        var fabric1 = SampleLookupFabric.builder()
                .name(FabricName.of("Fabric 1"))
                .image(ImageId.of("IMAGE_ID_1"))
                .topic(topicId1)
                .published(true)
                .createdAt(Instant.parse("2024-03-12T13:00:00.00Z"))
                .build()
                .toModel();
        var fabric2 = SampleLookupFabric.builder()
                .name(FabricName.of("Fabric 2"))
                .image(ImageId.of("IMAGE_ID_2"))
                .topic(topicId2)
                .published(true)
                .createdAt(Instant.parse("2024-03-12T09:30:00.00Z"))
                .build()
                .toModel();
        var fabric3 = SampleLookupFabric.builder()
                .name(FabricName.of("Fabric 3"))
                .image(ImageId.of("IMAGE_ID_3"))
                .topic(topicId3)
                .topic(topicId2)
                .published(true)
                .createdAt(Instant.parse("2024-03-12T11:00:00.00Z"))
                .build()
                .toModel();
        update(fabric1);
        update(fabric2);
        update(fabric3);

        assertThat(findByTopic(topicId2)).containsExactlyInAnyOrder(fabric2, fabric3);
        assertThat(findByTopic(topicId1)).containsExactly(fabric1);
        assertThat(findByTopic(TopicId.of("TOPIC_ID_4"))).isEmpty();
    }

    @Test
    void shouldFindFabricsByFabricType() {
        var fabricTypeId1 = FabricTypeId.of("FABRIC_TYPE_ID_1");
        var fabricTypeId2 = FabricTypeId.of("FABRIC_TYPE_ID_2");
        var fabricTypeId3 = FabricTypeId.of("FABRIC_TYPE_ID_3");

        var fabric1 = SampleLookupFabric.builder()
                .name(FabricName.of("Fabric 1"))
                .image(ImageId.of("IMAGE_ID_1"))
                .availability(FabricTypeAvailability.of(fabricTypeId1, true))
                .published(true)
                .createdAt(Instant.parse("2024-03-12T13:00:00.00Z"))
                .build()
                .toModel();
        var fabric2 = SampleLookupFabric.builder()
                .name(FabricName.of("Fabric 2"))
                .image(ImageId.of("IMAGE_ID_2"))
                .availability(FabricTypeAvailability.of(fabricTypeId2, true))
                .published(true)
                .createdAt(Instant.parse("2024-03-12T09:30:00.00Z"))
                .build()
                .toModel();
        var fabric3 = SampleLookupFabric.builder()
                .name(FabricName.of("Fabric 3"))
                .image(ImageId.of("IMAGE_ID_3"))
                .availability(FabricTypeAvailability.of(fabricTypeId2, true))
                .availability(FabricTypeAvailability.of(fabricTypeId3, true))
                .published(true)
                .createdAt(Instant.parse("2024-03-12T11:00:00.00Z"))
                .build()
                .toModel();
        update(fabric1);
        update(fabric2);
        update(fabric3);

        assertThat(findByFabricType(fabricTypeId2)).containsExactlyInAnyOrder(fabric2, fabric3);
        assertThat(findByFabricType(fabricTypeId1)).containsExactly(fabric1);
        assertThat(findByFabricType(FabricTypeId.of("FABRIC_TYPE_ID_4"))).isEmpty();
    }

    @Test
    void shouldFindUniqueColors() {
        var colorId1 = ColorId.of("COLOR_ID_1");
        var colorId2 = ColorId.of("COLOR_ID_2");
        var colorId3 = ColorId.of("COLOR_ID_3");

        var fabric1 = SampleLookupFabric.builder()
                .name(FabricName.of("Fabric 1"))
                .image(ImageId.of("IMAGE_ID_1"))
                .color(colorId1)
                .published(true)
                .createdAt(Instant.parse("2024-03-12T13:00:00.00Z"))
                .build()
                .toModel();
        var fabric2 = SampleLookupFabric.builder()
                .name(FabricName.of("Fabric 2"))
                .image(ImageId.of("IMAGE_ID_2"))
                .color(colorId2)
                .published(true)
                .createdAt(Instant.parse("2024-03-12T09:30:00.00Z"))
                .build()
                .toModel();
        var fabric3 = SampleLookupFabric.builder()
                .name(FabricName.of("Fabric 3"))
                .image(ImageId.of("IMAGE_ID_3"))
                .color(colorId3)
                .color(colorId2)
                .published(true)
                .createdAt(Instant.parse("2024-03-12T11:00:00.00Z"))
                .build()
                .toModel();
        update(fabric1);
        update(fabric2);
        update(fabric3);

        assertThat(findUniqueColors()).containsExactlyInAnyOrder(colorId1, colorId2, colorId3);

        remove(fabric1.getId());
        remove(fabric2.getId());
        remove(fabric3.getId());
        assertThat(findUniqueColors()).isEmpty();
    }

    @Test
    void shouldFindUniqueTopics() {
        var topicId1 = TopicId.of("TOPIC_ID_1");
        var topicId2 = TopicId.of("TOPIC_ID_2");
        var topicId3 = TopicId.of("TOPIC_ID_3");

        var fabric1 = SampleLookupFabric.builder()
                .name(FabricName.of("Fabric 1"))
                .image(ImageId.of("IMAGE_ID_1"))
                .topic(topicId1)
                .published(true)
                .createdAt(Instant.parse("2024-03-12T13:00:00.00Z"))
                .build()
                .toModel();
        var fabric2 = SampleLookupFabric.builder()
                .name(FabricName.of("Fabric 2"))
                .image(ImageId.of("IMAGE_ID_2"))
                .topic(topicId2)
                .published(true)
                .createdAt(Instant.parse("2024-03-12T09:30:00.00Z"))
                .build()
                .toModel();
        var fabric3 = SampleLookupFabric.builder()
                .name(FabricName.of("Fabric 3"))
                .image(ImageId.of("IMAGE_ID_3"))
                .topic(topicId3)
                .topic(topicId2)
                .published(true)
                .createdAt(Instant.parse("2024-03-12T11:00:00.00Z"))
                .build()
                .toModel();
        update(fabric1);
        update(fabric2);
        update(fabric3);

        assertThat(findUniqueTopics()).containsExactlyInAnyOrder(topicId1, topicId2, topicId3);

        remove(fabric1.getId());
        remove(fabric2.getId());
        remove(fabric3.getId());
        assertThat(findUniqueTopics()).isEmpty();
    }

    @Test
    void shouldFindUniqueFabricTypes() {
        var fabricTypeId1 = FabricTypeId.of("FABRIC_TYPE_ID_1");
        var fabricTypeId2 = FabricTypeId.of("FABRIC_TYPE_ID_2");
        var fabricTypeId3 = FabricTypeId.of("FABRIC_TYPE_ID_3");

        var fabric1 = SampleLookupFabric.builder()
                .name(FabricName.of("Fabric 1"))
                .image(ImageId.of("IMAGE_ID_1"))
                .availability(FabricTypeAvailability.of(fabricTypeId1, true))
                .published(true)
                .createdAt(Instant.parse("2024-03-12T13:00:00.00Z"))
                .build()
                .toModel();
        var fabric2 = SampleLookupFabric.builder()
                .name(FabricName.of("Fabric 2"))
                .image(ImageId.of("IMAGE_ID_2"))
                .availability(FabricTypeAvailability.of(fabricTypeId2, true))
                .published(true)
                .createdAt(Instant.parse("2024-03-12T09:30:00.00Z"))
                .build()
                .toModel();
        var fabric3 = SampleLookupFabric.builder()
                .name(FabricName.of("Fabric 3"))
                .image(ImageId.of("IMAGE_ID_3"))
                .availability(FabricTypeAvailability.of(fabricTypeId2, true))
                .availability(FabricTypeAvailability.of(fabricTypeId3, true))
                .published(true)
                .createdAt(Instant.parse("2024-03-12T11:00:00.00Z"))
                .build()
                .toModel();
        update(fabric1);
        update(fabric2);
        update(fabric3);

        assertThat(findUniqueFabricTypes()).containsExactlyInAnyOrder(fabricTypeId1, fabricTypeId2, fabricTypeId3);

        remove(fabric1.getId());
        remove(fabric2.getId());
        remove(fabric3.getId());
        assertThat(findUniqueFabricTypes()).isEmpty();
    }

    private List<ColorId> findUniqueColors() {
        return repo.findUniqueColors().collectList().block();
    }

    private List<TopicId> findUniqueTopics() {
        return repo.findUniqueTopics().collectList().block();
    }

    private List<FabricTypeId> findUniqueFabricTypes() {
        return repo.findUniqueFabricTypes().collectList().block();
    }

    private List<LookupFabric> findByColor(ColorId colorId) {
        return repo.findByColor(colorId).collectList().block();
    }

    private List<LookupFabric> findByTopic(TopicId topicId) {
        return repo.findByTopic(topicId).collectList().block();
    }

    private List<LookupFabric> findByFabricType(FabricTypeId fabricTypeId) {
        return repo.findByFabricType(fabricTypeId).collectList().block();
    }

    private LookupFabric findPublished(FabricId id) {
        return repo.findPublished(id).block();
    }

    private LookupFabricPage findPublished(
            String searchTerm,
            Set<ColorId> colors,
            Set<TopicId> topics,
            boolean filterAvailability,
            boolean inStock,
            boolean ascending,
            long skip,
            long limit
    ) {
        return repo.findPublished(searchTerm, colors, topics, filterAvailability, inStock, ascending, skip, limit).block();
    }

    private void update(LookupFabric fabric) {
        repo.update(fabric).block();
    }

    private void remove(FabricId fabricId) {
        repo.remove(fabricId).block();
    }

    private List<LookupFabric> find(Collection<FabricId> fabricIds) {
        return find(fabricIds, "", 0, Integer.MAX_VALUE);
    }

    private List<LookupFabric> find(Collection<FabricId> fabricIds, String searchTerm) {
        return find(fabricIds, searchTerm, 0, Integer.MAX_VALUE);
    }

    private List<LookupFabric> find(Collection<FabricId> fabricIds, long skip, long limit) {
        return find(fabricIds, "", skip, limit);
    }

    private List<LookupFabric> find(Collection<FabricId> fabricIds, String searchTerm, long skip, long limit) {
        return repo.find(fabricIds, searchTerm, skip, limit).block().getResults();
    }

    private LookupFabricPage findPage(Collection<FabricId> fabricIds, String searchTerm, long skip, long limit) {
        return repo.find(fabricIds, searchTerm, skip, limit).block();
    }

}
