package de.bennyboer.kicherkrabbe.highlights.persistence.lookup;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.highlights.*;
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
        var highlight = LookupHighlight.of(
                HighlightId.create(),
                Version.zero(),
                ImageId.of("IMAGE_ID"),
                Links.of(Set.of(
                        Link.of(LinkType.PATTERN, LinkId.of("PATTERN_ID"), LinkName.of("Pattern"))
                )),
                false,
                100L,
                Instant.parse("2024-12-10T12:30:00.000Z")
        );

        update(highlight);

        var actualHighlight = findById(highlight.getId());
        assertThat(actualHighlight).isEqualTo(highlight);
    }

    @Test
    void shouldRemoveHighlight() {
        var highlight1 = LookupHighlight.of(
                HighlightId.create(),
                Version.zero(),
                ImageId.of("IMAGE_ID_1"),
                Links.of(Set.of()),
                false,
                100L,
                Instant.parse("2024-12-10T12:30:00.000Z")
        );
        var highlight2 = LookupHighlight.of(
                HighlightId.create(),
                Version.zero(),
                ImageId.of("IMAGE_ID_2"),
                Links.of(Set.of()),
                false,
                200L,
                Instant.parse("2024-12-10T13:30:00.000Z")
        );
        update(highlight1);
        update(highlight2);

        remove(highlight1.getId());

        assertThat(findById(highlight1.getId())).isNull();
        assertThat(findById(highlight2.getId())).isEqualTo(highlight2);
    }

    @Test
    void shouldFindPublishedHighlights() {
        var highlight1 = LookupHighlight.of(
                HighlightId.create(),
                Version.zero(),
                ImageId.of("IMAGE_ID_1"),
                Links.of(Set.of()),
                true,
                200L,
                Instant.parse("2024-12-10T12:30:00.000Z")
        );
        var highlight2 = LookupHighlight.of(
                HighlightId.create(),
                Version.zero(),
                ImageId.of("IMAGE_ID_2"),
                Links.of(Set.of()),
                false,
                100L,
                Instant.parse("2024-12-10T13:30:00.000Z")
        );
        var highlight3 = LookupHighlight.of(
                HighlightId.create(),
                Version.zero(),
                ImageId.of("IMAGE_ID_3"),
                Links.of(Set.of()),
                true,
                50L,
                Instant.parse("2024-12-10T14:30:00.000Z")
        );
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
        var highlight1 = LookupHighlight.of(
                HighlightId.create(),
                Version.zero(),
                ImageId.of("IMAGE_ID_1"),
                Links.of(Set.of()),
                false,
                100L,
                Instant.parse("2024-12-10T12:30:00.000Z")
        );
        var highlight2 = LookupHighlight.of(
                HighlightId.create(),
                Version.zero(),
                ImageId.of("IMAGE_ID_2"),
                Links.of(Set.of()),
                false,
                50L,
                Instant.parse("2024-12-10T13:30:00.000Z")
        );
        var highlight3 = LookupHighlight.of(
                HighlightId.create(),
                Version.zero(),
                ImageId.of("IMAGE_ID_3"),
                Links.of(Set.of()),
                false,
                150L,
                Instant.parse("2024-12-10T14:30:00.000Z")
        );
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
        var highlight1 = LookupHighlight.of(
                HighlightId.create(),
                Version.zero(),
                ImageId.of("IMAGE_ID_1"),
                Links.of(Set.of(
                        Link.of(LinkType.PATTERN, LinkId.of("PATTERN_ID"), LinkName.of("Pattern")),
                        Link.of(LinkType.FABRIC, LinkId.of("FABRIC_ID"), LinkName.of("Fabric"))
                )),
                false,
                100L,
                Instant.parse("2024-12-10T12:30:00.000Z")
        );
        var highlight2 = LookupHighlight.of(
                HighlightId.create(),
                Version.zero(),
                ImageId.of("IMAGE_ID_2"),
                Links.of(Set.of(
                        Link.of(LinkType.PATTERN, LinkId.of("PATTERN_ID"), LinkName.of("Pattern"))
                )),
                false,
                200L,
                Instant.parse("2024-12-10T13:30:00.000Z")
        );
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
