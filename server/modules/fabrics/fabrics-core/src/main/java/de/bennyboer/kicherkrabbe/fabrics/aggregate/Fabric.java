package de.bennyboer.kicherkrabbe.fabrics.aggregate;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.Aggregate;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.ApplyCommandResult;
import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.eventsourcing.command.SnapshotCmd;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.EventMetadata;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.fabrics.aggregate.create.CreateCmd;
import de.bennyboer.kicherkrabbe.fabrics.aggregate.create.CreatedEvent;
import de.bennyboer.kicherkrabbe.fabrics.aggregate.delete.DeleteCmd;
import de.bennyboer.kicherkrabbe.fabrics.aggregate.delete.DeletedEvent;
import de.bennyboer.kicherkrabbe.fabrics.aggregate.publish.PublishCmd;
import de.bennyboer.kicherkrabbe.fabrics.aggregate.publish.PublishedEvent;
import de.bennyboer.kicherkrabbe.fabrics.aggregate.rename.RenameCmd;
import de.bennyboer.kicherkrabbe.fabrics.aggregate.rename.RenamedEvent;
import de.bennyboer.kicherkrabbe.fabrics.aggregate.snapshot.SnapshottedEvent;
import de.bennyboer.kicherkrabbe.fabrics.aggregate.unpublish.UnpublishCmd;
import de.bennyboer.kicherkrabbe.fabrics.aggregate.unpublish.UnpublishedEvent;
import de.bennyboer.kicherkrabbe.fabrics.aggregate.update.availability.AvailabilityUpdatedEvent;
import de.bennyboer.kicherkrabbe.fabrics.aggregate.update.availability.UpdateAvailabilityCmd;
import de.bennyboer.kicherkrabbe.fabrics.aggregate.update.colors.ColorsUpdatedEvent;
import de.bennyboer.kicherkrabbe.fabrics.aggregate.update.colors.UpdateColorsCmd;
import de.bennyboer.kicherkrabbe.fabrics.aggregate.update.image.ImageUpdatedEvent;
import de.bennyboer.kicherkrabbe.fabrics.aggregate.update.image.UpdateImageCmd;
import de.bennyboer.kicherkrabbe.fabrics.aggregate.update.themes.ThemesUpdatedEvent;
import de.bennyboer.kicherkrabbe.fabrics.aggregate.update.themes.UpdateThemesCmd;
import de.bennyboer.kicherkrabbe.fabrics.colors.ColorId;
import de.bennyboer.kicherkrabbe.fabrics.themes.ThemeId;
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
public class Fabric implements Aggregate {

    public static final AggregateType TYPE = AggregateType.of("FABRIC");

    FabricId id;

    Version version;

    FabricName name;

    ImageId image;

    Set<ColorId> colors;

    Set<ThemeId> themes;

    Set<FabricTypeAvailability> availability;

    boolean published;

    Instant createdAt;

    @Nullable
    Instant deletedAt;

    public static Fabric init() {
        return new Fabric(
                null,
                Version.zero(),
                null,
                null,
                Set.of(),
                Set.of(),
                Set.of(),
                false,
                Instant.now(),
                null
        );
    }

    @Override
    public ApplyCommandResult apply(Command cmd, Agent agent) {
        check(isNotDeleted() || cmd instanceof SnapshotCmd, "Cannot apply command to deleted aggregate");

        return switch (cmd) {
            case SnapshotCmd ignored -> ApplyCommandResult.of(SnapshottedEvent.of(
                    getName(),
                    getImage(),
                    getColors(),
                    getThemes(),
                    getAvailability(),
                    isPublished(),
                    getCreatedAt(),
                    getDeletedAt().orElse(null)
            ));
            case CreateCmd c -> ApplyCommandResult.of(CreatedEvent.of(
                    c.getName(),
                    c.getImage(),
                    c.getColors(),
                    c.getThemes(),
                    c.getAvailability()
            ));
            case DeleteCmd ignored -> ApplyCommandResult.of(DeletedEvent.of());
            case PublishCmd ignored -> ApplyCommandResult.of(PublishedEvent.of());
            case UnpublishCmd ignored -> ApplyCommandResult.of(UnpublishedEvent.of());
            case UpdateImageCmd c -> ApplyCommandResult.of(ImageUpdatedEvent.of(c.getImage()));
            case UpdateColorsCmd c -> ApplyCommandResult.of(ColorsUpdatedEvent.of(c.getColors()));
            case UpdateThemesCmd c -> ApplyCommandResult.of(ThemesUpdatedEvent.of(c.getThemes()));
            case UpdateAvailabilityCmd c -> ApplyCommandResult.of(AvailabilityUpdatedEvent.of(c.getAvailability()));
            case RenameCmd c -> ApplyCommandResult.of(RenamedEvent.of(c.getName()));
            default -> throw new IllegalArgumentException("Unknown command " + cmd.getClass().getSimpleName());
        };
    }

    @Override
    public Aggregate apply(Event event, EventMetadata metadata) {
        FabricId id = FabricId.of(metadata.getAggregateId().getValue());
        Version version = metadata.getAggregateVersion();

        return (switch (event) {
            case SnapshottedEvent e -> withId(id)
                    .withName(e.getName())
                    .withImage(e.getImage())
                    .withColors(e.getColors())
                    .withThemes(e.getThemes())
                    .withAvailability(e.getAvailability())
                    .withPublished(e.isPublished())
                    .withCreatedAt(e.getCreatedAt())
                    .withDeletedAt(e.getDeletedAt().orElse(null));
            case CreatedEvent e -> withId(id)
                    .withName(e.getName())
                    .withImage(e.getImage())
                    .withColors(e.getColors())
                    .withThemes(e.getThemes())
                    .withAvailability(e.getAvailability());
            case DeletedEvent ignored -> withDeletedAt(metadata.getDate());
            case PublishedEvent ignored -> withPublished(true);
            case UnpublishedEvent ignored -> withPublished(false);
            case ImageUpdatedEvent e -> withImage(e.getImage());
            case ColorsUpdatedEvent e -> withColors(e.getColors());
            case ThemesUpdatedEvent e -> withThemes(e.getThemes());
            case AvailabilityUpdatedEvent e -> withAvailability(e.getAvailability());
            case RenamedEvent e -> withName(e.getName());
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
