package de.bennyboer.kicherkrabbe.embroideries;

import de.bennyboer.kicherkrabbe.embroideries.create.CreateCmd;
import de.bennyboer.kicherkrabbe.embroideries.create.CreatedEvent;
import de.bennyboer.kicherkrabbe.embroideries.delete.DeleteCmd;
import de.bennyboer.kicherkrabbe.embroideries.delete.DeletedEvent;
import de.bennyboer.kicherkrabbe.embroideries.delete.category.CategoryRemovedEvent;
import de.bennyboer.kicherkrabbe.embroideries.delete.category.RemoveCategoryCmd;
import de.bennyboer.kicherkrabbe.embroideries.feature.AlreadyFeaturedError;
import de.bennyboer.kicherkrabbe.embroideries.feature.FeatureCmd;
import de.bennyboer.kicherkrabbe.embroideries.feature.FeaturedEvent;
import de.bennyboer.kicherkrabbe.embroideries.publish.AlreadyPublishedError;
import de.bennyboer.kicherkrabbe.embroideries.publish.PublishCmd;
import de.bennyboer.kicherkrabbe.embroideries.publish.PublishedEvent;
import de.bennyboer.kicherkrabbe.embroideries.rename.RenameCmd;
import de.bennyboer.kicherkrabbe.embroideries.rename.RenamedEvent;
import de.bennyboer.kicherkrabbe.embroideries.unfeature.AlreadyUnfeaturedError;
import de.bennyboer.kicherkrabbe.embroideries.unfeature.UnfeatureCmd;
import de.bennyboer.kicherkrabbe.embroideries.unfeature.UnfeaturedEvent;
import de.bennyboer.kicherkrabbe.embroideries.unpublish.AlreadyUnpublishedError;
import de.bennyboer.kicherkrabbe.embroideries.unpublish.UnpublishCmd;
import de.bennyboer.kicherkrabbe.embroideries.unpublish.UnpublishedEvent;
import de.bennyboer.kicherkrabbe.embroideries.update.categories.CategoriesUpdatedEvent;
import de.bennyboer.kicherkrabbe.embroideries.update.categories.UpdateCategoriesCmd;
import de.bennyboer.kicherkrabbe.embroideries.update.image.ImageUpdatedEvent;
import de.bennyboer.kicherkrabbe.embroideries.update.image.UpdateImageCmd;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.Aggregate;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.ApplyCommandResult;
import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.EventMetadata;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.snapshot.SnapshotExclude;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static lombok.AccessLevel.PRIVATE;

@Value
@With(PRIVATE)
@AllArgsConstructor(access = PRIVATE)
public class Embroidery implements Aggregate {

    public static final AggregateType TYPE = AggregateType.of("EMBROIDERY");

    @SnapshotExclude
    EmbroideryId id;

    @SnapshotExclude
    Version version;

    EmbroideryName name;

    ImageId image;

    Set<EmbroideryCategoryId> categories;

    boolean published;

    boolean featured;

    Instant createdAt;

    @Nullable
    Instant deletedAt;

    public static Embroidery init() {
        return new Embroidery(
                null,
                Version.zero(),
                null,
                null,
                Set.of(),
                false,
                false,
                null,
                null
        );
    }

    @Override
    public ApplyCommandResult apply(Command cmd, Agent agent) {
        check(isCreated() || cmd instanceof CreateCmd, "Cannot apply command to not yet created aggregate");
        check(isNotDeleted(), "Cannot apply command to deleted aggregate");

        return switch (cmd) {
            case CreateCmd c -> ApplyCommandResult.of(CreatedEvent.of(c.getName(), c.getImage(), c.getCategories()));
            case DeleteCmd ignored -> ApplyCommandResult.of(DeletedEvent.of());
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
            case FeatureCmd ignored -> {
                if (isFeatured()) {
                    throw new AlreadyFeaturedError();
                }

                yield ApplyCommandResult.of(FeaturedEvent.of());
            }
            case UnfeatureCmd ignored -> {
                if (!isFeatured()) {
                    throw new AlreadyUnfeaturedError();
                }

                yield ApplyCommandResult.of(UnfeaturedEvent.of());
            }
            case RenameCmd c -> ApplyCommandResult.of(RenamedEvent.of(c.getName()));
            case UpdateImageCmd c -> ApplyCommandResult.of(ImageUpdatedEvent.of(c.getImage()));
            case UpdateCategoriesCmd c -> ApplyCommandResult.of(CategoriesUpdatedEvent.of(c.getCategories()));
            case RemoveCategoryCmd c -> {
                check(getCategories().contains(c.getCategoryId()), "Category to remove is not in embroidery");
                yield ApplyCommandResult.of(CategoryRemovedEvent.of(c.getCategoryId()));
            }
            default -> throw new IllegalArgumentException("Unknown command " + cmd.getClass().getSimpleName());
        };
    }

    @Override
    public Aggregate apply(Event event, EventMetadata metadata) {
        EmbroideryId id = EmbroideryId.of(metadata.getAggregateId().getValue());
        Version version = metadata.getAggregateVersion();

        return (switch (event) {
            case CreatedEvent e -> withId(id)
                    .withName(e.getName())
                    .withImage(e.getImage())
                    .withCategories(e.getCategories())
                    .withCreatedAt(metadata.getDate());
            case DeletedEvent ignored -> withDeletedAt(metadata.getDate());
            case PublishedEvent ignored -> withPublished(true);
            case UnpublishedEvent ignored -> withPublished(false);
            case FeaturedEvent ignored -> withFeatured(true);
            case UnfeaturedEvent ignored -> withFeatured(false);
            case RenamedEvent e -> withName(e.getName());
            case ImageUpdatedEvent e -> withImage(e.getImage());
            case CategoriesUpdatedEvent e -> withCategories(e.getCategories());
            case CategoryRemovedEvent e -> {
                Set<EmbroideryCategoryId> updatedCategories = new HashSet<>(getCategories());
                updatedCategories.remove(e.getCategoryId());
                yield withCategories(updatedCategories);
            }
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
