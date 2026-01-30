package de.bennyboer.kicherkrabbe.patterns;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.Aggregate;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.ApplyCommandResult;
import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.EventMetadata;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.snapshot.SnapshotExclude;
import de.bennyboer.kicherkrabbe.patterns.create.CreateCmd;
import de.bennyboer.kicherkrabbe.patterns.create.CreatedEvent;
import de.bennyboer.kicherkrabbe.patterns.delete.DeleteCmd;
import de.bennyboer.kicherkrabbe.patterns.delete.DeletedEvent;
import de.bennyboer.kicherkrabbe.patterns.delete.category.CategoryRemovedEvent;
import de.bennyboer.kicherkrabbe.patterns.delete.category.RemoveCategoryCmd;
import de.bennyboer.kicherkrabbe.patterns.feature.AlreadyFeaturedError;
import de.bennyboer.kicherkrabbe.patterns.feature.FeatureCmd;
import de.bennyboer.kicherkrabbe.patterns.feature.FeaturedEvent;
import de.bennyboer.kicherkrabbe.patterns.publish.AlreadyPublishedError;
import de.bennyboer.kicherkrabbe.patterns.publish.PublishCmd;
import de.bennyboer.kicherkrabbe.patterns.publish.PublishedEvent;
import de.bennyboer.kicherkrabbe.patterns.rename.RenameCmd;
import de.bennyboer.kicherkrabbe.patterns.rename.RenamedEvent;
import de.bennyboer.kicherkrabbe.patterns.unfeature.AlreadyUnfeaturedError;
import de.bennyboer.kicherkrabbe.patterns.unfeature.UnfeatureCmd;
import de.bennyboer.kicherkrabbe.patterns.unfeature.UnfeaturedEvent;
import de.bennyboer.kicherkrabbe.patterns.unpublish.AlreadyUnpublishedError;
import de.bennyboer.kicherkrabbe.patterns.unpublish.UnpublishCmd;
import de.bennyboer.kicherkrabbe.patterns.unpublish.UnpublishedEvent;
import de.bennyboer.kicherkrabbe.patterns.update.attribution.AttributionUpdatedEvent;
import de.bennyboer.kicherkrabbe.patterns.update.attribution.UpdateAttributionCmd;
import de.bennyboer.kicherkrabbe.patterns.update.categories.CategoriesUpdatedEvent;
import de.bennyboer.kicherkrabbe.patterns.update.categories.UpdateCategoriesCmd;
import de.bennyboer.kicherkrabbe.patterns.update.description.DescriptionUpdatedEvent;
import de.bennyboer.kicherkrabbe.patterns.update.description.UpdateDescriptionCmd;
import de.bennyboer.kicherkrabbe.patterns.update.extras.ExtrasUpdatedEvent;
import de.bennyboer.kicherkrabbe.patterns.update.extras.UpdateExtrasCmd;
import de.bennyboer.kicherkrabbe.patterns.update.images.ImagesUpdatedEvent;
import de.bennyboer.kicherkrabbe.patterns.update.images.UpdateImagesCmd;
import de.bennyboer.kicherkrabbe.patterns.update.number.NumberUpdatedEvent;
import de.bennyboer.kicherkrabbe.patterns.update.number.UpdateNumberCmd;
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

    @SnapshotExclude
    PatternId id;

    @SnapshotExclude
    Version version;

    boolean published;

    boolean featured;

    PatternName name;

    PatternNumber number;

    @Nullable
    PatternDescription description;

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
                false,
                null,
                null,
                null,
                null,
                Set.of(),
                List.of(),
                List.of(),
                List.of(),
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
                    c.getName(),
                    c.getNumber(),
                    c.getDescription().orElse(null),
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
            case UpdateNumberCmd c -> ApplyCommandResult.of(NumberUpdatedEvent.of(c.getNumber()));
            case UpdateAttributionCmd c -> ApplyCommandResult.of(AttributionUpdatedEvent.of(c.getAttribution()));
            case UpdateCategoriesCmd c -> ApplyCommandResult.of(CategoriesUpdatedEvent.of(c.getCategories()));
            case UpdateImagesCmd c -> ApplyCommandResult.of(ImagesUpdatedEvent.of(c.getImages()));
            case UpdateVariantsCmd c -> ApplyCommandResult.of(VariantsUpdatedEvent.of(c.getVariants()));
            case UpdateExtrasCmd c -> ApplyCommandResult.of(ExtrasUpdatedEvent.of(c.getExtras()));
            case UpdateDescriptionCmd c -> ApplyCommandResult.of(DescriptionUpdatedEvent.of(
                    c.getDescription().orElse(null)
            ));
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
            case CreatedEvent e -> withId(id)
                    .withName(e.getName())
                    .withNumber(e.getNumber())
                    .withDescription(e.getDescription().orElse(null))
                    .withAttribution(e.getAttribution())
                    .withCategories(e.getCategories())
                    .withImages(e.getImages())
                    .withVariants(e.getVariants())
                    .withExtras(e.getExtras())
                    .withCreatedAt(metadata.getDate());
            case PublishedEvent ignored -> withPublished(true);
            case UnpublishedEvent ignored -> withPublished(false);
            case FeaturedEvent ignored -> withFeatured(true);
            case UnfeaturedEvent ignored -> withFeatured(false);
            case RenamedEvent e -> withName(e.getName());
            case NumberUpdatedEvent e -> withNumber(e.getNumber());
            case AttributionUpdatedEvent e -> withAttribution(e.getAttribution());
            case CategoriesUpdatedEvent e -> withCategories(e.getCategories());
            case ImagesUpdatedEvent e -> withImages(e.getImages());
            case VariantsUpdatedEvent e -> withVariants(e.getVariants());
            case ExtrasUpdatedEvent e -> withExtras(e.getExtras());
            case DescriptionUpdatedEvent e -> withDescription(e.getDescription().orElse(null));
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

    public Optional<PatternDescription> getDescription() {
        return Optional.ofNullable(description);
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
