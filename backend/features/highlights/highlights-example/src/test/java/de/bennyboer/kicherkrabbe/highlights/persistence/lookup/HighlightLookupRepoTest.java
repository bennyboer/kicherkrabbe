package de.bennyboer.kicherkrabbe.highlights.persistence.lookup;

import de.bennyboer.kicherkrabbe.highlights.HighlightId;
import de.bennyboer.kicherkrabbe.highlights.LinkId;
import de.bennyboer.kicherkrabbe.highlights.LinkType;
import de.bennyboer.kicherkrabbe.highlights.samples.SampleLink;
import de.bennyboer.kicherkrabbe.highlights.samples.SampleLookupHighlight;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class HighlightLookupRepoTest {

    private HighlightLookupRepo repo;

    protected abstract HighlightLookupRepo createRepo();

    @BeforeEach
    public void setUp() {
        repo = createRepo();
    }

    @Test
    void shouldUpdateHighlight() {
        var highlight = SampleLookupHighlight.builder()
                .link(SampleLink.builder()
                        .type(LinkType.PATTERN)
                        .id("PATTERN_ID")
                        .name("Pattern")
                        .build())
                .build()
                .toValue();

        update(highlight);

        var actualHighlight = findById(highlight.getId());
        assertThat(actualHighlight).isEqualTo(highlight);
    }

    @Test
    void shouldRemoveHighlight() {
        var highlight1 = SampleLookupHighlight.builder()
                .imageId("IMAGE_ID_1")
                .build()
                .toValue();
        var highlight2 = SampleLookupHighlight.builder()
                .imageId("IMAGE_ID_2")
                .sortOrder(200L)
                .createdAt(Instant.parse("2024-12-10T13:30:00.000Z"))
                .build()
                .toValue();
        update(highlight1);
        update(highlight2);

        remove(highlight1.getId());

        assertThat(findById(highlight1.getId())).isNull();
        assertThat(findById(highlight2.getId())).isEqualTo(highlight2);
    }

    @Test
    void shouldFindPublishedHighlights() {
        var highlight1 = SampleLookupHighlight.builder()
                .imageId("IMAGE_ID_1")
                .published(true)
                .sortOrder(200L)
                .build()
                .toValue();
        var highlight2 = SampleLookupHighlight.builder()
                .imageId("IMAGE_ID_2")
                .published(false)
                .createdAt(Instant.parse("2024-12-10T13:30:00.000Z"))
                .build()
                .toValue();
        var highlight3 = SampleLookupHighlight.builder()
                .imageId("IMAGE_ID_3")
                .published(true)
                .sortOrder(50L)
                .createdAt(Instant.parse("2024-12-10T14:30:00.000Z"))
                .build()
                .toValue();
        update(highlight1);
        update(highlight2);
        update(highlight3);

        var published = findPublished();

        assertThat(published).hasSize(2);
        assertThat(published.get(0).getId()).isEqualTo(highlight3.getId());
        assertThat(published.get(1).getId()).isEqualTo(highlight1.getId());
    }

    @Test
    void shouldFindAllHighlightsWithPaging() {
        var highlight1 = SampleLookupHighlight.builder()
                .imageId("IMAGE_ID_1")
                .build()
                .toValue();
        var highlight2 = SampleLookupHighlight.builder()
                .imageId("IMAGE_ID_2")
                .sortOrder(50L)
                .createdAt(Instant.parse("2024-12-10T13:30:00.000Z"))
                .build()
                .toValue();
        var highlight3 = SampleLookupHighlight.builder()
                .imageId("IMAGE_ID_3")
                .sortOrder(150L)
                .createdAt(Instant.parse("2024-12-10T14:30:00.000Z"))
                .build()
                .toValue();
        update(highlight1);
        update(highlight2);
        update(highlight3);

        var page = findAll(Set.of(highlight1.getId(), highlight2.getId(), highlight3.getId()), 0, 10);

        assertThat(page.getTotal()).isEqualTo(3);
        var highlightIds = page.getResults().stream()
                .map(LookupHighlight::getId)
                .toList();
        assertThat(highlightIds).containsExactly(highlight2.getId(), highlight1.getId(), highlight3.getId());

        page = findAll(Set.of(highlight1.getId(), highlight2.getId(), highlight3.getId()), 1, 1);

        assertThat(page.getTotal()).isEqualTo(3);
        highlightIds = page.getResults().stream()
                .map(LookupHighlight::getId)
                .toList();
        assertThat(highlightIds).containsExactly(highlight1.getId());
    }

    @Test
    void shouldFindHighlightsByLink() {
        var highlight1 = SampleLookupHighlight.builder()
                .imageId("IMAGE_ID_1")
                .link(SampleLink.builder()
                        .type(LinkType.PATTERN)
                        .id("PATTERN_ID")
                        .name("Pattern")
                        .build())
                .link(SampleLink.builder()
                        .type(LinkType.FABRIC)
                        .id("FABRIC_ID")
                        .name("Fabric")
                        .build())
                .build()
                .toValue();
        var highlight2 = SampleLookupHighlight.builder()
                .imageId("IMAGE_ID_2")
                .link(SampleLink.builder()
                        .type(LinkType.PATTERN)
                        .id("PATTERN_ID")
                        .name("Pattern")
                        .build())
                .sortOrder(200L)
                .createdAt(Instant.parse("2024-12-10T13:30:00.000Z"))
                .build()
                .toValue();
        update(highlight1);
        update(highlight2);

        var highlights = findByLink(LinkType.PATTERN, LinkId.of("PATTERN_ID"));

        assertThat(highlights).hasSize(2);
        assertThat(highlights).containsExactlyInAnyOrder(highlight1, highlight2);

        highlights = findByLink(LinkType.FABRIC, LinkId.of("FABRIC_ID"));

        assertThat(highlights).hasSize(1);
        assertThat(highlights).containsExactly(highlight1);

        highlights = findByLink(LinkType.FABRIC, LinkId.of("FABRIC_ID_2"));

        assertThat(highlights).isEmpty();
    }

    private void update(LookupHighlight highlight) {
        repo.update(highlight).block();
    }

    private void remove(HighlightId highlightId) {
        repo.remove(highlightId).block();
    }

    private LookupHighlight findById(HighlightId id) {
        return repo.findById(id).block();
    }

    private java.util.List<LookupHighlight> findPublished() {
        return repo.findPublished().collectList().block();
    }

    private java.util.List<LookupHighlight> findByLink(LinkType linkType, LinkId linkId) {
        return repo.findByLink(linkType, linkId).collectList().block();
    }

    private LookupHighlightPage findAll(java.util.Collection<HighlightId> highlightIds, long skip, long limit) {
        return repo.findAll(highlightIds, skip, limit).block();
    }

}
