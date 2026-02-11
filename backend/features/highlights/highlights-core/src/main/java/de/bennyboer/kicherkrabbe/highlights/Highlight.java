package de.bennyboer.kicherkrabbe.highlights;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.Aggregate;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.ApplyCommandResult;
import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.EventMetadata;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.snapshot.SnapshotExclude;
import de.bennyboer.kicherkrabbe.highlights.create.CreateCmd;
import de.bennyboer.kicherkrabbe.highlights.create.CreatedEvent;
import de.bennyboer.kicherkrabbe.highlights.delete.DeleteCmd;
import de.bennyboer.kicherkrabbe.highlights.delete.DeletedEvent;
import de.bennyboer.kicherkrabbe.highlights.image.ImageUpdatedEvent;
import de.bennyboer.kicherkrabbe.highlights.image.UpdateImageCmd;
import de.bennyboer.kicherkrabbe.highlights.links.add.AddLinkCmd;
import de.bennyboer.kicherkrabbe.highlights.links.add.LinkAddedEvent;
import de.bennyboer.kicherkrabbe.highlights.links.remove.LinkRemovedEvent;
import de.bennyboer.kicherkrabbe.highlights.links.remove.RemoveLinkCmd;
import de.bennyboer.kicherkrabbe.highlights.links.update.LinkUpdatedEvent;
import de.bennyboer.kicherkrabbe.highlights.links.update.UpdateLinkCmd;
import de.bennyboer.kicherkrabbe.highlights.publish.AlreadyPublishedError;
import de.bennyboer.kicherkrabbe.highlights.publish.PublishCmd;
import de.bennyboer.kicherkrabbe.highlights.publish.PublishedEvent;
import de.bennyboer.kicherkrabbe.highlights.sort.SortOrderUpdatedEvent;
import de.bennyboer.kicherkrabbe.highlights.sort.UpdateSortOrderCmd;
import de.bennyboer.kicherkrabbe.highlights.unpublish.AlreadyUnpublishedError;
import de.bennyboer.kicherkrabbe.highlights.unpublish.UnpublishCmd;
import de.bennyboer.kicherkrabbe.highlights.unpublish.UnpublishedEvent;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static lombok.AccessLevel.PRIVATE;

@Value
@With(PRIVATE)
@AllArgsConstructor(access = PRIVATE)
public class Highlight implements Aggregate {

    public static final AggregateType TYPE = AggregateType.of("HIGHLIGHT");

    @SnapshotExclude
    HighlightId id;

    @SnapshotExclude
    Version version;

    ImageId imageId;

    Links links;

    boolean published;

    long sortOrder;

    Instant createdAt;

    @Nullable
    Instant deletedAt;

    public static Highlight init() {
        return new Highlight(
                null,
                Version.zero(),
                null,
                Links.of(Set.of()),
                false,
                0L,
                null,
                null
        );
    }

    @Override
    public ApplyCommandResult apply(Command cmd, Agent agent) {
        check(isCreated() || cmd instanceof CreateCmd, "Cannot apply command to not yet created aggregate");
        check(isNotDeleted(), "Cannot apply command to deleted aggregate");

        return switch (cmd) {
            case CreateCmd c -> ApplyCommandResult.of(CreatedEvent.of(
                    c.getImageId(),
                    c.getSortOrder()
            ));
            case UpdateImageCmd c -> ApplyCommandResult.of(ImageUpdatedEvent.of(c.getImageId()));
            case AddLinkCmd c -> ApplyCommandResult.of(LinkAddedEvent.of(c.getLink()));
            case UpdateLinkCmd c -> ApplyCommandResult.of(LinkUpdatedEvent.of(c.getLink()));
            case RemoveLinkCmd c -> ApplyCommandResult.of(LinkRemovedEvent.of(c.getLinkType(), c.getLinkId()));
            case PublishCmd ignored -> {
                if (isPublished()) {
                    throw new AlreadyPublishedError();
                }

                yield ApplyCommandResult.of(PublishedEvent.of());
            }
            case UnpublishCmd ignored -> {
                if (!isPublished()) {
                    throw new AlreadyUnpublishedError();
                }

                yield ApplyCommandResult.of(UnpublishedEvent.of());
            }
            case UpdateSortOrderCmd c -> ApplyCommandResult.of(SortOrderUpdatedEvent.of(c.getSortOrder()));
            case DeleteCmd ignored -> ApplyCommandResult.of(DeletedEvent.of());
            default -> throw new IllegalArgumentException("Unknown command " + cmd.getClass().getSimpleName());
        };
    }

    @Override
    public Aggregate apply(Event event, EventMetadata metadata) {
        HighlightId id = HighlightId.of(metadata.getAggregateId().getValue());
        Version version = metadata.getAggregateVersion();

        return (switch (event) {
            case CreatedEvent e -> withId(id)
                    .withImageId(e.getImageId())
                    .withSortOrder(e.getSortOrder())
                    .withCreatedAt(metadata.getDate());
            case ImageUpdatedEvent e -> withImageId(e.getImageId());
            case LinkAddedEvent e -> withLinks(links.add(e.getLink()));
            case LinkUpdatedEvent e -> withLinks(links.update(e.getLink()));
            case LinkRemovedEvent e -> withLinks(links.remove(e.getLinkType(), e.getLinkId()));
            case PublishedEvent ignored -> withPublished(true);
            case UnpublishedEvent ignored -> withPublished(false);
            case SortOrderUpdatedEvent e -> withSortOrder(e.getSortOrder());
            case DeletedEvent ignored -> withDeletedAt(metadata.getDate());
            default -> throw new IllegalArgumentException("Unknown event " + event.getClass().getSimpleName());
        }).withVersion(version);
    }

    public Optional<Instant> getDeletedAt() {
        return Optional.ofNullable(deletedAt);
    }

    public boolean isDeleted() {
        return getDeletedAt().isPresent();
    }

    public boolean isNotDeleted() {
        return !isDeleted();
    }

    private boolean isCreated() {
        return createdAt != null;
    }

}
