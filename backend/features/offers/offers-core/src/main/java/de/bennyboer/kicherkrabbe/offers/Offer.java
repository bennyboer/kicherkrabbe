package de.bennyboer.kicherkrabbe.offers;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.Aggregate;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.ApplyCommandResult;
import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.EventMetadata;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.snapshot.SnapshotExclude;
import de.bennyboer.kicherkrabbe.offers.archive.ArchiveCmd;
import de.bennyboer.kicherkrabbe.offers.archive.ArchivedEvent;
import de.bennyboer.kicherkrabbe.offers.archive.NotReservedForArchiveError;
import de.bennyboer.kicherkrabbe.offers.categories.remove.CategoryRemovedEvent;
import de.bennyboer.kicherkrabbe.offers.categories.remove.RemoveCategoryCmd;
import de.bennyboer.kicherkrabbe.offers.categories.update.CategoriesUpdatedEvent;
import de.bennyboer.kicherkrabbe.offers.categories.update.UpdateCategoriesCmd;
import de.bennyboer.kicherkrabbe.offers.create.CreateCmd;
import de.bennyboer.kicherkrabbe.offers.create.CreatedEvent;
import de.bennyboer.kicherkrabbe.offers.delete.CannotDeleteNonDraftError;
import de.bennyboer.kicherkrabbe.offers.delete.DeleteCmd;
import de.bennyboer.kicherkrabbe.offers.delete.DeletedEvent;
import de.bennyboer.kicherkrabbe.offers.discount.add.AddDiscountCmd;
import de.bennyboer.kicherkrabbe.offers.discount.add.DiscountAddedEvent;
import de.bennyboer.kicherkrabbe.offers.discount.remove.DiscountRemovedEvent;
import de.bennyboer.kicherkrabbe.offers.discount.remove.RemoveDiscountCmd;
import de.bennyboer.kicherkrabbe.offers.images.update.ImagesUpdatedEvent;
import de.bennyboer.kicherkrabbe.offers.images.update.UpdateImagesCmd;
import de.bennyboer.kicherkrabbe.offers.notes.update.NotesUpdatedEvent;
import de.bennyboer.kicherkrabbe.offers.notes.update.UpdateNotesCmd;
import de.bennyboer.kicherkrabbe.offers.price.update.PriceUpdatedEvent;
import de.bennyboer.kicherkrabbe.offers.price.update.UpdatePriceCmd;
import de.bennyboer.kicherkrabbe.offers.publish.AlreadyPublishedError;
import de.bennyboer.kicherkrabbe.offers.publish.PublishCmd;
import de.bennyboer.kicherkrabbe.offers.publish.PublishedEvent;
import de.bennyboer.kicherkrabbe.offers.reserve.AlreadyReservedError;
import de.bennyboer.kicherkrabbe.offers.reserve.NotPublishedError;
import de.bennyboer.kicherkrabbe.offers.reserve.ReserveCmd;
import de.bennyboer.kicherkrabbe.offers.reserve.ReservedEvent;
import de.bennyboer.kicherkrabbe.offers.size.update.SizeUpdatedEvent;
import de.bennyboer.kicherkrabbe.offers.size.update.UpdateSizeCmd;
import de.bennyboer.kicherkrabbe.offers.title.update.TitleUpdatedEvent;
import de.bennyboer.kicherkrabbe.offers.title.update.UpdateTitleCmd;
import de.bennyboer.kicherkrabbe.offers.unpublish.AlreadyUnpublishedError;
import de.bennyboer.kicherkrabbe.offers.unpublish.CannotUnpublishReservedError;
import de.bennyboer.kicherkrabbe.offers.unpublish.UnpublishCmd;
import de.bennyboer.kicherkrabbe.offers.unpublish.UnpublishedEvent;
import de.bennyboer.kicherkrabbe.offers.unreserve.NotReservedError;
import de.bennyboer.kicherkrabbe.offers.unreserve.UnreserveCmd;
import de.bennyboer.kicherkrabbe.offers.unreserve.UnreservedEvent;
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
public class Offer implements Aggregate {

    public static final AggregateType TYPE = AggregateType.of("OFFER");

    @SnapshotExclude
    OfferId id;

    @SnapshotExclude
    Version version;

    OfferTitle title;

    OfferSize size;

    Set<OfferCategoryId> categories;

    ProductId productId;

    List<ImageId> images;

    Pricing pricing;

    Notes notes;

    boolean published;

    boolean reserved;

    Instant createdAt;

    @Nullable
    Instant archivedAt;

    @Nullable
    Instant deletedAt;

    public static Offer init() {
        return new Offer(
                null,
                Version.zero(),
                null,
                null,
                Set.of(),
                null,
                List.of(),
                null,
                null,
                false,
                false,
                null,
                null,
                null
        );
    }

    @Override
    public ApplyCommandResult apply(Command cmd, Agent agent) {
        check(isCreated() || cmd instanceof CreateCmd, "Cannot apply command to not yet created aggregate");
        check(isNotDeleted(), "Cannot apply command to deleted aggregate");
        check(isNotArchived(), "Cannot apply command to archived aggregate");

        return switch (cmd) {
            case CreateCmd c -> ApplyCommandResult.of(CreatedEvent.of(
                    c.getTitle(),
                    c.getSize(),
                    c.getCategories(),
                    c.getProductId(),
                    c.getImages(),
                    c.getNotes(),
                    c.getPrice()
            ));
            case DeleteCmd ignored -> {
                if (isPublished() || isReserved()) {
                    throw new CannotDeleteNonDraftError();
                }
                yield ApplyCommandResult.of(DeletedEvent.of());
            }
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
                if (isReserved()) {
                    throw new CannotUnpublishReservedError();
                }
                yield ApplyCommandResult.of(UnpublishedEvent.of());
            }
            case ReserveCmd ignored -> {
                if (!isPublished()) {
                    throw new NotPublishedError();
                }
                if (isReserved()) {
                    throw new AlreadyReservedError();
                }
                yield ApplyCommandResult.of(ReservedEvent.of());
            }
            case UnreserveCmd ignored -> {
                if (!isReserved()) {
                    throw new NotReservedError();
                }
                yield ApplyCommandResult.of(UnreservedEvent.of());
            }
            case ArchiveCmd ignored -> {
                if (!isReserved()) {
                    throw new NotReservedForArchiveError();
                }
                yield ApplyCommandResult.of(ArchivedEvent.of());
            }
            case UpdateImagesCmd c -> ApplyCommandResult.of(ImagesUpdatedEvent.of(c.getImages()));
            case UpdateNotesCmd c -> ApplyCommandResult.of(NotesUpdatedEvent.of(c.getNotes()));
            case UpdatePriceCmd c -> ApplyCommandResult.of(PriceUpdatedEvent.of(c.getPrice()));
            case AddDiscountCmd c -> ApplyCommandResult.of(DiscountAddedEvent.of(c.getDiscountedPrice()));
            case RemoveDiscountCmd ignored -> ApplyCommandResult.of(DiscountRemovedEvent.of());
            case UpdateTitleCmd c -> ApplyCommandResult.of(TitleUpdatedEvent.of(c.getTitle()));
            case UpdateSizeCmd c -> ApplyCommandResult.of(SizeUpdatedEvent.of(c.getSize()));
            case UpdateCategoriesCmd c -> ApplyCommandResult.of(CategoriesUpdatedEvent.of(c.getCategories()));
            case RemoveCategoryCmd c -> {
                if (!categories.contains(c.getCategoryId())) {
                    yield ApplyCommandResult.of();
                }
                yield ApplyCommandResult.of(CategoryRemovedEvent.of(c.getCategoryId()));
            }
            default -> throw new IllegalArgumentException("Unknown command " + cmd.getClass().getSimpleName());
        };
    }

    @Override
    public Aggregate apply(Event event, EventMetadata metadata) {
        OfferId id = OfferId.of(metadata.getAggregateId().getValue());
        Version version = metadata.getAggregateVersion();

        return (switch (event) {
            case CreatedEvent e -> withId(id)
                    .withTitle(e.getTitle())
                    .withSize(e.getSize())
                    .withCategories(e.getCategories())
                    .withProductId(e.getProductId())
                    .withImages(e.getImages())
                    .withPricing(Pricing.of(e.getPrice()))
                    .withNotes(e.getNotes())
                    .withCreatedAt(metadata.getDate());
            case DeletedEvent ignored -> withDeletedAt(metadata.getDate());
            case PublishedEvent ignored -> withPublished(true);
            case UnpublishedEvent ignored -> withPublished(false);
            case ReservedEvent ignored -> withReserved(true);
            case UnreservedEvent ignored -> withReserved(false);
            case ArchivedEvent ignored -> withPublished(false)
                    .withReserved(false)
                    .withArchivedAt(metadata.getDate());
            case ImagesUpdatedEvent e -> withImages(e.getImages());
            case NotesUpdatedEvent e -> withNotes(e.getNotes());
            case PriceUpdatedEvent e -> withPricing(pricing.withUpdatedPrice(e.getPrice(), metadata.getDate()));
            case DiscountAddedEvent e -> withPricing(pricing.withDiscount(e.getDiscountedPrice()));
            case DiscountRemovedEvent ignored -> withPricing(pricing.withoutDiscount());
            case TitleUpdatedEvent e -> withTitle(e.getTitle());
            case SizeUpdatedEvent e -> withSize(e.getSize());
            case CategoriesUpdatedEvent e -> withCategories(e.getCategories());
            case CategoryRemovedEvent e -> {
                var updatedCategories = new HashSet<>(categories);
                updatedCategories.remove(e.getCategoryId());
                yield withCategories(updatedCategories);
            }
            default -> throw new IllegalArgumentException("Unknown event " + event.getClass().getSimpleName());
        }).withVersion(version);
    }

    public Optional<Instant> getDeletedAt() {
        return Optional.ofNullable(deletedAt);
    }

    public Optional<Instant> getArchivedAt() {
        return Optional.ofNullable(archivedAt);
    }

    public boolean isDeleted() {
        return getDeletedAt().isPresent();
    }

    public boolean isNotDeleted() {
        return !isDeleted();
    }

    public boolean isArchived() {
        return getArchivedAt().isPresent();
    }

    public boolean isNotArchived() {
        return !isArchived();
    }

    private boolean isCreated() {
        return createdAt != null;
    }

}
