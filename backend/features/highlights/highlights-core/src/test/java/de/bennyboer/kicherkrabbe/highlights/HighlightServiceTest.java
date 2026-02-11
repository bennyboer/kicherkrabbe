package de.bennyboer.kicherkrabbe.highlights;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.LoggingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.inmemory.InMemoryEventSourcingRepo;
import de.bennyboer.kicherkrabbe.highlights.publish.AlreadyPublishedError;
import de.bennyboer.kicherkrabbe.highlights.samples.SampleHighlight;
import de.bennyboer.kicherkrabbe.highlights.samples.SampleLink;
import de.bennyboer.kicherkrabbe.highlights.unpublish.AlreadyUnpublishedError;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.util.Set;

import static de.bennyboer.kicherkrabbe.highlights.LinkType.FABRIC;
import static de.bennyboer.kicherkrabbe.highlights.LinkType.PATTERN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class HighlightServiceTest {

    private final EventSourcingRepo repo = new InMemoryEventSourcingRepo();

    private final LoggingEventPublisher eventPublisher = new LoggingEventPublisher();

    private final HighlightService highlightService = new HighlightService(repo, eventPublisher, Clock.systemUTC());

    @Test
    void shouldCreateHighlight() {
        var id = create(ImageId.of("IMAGE_ID"), 100L);

        var highlight = get(id);
        assertThat(highlight.getId()).isEqualTo(id);
        assertThat(highlight.getVersion()).isEqualTo(Version.zero());
        assertThat(highlight.getImageId()).isEqualTo(ImageId.of("IMAGE_ID"));
        assertThat(highlight.getSortOrder()).isEqualTo(100L);
        assertThat(highlight.isPublished()).isFalse();
        assertThat(highlight.getLinks()).isEqualTo(Links.of(Set.of()));
        assertThat(highlight.isDeleted()).isFalse();
    }

    @Test
    void shouldNotAllowAddingLinkToNotYetCreatedHighlight() {
        var id = HighlightId.of("HIGHLIGHT_ID");

        assertThatThrownBy(() -> addLink(id, Version.zero(), Link.of(
                PATTERN,
                LinkId.of("PATTERN_ID"),
                LinkName.of("Pattern")
        ))).matches(e -> e instanceof IllegalArgumentException && e.getMessage()
                .equals("Cannot apply command to not yet created aggregate"));
    }

    @Test
    void shouldAddLink() {
        var id = create(ImageId.of("IMAGE_ID"), 0L);

        var version = addLink(id, Version.zero(), Link.of(
                PATTERN,
                LinkId.of("PATTERN_ID"),
                LinkName.of("Pattern")
        ));

        var highlight = get(id);
        assertThat(highlight.getVersion()).isEqualTo(version);
        assertThat(highlight.getLinks()).isEqualTo(Links.of(Set.of(
                Link.of(PATTERN, LinkId.of("PATTERN_ID"), LinkName.of("Pattern"))
        )));
    }

    @Test
    void shouldNotAddLinkGivenAnOutdatedVersion() {
        var id = create(ImageId.of("IMAGE_ID"), 0L);
        addLink(id, Version.zero(), Link.of(
                PATTERN,
                LinkId.of("PATTERN_ID"),
                LinkName.of("Pattern")
        ));

        assertThatThrownBy(() -> addLink(id, Version.zero(), Link.of(
                PATTERN,
                LinkId.of("PATTERN_ID_2"),
                LinkName.of("Pattern 2")
        ))).matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldUpdateLink() {
        var id = create(ImageId.of("IMAGE_ID"), 0L);
        var version = addLink(id, Version.zero(), Link.of(
                PATTERN,
                LinkId.of("PATTERN_ID"),
                LinkName.of("Pattern")
        ));

        version = updateLink(id, version, Link.of(
                PATTERN,
                LinkId.of("PATTERN_ID"),
                LinkName.of("New name")
        ));

        var highlight = get(id);
        assertThat(highlight.getVersion()).isEqualTo(version);
        assertThat(highlight.getLinks()).isEqualTo(Links.of(Set.of(
                Link.of(PATTERN, LinkId.of("PATTERN_ID"), LinkName.of("New name"))
        )));
    }

    @Test
    void shouldNotUpdateLinkGivenAnOutdatedVersion() {
        var id = create(ImageId.of("IMAGE_ID"), 0L);
        var version = addLink(id, Version.zero(), Link.of(
                PATTERN,
                LinkId.of("PATTERN_ID"),
                LinkName.of("Pattern")
        ));
        updateLink(id, version, Link.of(
                PATTERN,
                LinkId.of("PATTERN_ID"),
                LinkName.of("New name")
        ));

        assertThatThrownBy(() -> updateLink(id, version, Link.of(
                PATTERN,
                LinkId.of("PATTERN_ID"),
                LinkName.of("New name 2")
        ))).matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldRemoveLink() {
        var id = create(ImageId.of("IMAGE_ID"), 0L);
        var version = addLink(id, Version.zero(), Link.of(
                PATTERN,
                LinkId.of("PATTERN_ID"),
                LinkName.of("Pattern")
        ));
        version = addLink(id, version, Link.of(
                FABRIC,
                LinkId.of("FABRIC_ID"),
                LinkName.of("Fabric")
        ));

        version = removeLink(id, version, PATTERN, LinkId.of("PATTERN_ID"));

        var highlight = get(id);
        assertThat(highlight.getVersion()).isEqualTo(version);
        assertThat(highlight.getLinks()).isEqualTo(Links.of(Set.of(
                Link.of(FABRIC, LinkId.of("FABRIC_ID"), LinkName.of("Fabric"))
        )));
    }

    @Test
    void shouldNotRemoveLinkGivenAnOutdatedVersion() {
        var id = create(ImageId.of("IMAGE_ID"), 0L);
        var version = addLink(id, Version.zero(), Link.of(
                PATTERN,
                LinkId.of("PATTERN_ID"),
                LinkName.of("Pattern")
        ));
        removeLink(id, version, PATTERN, LinkId.of("PATTERN_ID"));

        assertThatThrownBy(() -> removeLink(id, version, PATTERN, LinkId.of("PATTERN_ID")))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldUpdateImage() {
        var id = create(ImageId.of("IMAGE_ID"), 0L);

        var version = updateImage(id, Version.zero(), ImageId.of("NEW_IMAGE_ID"));

        var highlight = get(id);
        assertThat(highlight.getVersion()).isEqualTo(version);
        assertThat(highlight.getImageId()).isEqualTo(ImageId.of("NEW_IMAGE_ID"));
    }

    @Test
    void shouldNotUpdateImageGivenAnOutdatedVersion() {
        var id = create(ImageId.of("IMAGE_ID"), 0L);
        updateImage(id, Version.zero(), ImageId.of("NEW_IMAGE_ID"));

        assertThatThrownBy(() -> updateImage(id, Version.zero(), ImageId.of("ANOTHER_IMAGE_ID")))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldPublishHighlight() {
        var id = create(ImageId.of("IMAGE_ID"), 0L);

        var version = publish(id, Version.zero());

        var highlight = get(id);
        assertThat(highlight.getVersion()).isEqualTo(version);
        assertThat(highlight.isPublished()).isTrue();
    }

    @Test
    void shouldNotPublishAlreadyPublishedHighlight() {
        var id = create(ImageId.of("IMAGE_ID"), 0L);
        var version = publish(id, Version.zero());

        assertThatThrownBy(() -> publish(id, version))
                .isInstanceOf(AlreadyPublishedError.class);
    }

    @Test
    void shouldUnpublishHighlight() {
        var id = create(ImageId.of("IMAGE_ID"), 0L);
        var version = publish(id, Version.zero());

        version = unpublish(id, version);

        var highlight = get(id);
        assertThat(highlight.getVersion()).isEqualTo(version);
        assertThat(highlight.isPublished()).isFalse();
    }

    @Test
    void shouldNotUnpublishAlreadyUnpublishedHighlight() {
        var id = create(ImageId.of("IMAGE_ID"), 0L);

        assertThatThrownBy(() -> unpublish(id, Version.zero()))
                .isInstanceOf(AlreadyUnpublishedError.class);
    }

    @Test
    void shouldUpdateSortOrder() {
        var id = create(ImageId.of("IMAGE_ID"), 0L);

        var version = updateSortOrder(id, Version.zero(), 50L);

        var highlight = get(id);
        assertThat(highlight.getVersion()).isEqualTo(version);
        assertThat(highlight.getSortOrder()).isEqualTo(50L);
    }

    @Test
    void shouldNotUpdateSortOrderGivenAnOutdatedVersion() {
        var id = create(ImageId.of("IMAGE_ID"), 0L);
        updateSortOrder(id, Version.zero(), 50L);

        assertThatThrownBy(() -> updateSortOrder(id, Version.zero(), 100L))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldDeleteHighlight() {
        var id = create(ImageId.of("IMAGE_ID"), 0L);

        var version = delete(id, Version.zero());

        var highlight = get(id);
        assertThat(highlight).isNull();

        assertThatThrownBy(() -> updateImage(id, version, ImageId.of("NEW_IMAGE_ID")))
                .matches(e -> e instanceof IllegalArgumentException && e.getMessage()
                        .equals("Cannot apply command to deleted aggregate"));
    }

    @Test
    void shouldNotDeleteHighlightGivenAnOutdatedVersion() {
        var id = create(ImageId.of("IMAGE_ID"), 0L);
        addLink(id, Version.zero(), Link.of(
                PATTERN,
                LinkId.of("PATTERN_ID"),
                LinkName.of("Pattern")
        ));

        assertThatThrownBy(() -> delete(id, Version.zero()))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldSnapshotEvery100Events() {
        var id = create(ImageId.of("IMAGE_ID"), 0L);

        var version = Version.zero();
        for (int i = 0; i < 200; i++) {
            version = updateSortOrder(id, version, i);
        }

        var highlight = get(id);
        assertThat(highlight.getVersion()).isEqualTo(Version.of(202));
        assertThat(highlight.getSortOrder()).isEqualTo(199L);

        var events = repo.findEventsByAggregateIdAndType(
                AggregateId.of(id.getValue()),
                Highlight.TYPE,
                Version.zero()
        ).collectList().block();
        var snapshotEvents = events.stream().filter(e -> e.getMetadata().isSnapshot()).toList();
        assertThat(snapshotEvents).hasSize(2);
        assertThat(snapshotEvents.getFirst().getMetadata().getAggregateVersion()).isEqualTo(Version.of(100));
        assertThat(snapshotEvents.getLast().getMetadata().getAggregateVersion()).isEqualTo(Version.of(200));
    }

    private HighlightId create(ImageId imageId, long sortOrder) {
        return highlightService.create(imageId, sortOrder, Agent.system()).block().getId();
    }

    private HighlightId create(SampleHighlight sample) {
        return create(sample.getImageId(), sample.getSortOrder());
    }

    private HighlightId createSampleHighlight() {
        return create(SampleHighlight.builder().build());
    }

    private Highlight get(HighlightId id) {
        return highlightService.get(id).block();
    }

    private Version addLink(HighlightId id, Version version, Link link) {
        return highlightService.addLink(id, version, link, Agent.system()).block();
    }

    private Version updateLink(HighlightId id, Version version, Link link) {
        return highlightService.updateLink(id, version, link, Agent.system()).block();
    }

    private Version removeLink(HighlightId id, Version version, LinkType linkType, LinkId linkId) {
        return highlightService.removeLink(id, version, linkType, linkId, Agent.system()).block();
    }

    private Version updateImage(HighlightId id, Version version, ImageId imageId) {
        return highlightService.updateImage(id, version, imageId, Agent.system()).block();
    }

    private Version publish(HighlightId id, Version version) {
        return highlightService.publish(id, version, Agent.system()).block();
    }

    private Version unpublish(HighlightId id, Version version) {
        return highlightService.unpublish(id, version, Agent.system()).block();
    }

    private Version updateSortOrder(HighlightId id, Version version, long sortOrder) {
        return highlightService.updateSortOrder(id, version, sortOrder, Agent.system()).block();
    }

    private Version delete(HighlightId id, Version version) {
        return highlightService.delete(id, version, Agent.system()).block();
    }

}
