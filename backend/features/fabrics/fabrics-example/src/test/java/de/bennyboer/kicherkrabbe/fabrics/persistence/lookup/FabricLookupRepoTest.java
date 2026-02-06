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
        // given: a fabric to update
        var fabric = SampleLookupFabric.builder().build().toModel();

        // when: updating the fabric
        update(fabric);

        // then: the fabric is updated
        var fabrics = find(Set.of(fabric.getId()));
        assertThat(fabrics).containsExactly(fabric);
    }

    @Test
    void shouldRemoveFabric() {
        // given: some fabrics
        var fabric1 = SampleLookupFabric.builder().build().toModel();
        var fabric2 = SampleLookupFabric.builder().build().toModel();
        update(fabric1);
        update(fabric2);

        // when: removing a fabric
        remove(fabric1.getId());

        // then: the fabric is removed
        var fabrics = find(Set.of(fabric1.getId(), fabric2.getId()));
        assertThat(fabrics).containsExactly(fabric2);
    }

    @Test
    void shouldFindFabrics() {
        // given: some fabrics
        var fabric1 = SampleLookupFabric.builder()
                .createdAt(Instant.parse("2024-03-12T13:00:00.00Z"))
                .build()
                .toModel();
        var fabric2 = SampleLookupFabric.builder()
                .createdAt(Instant.parse("2024-03-12T12:30:00.00Z"))
                .build()
                .toModel();
        update(fabric1);
        update(fabric2);

        // when: finding fabrics
        var fabrics = find(Set.of(fabric1.getId(), fabric2.getId()));

        // then: the fabrics are found sorted by creation date
        assertThat(fabrics).containsExactly(fabric2, fabric1);
    }

    @Test
    void shouldFindFabricsBySearchTerm() {
        // given: some fabrics with different names
        var fabric1 = SampleLookupFabric.builder()
                .name(FabricName.of("Ice bear party"))
                .createdAt(Instant.parse("2024-03-12T13:00:00.00Z"))
                .build()
                .toModel();
        var fabric2 = SampleLookupFabric.builder()
                .name(FabricName.of("Colorful"))
                .createdAt(Instant.parse("2024-03-12T09:30:00.00Z"))
                .build()
                .toModel();
        var fabric3 = SampleLookupFabric.builder()
                .name(FabricName.of("Owls"))
                .createdAt(Instant.parse("2024-03-12T11:00:00.00Z"))
                .build()
                .toModel();
        update(fabric1);
        update(fabric2);
        update(fabric3);

        // when: finding fabrics by search term
        var fabricIds = Set.of(fabric1.getId(), fabric2.getId(), fabric3.getId());
        var fabrics = find(fabricIds, "o");

        // then: the fabrics are found by search term
        assertThat(fabrics).containsExactly(fabric2, fabric3);

        // when: finding fabrics by another search term
        fabrics = find(fabricIds, "r");

        // then: the fabrics are found by another search term
        assertThat(fabrics).containsExactly(fabric2, fabric1);

        // when: finding fabrics by blank search term
        fabrics = find(fabricIds, "    ");

        // then: all fabrics are found
        assertThat(fabrics).containsExactly(fabric2, fabric3, fabric1);

        // when: finding fabrics by non-matching search term
        fabrics = find(fabricIds, "blblblbll");

        // then: no fabrics are found
        assertThat(fabrics).isEmpty();
    }

    @Test
    void shouldFindFabricsWithPaging() {
        // given: some fabrics
        var fabric1 = SampleLookupFabric.builder()
                .createdAt(Instant.parse("2024-03-12T13:00:00.00Z"))
                .build()
                .toModel();
        var fabric2 = SampleLookupFabric.builder()
                .createdAt(Instant.parse("2024-03-12T09:30:00.00Z"))
                .build()
                .toModel();
        var fabric3 = SampleLookupFabric.builder()
                .createdAt(Instant.parse("2024-03-12T11:00:00.00Z"))
                .build()
                .toModel();
        update(fabric1);
        update(fabric2);
        update(fabric3);

        // when: finding fabrics with paging
        var fabricIds = Set.of(fabric1.getId(), fabric2.getId(), fabric3.getId());

        // then: the fabrics are found with paging
        assertThat(find(fabricIds, 1, 1)).containsExactly(fabric3);
        assertThat(find(fabricIds, 2, 1)).containsExactly(fabric1);
        assertThat(find(fabricIds, 3, 1)).isEmpty();
        assertThat(find(fabricIds, 0, 2)).containsExactly(fabric2, fabric3);
    }

    @Test
    void shouldFindWithSearchTermAndPaging() {
        // given: some fabrics with different names
        var fabric1 = SampleLookupFabric.builder()
                .name(FabricName.of("Ice bear party"))
                .createdAt(Instant.parse("2024-03-12T13:00:00.00Z"))
                .build()
                .toModel();
        var fabric2 = SampleLookupFabric.builder()
                .name(FabricName.of("Colorful"))
                .createdAt(Instant.parse("2024-03-12T09:30:00.00Z"))
                .build()
                .toModel();
        var fabric3 = SampleLookupFabric.builder()
                .name(FabricName.of("Owls"))
                .createdAt(Instant.parse("2024-03-12T11:00:00.00Z"))
                .build()
                .toModel();
        update(fabric1);
        update(fabric2);
        update(fabric3);

        // when: finding fabrics with search term and paging
        var fabricIds = Set.of(fabric1.getId(), fabric2.getId(), fabric3.getId());
        var page = findPage(fabricIds, "r", 0, 1);

        // then: the fabrics are found with search term and paging
        assertThat(page.getResults()).containsExactly(fabric2);
        assertThat(page.getTotal()).isEqualTo(2);

        // when: finding fabrics with search term and paging
        page = findPage(fabricIds, "color", 1, 1);

        // then: the fabrics are found with search term and paging
        assertThat(page.getResults()).isEmpty();
        assertThat(page.getTotal()).isEqualTo(1);
    }

    @Test
    void shouldFindPublishedFabric() {
        // given: a published and an unpublished fabric
        var fabric1 = SampleLookupFabric.builder().published(true).build().toModel();
        var fabric2 = SampleLookupFabric.builder().published(false).build().toModel();
        update(fabric1);
        update(fabric2);

        // when: finding the published fabric
        var foundFabric1 = findPublished(fabric1.getId());

        // then: the published fabric is found
        assertThat(foundFabric1).isEqualTo(fabric1);

        // when: finding the unpublished fabric
        var foundFabric2 = findPublished(fabric2.getId());

        // then: the unpublished fabric is not found
        assertThat(foundFabric2).isNull();
    }

    @Test
    void shouldFindPublishedFabricByAlias() {
        // given: a published and an unpublished fabric with different aliases
        var fabric1 = SampleLookupFabric.builder()
                .alias(FabricAlias.of("ice-bear-party"))
                .published(true)
                .build()
                .toModel();
        var fabric2 = SampleLookupFabric.builder()
                .alias(FabricAlias.of("colorful-meadow"))
                .published(false)
                .build()
                .toModel();
        update(fabric1);
        update(fabric2);

        // when: finding the published fabric by alias
        var foundFabric1 = findPublishedByAlias(FabricAlias.of("ice-bear-party"));

        // then: the published fabric is found
        assertThat(foundFabric1).isEqualTo(fabric1);

        // when: finding the unpublished fabric by alias
        var foundFabric2 = findPublishedByAlias(FabricAlias.of("colorful-meadow"));

        // then: the unpublished fabric is not found
        assertThat(foundFabric2).isNull();

        // when: finding a fabric by a non-existing alias
        var foundFabric3 = findPublishedByAlias(FabricAlias.of("non-existing"));

        // then: no fabric is found
        assertThat(foundFabric3).isNull();
    }

    @Test
    void shouldFindPublishedFabrics() {
        // given: some fabrics with different names and availability
        var fabric1 = SampleLookupFabric.builder()
                .name(FabricName.of("Ice bear party"))
                .availability(FabricTypeAvailability.of(FabricTypeId.of("JERSEY_ID"), true))
                .published(true)
                .createdAt(Instant.parse("2024-03-12T13:00:00.00Z"))
                .build()
                .toModel();
        var fabric2 = SampleLookupFabric.builder()
                .name(FabricName.of("Colorful"))
                .availability(FabricTypeAvailability.of(FabricTypeId.of("JERSEY_ID"), false))
                .published(true)
                .createdAt(Instant.parse("2024-03-12T09:30:00.00Z"))
                .build()
                .toModel();
        var fabric3 = SampleLookupFabric.builder()
                .name(FabricName.of("Owls"))
                .published(false)
                .createdAt(Instant.parse("2024-03-12T11:00:00.00Z"))
                .build()
                .toModel();
        update(fabric1);
        update(fabric2);
        update(fabric3);

        // when: finding all published fabrics ordered by name ascending
        var result = findPublished("", Set.of(), Set.of(), false, false, true, 0, 10);

        // then: all published fabrics are found ordered by name ascending
        assertThat(result.getResults()).containsExactly(fabric2, fabric1);

        // when: finding all published fabrics ordered by name descending
        result = findPublished("", Set.of(), Set.of(), false, false, false, 0, 10);

        // then: all published fabrics are found ordered by name descending
        assertThat(result.getResults()).containsExactly(fabric1, fabric2);

        // when: finding published fabrics with search term
        result = findPublished("o", Set.of(), Set.of(), false, false, true, 0, 10);

        // then: all published fabrics are found with search term
        assertThat(result.getResults()).containsExactly(fabric2);

        // when: finding published fabrics with availability filter (in stock)
        result = findPublished("", Set.of(), Set.of(), true, true, true, 0, 10);

        // then: all published fabrics are found with availability filter (in stock)
        assertThat(result.getResults()).containsExactly(fabric1);

        // when: finding published fabrics with availability filter (not in stock)
        result = findPublished("", Set.of(), Set.of(), true, false, true, 0, 10);

        // then: all published fabrics are found with availability filter (not in stock)
        assertThat(result.getResults()).containsExactly(fabric2);

        // when: finding published fabrics with paging
        result = findPublished("", Set.of(), Set.of(), false, false, true, 0, 1);

        // then: all published fabrics are found with paging
        assertThat(result.getResults()).containsExactly(fabric2);

        // when: finding published fabrics with paging
        result = findPublished("", Set.of(), Set.of(), false, false, true, 1, 1);

        // then: all published fabrics are found with paging
        assertThat(result.getResults()).containsExactly(fabric1);
    }

    @Test
    void shouldFindPublishedFabricsByColor() {
        // given: some published fabrics with different colors
        var colorId1 = ColorId.of("COLOR_ID_1");
        var colorId2 = ColorId.of("COLOR_ID_2");

        var fabric1 = SampleLookupFabric.builder()
                .name(FabricName.of("A"))
                .color(colorId1)
                .published(true)
                .build()
                .toModel();
        var fabric2 = SampleLookupFabric.builder()
                .name(FabricName.of("B"))
                .color(colorId2)
                .published(true)
                .build()
                .toModel();
        update(fabric1);
        update(fabric2);

        // when: finding published fabrics with colors filter
        var result = findPublished("", Set.of(colorId1), Set.of(), false, false, true, 0, 10);

        // then: only fabrics with the specified color are found
        assertThat(result.getResults()).containsExactly(fabric1);
    }

    @Test
    void shouldFindPublishedFabricsByTopic() {
        // given: some published fabrics with different topics
        var topicId1 = TopicId.of("TOPIC_ID_1");
        var topicId2 = TopicId.of("TOPIC_ID_2");

        var fabric1 = SampleLookupFabric.builder()
                .name(FabricName.of("A"))
                .topic(topicId1)
                .published(true)
                .build()
                .toModel();
        var fabric2 = SampleLookupFabric.builder()
                .name(FabricName.of("B"))
                .topic(topicId2)
                .published(true)
                .build()
                .toModel();
        update(fabric1);
        update(fabric2);

        // when: finding published fabrics with topics filter
        var result = findPublished("", Set.of(), Set.of(topicId1), false, false, true, 0, 10);

        // then: only fabrics with the specified topic are found
        assertThat(result.getResults()).containsExactly(fabric1);
    }

    @Test
    void shouldFindFabricsByColor() {
        // given: some fabrics with different colors
        var colorId1 = ColorId.of("COLOR_ID_1");
        var colorId2 = ColorId.of("COLOR_ID_2");

        var fabric1 = SampleLookupFabric.builder().color(colorId1).build().toModel();
        var fabric2 = SampleLookupFabric.builder().color(colorId2).build().toModel();
        var fabric3 = SampleLookupFabric.builder().color(colorId2).color(colorId1).build().toModel();
        update(fabric1);
        update(fabric2);
        update(fabric3);

        // when: finding fabrics by color
        var fabrics = findByColor(colorId2);

        // then: the fabrics are found by color
        assertThat(fabrics).containsExactlyInAnyOrder(fabric2, fabric3);

        // when: finding fabrics by another color
        fabrics = findByColor(colorId1);

        // then: the fabrics are found by another color
        assertThat(fabrics).containsExactlyInAnyOrder(fabric1, fabric3);

        // when: finding fabrics by a color that is not used
        fabrics = findByColor(ColorId.of("COLOR_ID_4"));

        // then: no fabrics are found
        assertThat(fabrics).isEmpty();
    }

    @Test
    void shouldFindFabricsByTopic() {
        // given: some fabrics with different topics
        var topicId1 = TopicId.of("TOPIC_ID_1");
        var topicId2 = TopicId.of("TOPIC_ID_2");

        var fabric1 = SampleLookupFabric.builder().topic(topicId1).build().toModel();
        var fabric2 = SampleLookupFabric.builder().topic(topicId2).build().toModel();
        var fabric3 = SampleLookupFabric.builder().topic(topicId2).topic(topicId1).build().toModel();
        update(fabric1);
        update(fabric2);
        update(fabric3);

        // when: finding fabrics by topic
        var fabrics = findByTopic(topicId2);

        // then: the fabrics are found by topic
        assertThat(fabrics).containsExactlyInAnyOrder(fabric2, fabric3);

        // when: finding fabrics by another topic
        fabrics = findByTopic(topicId1);

        // then: the fabrics are found by another topic
        assertThat(fabrics).containsExactlyInAnyOrder(fabric1, fabric3);

        // when: finding fabrics by a topic that is not used
        fabrics = findByTopic(TopicId.of("TOPIC_ID_4"));

        // then: no fabrics are found
        assertThat(fabrics).isEmpty();
    }

    @Test
    void shouldFindFabricsByFabricType() {
        // given: some fabrics with different fabric types
        var fabricTypeId1 = FabricTypeId.of("FABRIC_TYPE_ID_1");
        var fabricTypeId2 = FabricTypeId.of("FABRIC_TYPE_ID_2");

        var fabric1 = SampleLookupFabric.builder()
                .availability(FabricTypeAvailability.of(fabricTypeId1, true))
                .build()
                .toModel();
        var fabric2 = SampleLookupFabric.builder()
                .availability(FabricTypeAvailability.of(fabricTypeId2, true))
                .build()
                .toModel();
        var fabric3 = SampleLookupFabric.builder()
                .availability(FabricTypeAvailability.of(fabricTypeId2, true))
                .availability(FabricTypeAvailability.of(fabricTypeId1, true))
                .build()
                .toModel();
        update(fabric1);
        update(fabric2);
        update(fabric3);

        // when: finding fabrics by fabric type
        var fabrics = findByFabricType(fabricTypeId2);

        // then: the fabrics are found by fabric type
        assertThat(fabrics).containsExactlyInAnyOrder(fabric2, fabric3);

        // when: finding fabrics by another fabric type
        fabrics = findByFabricType(fabricTypeId1);

        // then: the fabrics are found by another fabric type
        assertThat(fabrics).containsExactlyInAnyOrder(fabric1, fabric3);

        // when: finding fabrics by a fabric type that is not used
        fabrics = findByFabricType(FabricTypeId.of("FABRIC_TYPE_ID_4"));

        // then: no fabrics are found
        assertThat(fabrics).isEmpty();
    }

    @Test
    void shouldFindUniqueColors() {
        // given: some fabrics with different colors
        var colorId1 = ColorId.of("COLOR_ID_1");
        var colorId2 = ColorId.of("COLOR_ID_2");

        var fabric1 = SampleLookupFabric.builder().color(colorId1).build().toModel();
        var fabric2 = SampleLookupFabric.builder().color(colorId2).build().toModel();
        var fabric3 = SampleLookupFabric.builder().color(colorId2).color(colorId1).build().toModel();
        update(fabric1);
        update(fabric2);
        update(fabric3);

        // when: finding unique colors
        var colors = findUniqueColors();

        // then: the unique colors are found
        assertThat(colors).containsExactlyInAnyOrder(colorId1, colorId2);

        // when: removing all fabrics and finding unique colors
        remove(fabric1.getId());
        remove(fabric2.getId());
        remove(fabric3.getId());
        colors = findUniqueColors();

        // then: no unique colors are found
        assertThat(colors).isEmpty();
    }

    @Test
    void shouldFindUniqueTopics() {
        // given: some fabrics with different topics
        var topicId1 = TopicId.of("TOPIC_ID_1");
        var topicId2 = TopicId.of("TOPIC_ID_2");

        var fabric1 = SampleLookupFabric.builder().topic(topicId1).build().toModel();
        var fabric2 = SampleLookupFabric.builder().topic(topicId2).build().toModel();
        var fabric3 = SampleLookupFabric.builder().topic(topicId2).topic(topicId1).build().toModel();
        update(fabric1);
        update(fabric2);
        update(fabric3);

        // when: finding unique topics
        var topics = findUniqueTopics();

        // then: the unique topics are found
        assertThat(topics).containsExactlyInAnyOrder(topicId1, topicId2);

        // when: removing all fabrics and finding unique topics
        remove(fabric1.getId());
        remove(fabric2.getId());
        remove(fabric3.getId());
        topics = findUniqueTopics();

        // then: no unique topics are found
        assertThat(topics).isEmpty();
    }

    @Test
    void shouldFindUniqueFabricTypes() {
        // given: some fabrics with different fabric types
        var fabricTypeId1 = FabricTypeId.of("FABRIC_TYPE_ID_1");
        var fabricTypeId2 = FabricTypeId.of("FABRIC_TYPE_ID_2");

        var fabric1 = SampleLookupFabric.builder()
                .availability(FabricTypeAvailability.of(fabricTypeId1, true))
                .build()
                .toModel();
        var fabric2 = SampleLookupFabric.builder()
                .availability(FabricTypeAvailability.of(fabricTypeId2, true))
                .build()
                .toModel();
        var fabric3 = SampleLookupFabric.builder()
                .availability(FabricTypeAvailability.of(fabricTypeId2, true))
                .availability(FabricTypeAvailability.of(fabricTypeId1, true))
                .build()
                .toModel();
        update(fabric1);
        update(fabric2);
        update(fabric3);

        // when: finding unique fabric types
        var fabricTypes = findUniqueFabricTypes();

        // then: the unique fabric types are found
        assertThat(fabricTypes).containsExactlyInAnyOrder(fabricTypeId1, fabricTypeId2);

        // when: removing all fabrics and finding unique fabric types
        remove(fabric1.getId());
        remove(fabric2.getId());
        remove(fabric3.getId());
        fabricTypes = findUniqueFabricTypes();

        // then: no unique fabric types are found
        assertThat(fabricTypes).isEmpty();
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

    private LookupFabric findPublishedByAlias(FabricAlias alias) {
        return repo.findPublishedByAlias(alias).block();
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
