package de.bennyboer.kicherkrabbe.products.product;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.Aggregate;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.ApplyCommandResult;
import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.eventsourcing.command.SnapshotCmd;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.EventMetadata;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.products.product.create.CreateCmd;
import de.bennyboer.kicherkrabbe.products.product.create.CreatedEvent;
import de.bennyboer.kicherkrabbe.products.product.delete.DeleteCmd;
import de.bennyboer.kicherkrabbe.products.product.delete.DeletedEvent;
import de.bennyboer.kicherkrabbe.products.product.fabric.composition.update.FabricCompositionUpdatedEvent;
import de.bennyboer.kicherkrabbe.products.product.fabric.composition.update.UpdateFabricCompositionCmd;
import de.bennyboer.kicherkrabbe.products.product.images.update.ImagesUpdatedEvent;
import de.bennyboer.kicherkrabbe.products.product.images.update.UpdateImagesCmd;
import de.bennyboer.kicherkrabbe.products.product.links.add.AddLinkCmd;
import de.bennyboer.kicherkrabbe.products.product.links.add.LinkAddedEvent;
import de.bennyboer.kicherkrabbe.products.product.links.remove.LinkRemovedEvent;
import de.bennyboer.kicherkrabbe.products.product.links.remove.RemoveLinkCmd;
import de.bennyboer.kicherkrabbe.products.product.links.update.LinkUpdatedEvent;
import de.bennyboer.kicherkrabbe.products.product.links.update.UpdateLinkCmd;
import de.bennyboer.kicherkrabbe.products.product.notes.update.NotesUpdatedEvent;
import de.bennyboer.kicherkrabbe.products.product.notes.update.UpdateNotesCmd;
import de.bennyboer.kicherkrabbe.products.product.produced.update.ProducedAtUpdatedEvent;
import de.bennyboer.kicherkrabbe.products.product.produced.update.UpdateProducedAtCmd;
import de.bennyboer.kicherkrabbe.products.product.snapshot.SnapshottedEvent;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static lombok.AccessLevel.PRIVATE;

@Value
@With(PRIVATE)
@AllArgsConstructor(access = PRIVATE)
public class Product implements Aggregate {

    public static AggregateType TYPE = AggregateType.of("PRODUCT");

    ProductId id;

    Version version;

    ProductNumber number;

    List<ImageId> images;

    Links links;

    FabricComposition fabricComposition;

    Notes notes;

    Instant producedAt;

    Instant createdAt;

    @Nullable
    Instant deletedAt;

    public static Product init() {
        return new Product(
                null,
                Version.zero(),
                null,
                List.of(),
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    @Override
    public ApplyCommandResult apply(Command cmd, Agent agent) {
        check(isCreated() || cmd instanceof CreateCmd, "Cannot apply command to not yet created aggregate");
        check(isNotDeleted() || cmd instanceof SnapshotCmd, "Cannot apply command to deleted aggregate");

        return switch (cmd) {
            case SnapshotCmd ignored -> ApplyCommandResult.of(SnapshottedEvent.of(
                    getNumber(),
                    getImages(),
                    getLinks(),
                    getFabricComposition(),
                    getNotes(),
                    getProducedAt(),
                    getCreatedAt(),
                    getDeletedAt().orElse(null)
            ));
            case CreateCmd c -> ApplyCommandResult.of(CreatedEvent.of(
                    c.getNumber(),
                    c.getImages(),
                    c.getLinks(),
                    c.getFabricComposition(),
                    c.getNotes(),
                    c.getProducedAt()
            ));
            case DeleteCmd ignored -> ApplyCommandResult.of(DeletedEvent.of());
            case AddLinkCmd c -> ApplyCommandResult.of(LinkAddedEvent.of(c.getLink()));
            case UpdateLinkCmd c -> ApplyCommandResult.of(LinkUpdatedEvent.of(c.getLink()));
            case RemoveLinkCmd c -> ApplyCommandResult.of(LinkRemovedEvent.of(c.getLinkType(), c.getLinkId()));
            case UpdateProducedAtCmd c -> ApplyCommandResult.of(ProducedAtUpdatedEvent.of(c.getProducedAt()));
            case UpdateImagesCmd c -> ApplyCommandResult.of(ImagesUpdatedEvent.of(c.getImages()));
            case UpdateNotesCmd c -> ApplyCommandResult.of(NotesUpdatedEvent.of(c.getNotes()));
            case UpdateFabricCompositionCmd c ->
                    ApplyCommandResult.of(FabricCompositionUpdatedEvent.of(c.getFabricComposition()));
            default -> throw new IllegalArgumentException("Unknown command " + cmd.getClass().getSimpleName());
        };
    }

    @Override
    public Aggregate apply(Event event, EventMetadata metadata) {
        var id = ProductId.of(metadata.getAggregateId().getValue());
        Version version = metadata.getAggregateVersion();

        return (switch (event) {
            case SnapshottedEvent e -> withId(id)
                    .withNumber(e.getNumber())
                    .withImages(e.getImages())
                    .withLinks(e.getLinks())
                    .withFabricComposition(e.getFabricComposition())
                    .withNotes(e.getNotes())
                    .withProducedAt(e.getProducedAt())
                    .withCreatedAt(e.getCreatedAt())
                    .withDeletedAt(e.getDeletedAt().orElse(null));
            case CreatedEvent e -> withId(id)
                    .withNumber(e.getNumber())
                    .withImages(e.getImages())
                    .withLinks(e.getLinks())
                    .withFabricComposition(e.getFabricComposition())
                    .withNotes(e.getNotes())
                    .withProducedAt(e.getProducedAt())
                    .withCreatedAt(metadata.getDate());
            case DeletedEvent ignored -> withDeletedAt(metadata.getDate());
            case LinkAddedEvent e -> withLinks(getLinks().add(e.getLink()));
            case LinkUpdatedEvent e -> withLinks(getLinks().update(e.getLink()));
            case LinkRemovedEvent e -> withLinks(getLinks().remove(e.getLinkType(), e.getLinkId()));
            case ProducedAtUpdatedEvent e -> withProducedAt(e.getProducedAt());
            case ImagesUpdatedEvent e -> withImages(e.getImages());
            case NotesUpdatedEvent e -> withNotes(e.getNotes());
            case FabricCompositionUpdatedEvent e -> withFabricComposition(e.getFabricComposition());
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
