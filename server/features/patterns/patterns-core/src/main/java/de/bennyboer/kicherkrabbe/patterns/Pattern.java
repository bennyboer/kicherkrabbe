package de.bennyboer.kicherkrabbe.patterns;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.Aggregate;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.ApplyCommandResult;
import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.eventsourcing.command.SnapshotCmd;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.EventMetadata;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.patterns.create.CreateCmd;
import de.bennyboer.kicherkrabbe.patterns.create.CreatedEvent;
import de.bennyboer.kicherkrabbe.patterns.delete.DeleteCmd;
import de.bennyboer.kicherkrabbe.patterns.delete.DeletedEvent;
import de.bennyboer.kicherkrabbe.patterns.delete.category.CategoryRemovedEvent;
import de.bennyboer.kicherkrabbe.patterns.delete.category.RemoveCategoryCmd;
import de.bennyboer.kicherkrabbe.patterns.publish.AlreadyPublishedError;
import de.bennyboer.kicherkrabbe.patterns.publish.PublishCmd;
import de.bennyboer.kicherkrabbe.patterns.publish.PublishedEvent;
import de.bennyboer.kicherkrabbe.patterns.rename.RenameCmd;
import de.bennyboer.kicherkrabbe.patterns.rename.RenamedEvent;
import de.bennyboer.kicherkrabbe.patterns.snapshot.SnapshottedEvent;
import de.bennyboer.kicherkrabbe.patterns.unpublish.AlreadyUnpublishedError;
import de.bennyboer.kicherkrabbe.patterns.unpublish.UnpublishCmd;
import de.bennyboer.kicherkrabbe.patterns.unpublish.UnpublishedEvent;
import de.bennyboer.kicherkrabbe.patterns.update.attribution.AttributionUpdatedEvent;
import de.bennyboer.kicherkrabbe.patterns.update.attribution.UpdateAttributionCmd;
import de.bennyboer.kicherkrabbe.patterns.update.categories.CategoriesUpdatedEvent;
import de.bennyboer.kicherkrabbe.patterns.update.categories.UpdateCategoriesCmd;
import de.bennyboer.kicherkrabbe.patterns.update.extras.ExtrasUpdatedEvent;
import de.bennyboer.kicherkrabbe.patterns.update.extras.UpdateExtrasCmd;
import de.bennyboer.kicherkrabbe.patterns.update.images.ImagesUpdatedEvent;
import de.bennyboer.kicherkrabbe.patterns.update.images.UpdateImagesCmd;
import de.bennyboer.kicherkrabbe.patterns.update.variants.UpdateVariantsCmd;
import de.bennyboer.kicherkrabbe.patterns.update.variants.VariantsUpdatedEvent;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static lombok.AccessLevel.PRIVATE;

@Value
@With(PRIVATE)
@AllArgsConstructor(access = PRIVATE)
public class Pattern implements Aggregate {

    public static final AggregateType TYPE = AggregateType.of("PATTERN");

    PatternId id;

    Version version;

    boolean published;

    PatternName name;

    PatternAttribution attribution;

    Set<PatternCategoryId> categories;

    List<ImageId> images;

    List<PatternVariant> variants;

    List<PatternExtra> extras;

    Instant createdAt;

    @Nullable
    Instant deletedAt;

    public static Pattern init() {
        return new Pattern(
                null,
                Version.zero(),
                false,
                null,
                null,
                Set.of(),
                List.of(),
                List.of(),
                List.of(),
                Instant.now(),
                null
        );
    }

    @Override
    public ApplyCommandResult apply(Command cmd, Agent agent) {
        check(isNotDeleted() || cmd instanceof SnapshotCmd, "Cannot apply command to deleted aggregate");

        return switch (cmd) {
            case SnapshotCmd ignored -> ApplyCommandResult.of(SnapshottedEvent.of(
                    isPublished(),
                    getName(),
                    getAttribution(),
                    getCategories(),
                    getImages(),
                    getVariants(),
                    getExtras(),
                    getCreatedAt(),
                    getDeletedAt().orElse(null)
            ));
            case CreateCmd c -> ApplyCommandResult.of(CreatedEvent.of(
                    c.getName(),
                    c.getAttribution(),
                    c.getCategories(),
                    c.getImages(),
                    c.getVariants(),
                    c.getExtras()
            ));
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
            case RenameCmd c -> ApplyCommandResult.of(RenamedEvent.of(c.getName()));
            case UpdateAttributionCmd c -> ApplyCommandResult.of(AttributionUpdatedEvent.of(c.getAttribution()));
            case UpdateCategoriesCmd c -> ApplyCommandResult.of(CategoriesUpdatedEvent.of(c.getCategories()));
            case UpdateImagesCmd c -> ApplyCommandResult.of(ImagesUpdatedEvent.of(c.getImages()));
            case UpdateVariantsCmd c -> ApplyCommandResult.of(VariantsUpdatedEvent.of(c.getVariants()));
            case UpdateExtrasCmd c -> ApplyCommandResult.of(ExtrasUpdatedEvent.of(c.getExtras()));
            case RemoveCategoryCmd c -> ApplyCommandResult.of(CategoryRemovedEvent.of(c.getCategoryId()));
            case DeleteCmd ignored -> ApplyCommandResult.of(DeletedEvent.of());
            default -> throw new IllegalArgumentException("Unknown command " + cmd.getClass().getSimpleName());
        };
    }

    @Override
    public Aggregate apply(Event event, EventMetadata metadata) {
        PatternId id = PatternId.of(metadata.getAggregateId().getValue());
        Version version = metadata.getAggregateVersion();

        return (switch (event) {
            case SnapshottedEvent e -> withId(id)
                    .withName(e.getName())
                    .withAttribution(e.getAttribution())
                    .withCategories(e.getCategories())
                    .withImages(e.getImages())
                    .withVariants(e.getVariants())
                    .withExtras(e.getExtras())
                    .withCreatedAt(e.getCreatedAt())
                    .withDeletedAt(e.getDeletedAt().orElse(null));
            case CreatedEvent e -> withId(id)
                    .withName(e.getName())
                    .withAttribution(e.getAttribution())
                    .withCategories(e.getCategories())
                    .withImages(e.getImages())
                    .withVariants(e.getVariants())
                    .withExtras(e.getExtras())
                    .withCreatedAt(metadata.getDate());
            case PublishedEvent ignored -> withPublished(true);
            case UnpublishedEvent ignored -> withPublished(false);
            case RenamedEvent e -> withName(e.getName());
            case AttributionUpdatedEvent e -> withAttribution(e.getAttribution());
            case CategoriesUpdatedEvent e -> withCategories(e.getCategories());
            case ImagesUpdatedEvent e -> withImages(e.getImages());
            case VariantsUpdatedEvent e -> withVariants(e.getVariants());
            case ExtrasUpdatedEvent e -> withExtras(e.getExtras());
            case CategoryRemovedEvent e -> {
                Set<PatternCategoryId> updatedCategories = new HashSet<>(getCategories());
                updatedCategories.remove(e.getCategoryId());
                yield withCategories(updatedCategories);
            }
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

}
