package de.bennyboer.kicherkrabbe.fabrics;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.Aggregate;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.ApplyCommandResult;
import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.eventsourcing.command.SnapshotCmd;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.EventMetadata;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.fabrics.create.CreateCmd;
import de.bennyboer.kicherkrabbe.fabrics.create.CreatedEvent;
import de.bennyboer.kicherkrabbe.fabrics.delete.DeleteCmd;
import de.bennyboer.kicherkrabbe.fabrics.delete.DeletedEvent;
import de.bennyboer.kicherkrabbe.fabrics.delete.colors.ColorRemovedEvent;
import de.bennyboer.kicherkrabbe.fabrics.delete.colors.RemoveColorCmd;
import de.bennyboer.kicherkrabbe.fabrics.delete.fabrictype.FabricTypeRemovedEvent;
import de.bennyboer.kicherkrabbe.fabrics.delete.fabrictype.RemoveFabricTypeCmd;
import de.bennyboer.kicherkrabbe.fabrics.delete.topics.RemoveTopicCmd;
import de.bennyboer.kicherkrabbe.fabrics.delete.topics.TopicRemovedEvent;
import de.bennyboer.kicherkrabbe.fabrics.publish.AlreadyPublishedError;
import de.bennyboer.kicherkrabbe.fabrics.publish.PublishCmd;
import de.bennyboer.kicherkrabbe.fabrics.publish.PublishedEvent;
import de.bennyboer.kicherkrabbe.fabrics.rename.RenameCmd;
import de.bennyboer.kicherkrabbe.fabrics.rename.RenamedEvent;
import de.bennyboer.kicherkrabbe.fabrics.snapshot.SnapshottedEvent;
import de.bennyboer.kicherkrabbe.fabrics.unpublish.AlreadyUnpublishedError;
import de.bennyboer.kicherkrabbe.fabrics.unpublish.UnpublishCmd;
import de.bennyboer.kicherkrabbe.fabrics.unpublish.UnpublishedEvent;
import de.bennyboer.kicherkrabbe.fabrics.update.availability.AvailabilityUpdatedEvent;
import de.bennyboer.kicherkrabbe.fabrics.update.availability.UpdateAvailabilityCmd;
import de.bennyboer.kicherkrabbe.fabrics.update.colors.ColorsUpdatedEvent;
import de.bennyboer.kicherkrabbe.fabrics.update.colors.UpdateColorsCmd;
import de.bennyboer.kicherkrabbe.fabrics.update.image.ImageUpdatedEvent;
import de.bennyboer.kicherkrabbe.fabrics.update.image.UpdateImageCmd;
import de.bennyboer.kicherkrabbe.fabrics.update.topics.TopicsUpdatedEvent;
import de.bennyboer.kicherkrabbe.fabrics.update.topics.UpdateTopicsCmd;
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
public class Fabric implements Aggregate {

    public static final AggregateType TYPE = AggregateType.of("FABRIC");

    FabricId id;

    Version version;

    FabricName name;

    ImageId image;

    Set<ColorId> colors;

    Set<TopicId> topics;

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
                    getTopics(),
                    getAvailability(),
                    isPublished(),
                    getCreatedAt(),
                    getDeletedAt().orElse(null)
            ));
            case CreateCmd c -> ApplyCommandResult.of(CreatedEvent.of(
                    c.getName(),
                    c.getImage(),
                    c.getColors(),
                    c.getTopics(),
                    c.getAvailability()
            ));
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
            case UpdateImageCmd c -> ApplyCommandResult.of(ImageUpdatedEvent.of(c.getImage()));
            case UpdateColorsCmd c -> ApplyCommandResult.of(ColorsUpdatedEvent.of(c.getColors()));
            case UpdateTopicsCmd c -> ApplyCommandResult.of(TopicsUpdatedEvent.of(c.getTopics()));
            case UpdateAvailabilityCmd c -> ApplyCommandResult.of(AvailabilityUpdatedEvent.of(c.getAvailability()));
            case RenameCmd c -> ApplyCommandResult.of(RenamedEvent.of(c.getName()));
            case RemoveColorCmd c -> {
                check(getColors().contains(c.getColorId()), "Color to remove is not in fabric");
                yield ApplyCommandResult.of(ColorRemovedEvent.of(c.getColorId()));
            }
            case RemoveTopicCmd c -> {
                check(getTopics().contains(c.getTopicId()), "Topic to remove is not in fabric");
                yield ApplyCommandResult.of(TopicRemovedEvent.of(c.getTopicId()));
            }
            case RemoveFabricTypeCmd c -> {
                check(
                        getAvailability().stream().anyMatch(a -> a.getTypeId().equals(c.getFabricTypeId())),
                        "Fabric type to remove is not in fabric"
                );
                yield ApplyCommandResult.of(FabricTypeRemovedEvent.of(c.getFabricTypeId()));
            }
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
                    .withTopics(e.getTopics())
                    .withAvailability(e.getAvailability())
                    .withPublished(e.isPublished())
                    .withCreatedAt(e.getCreatedAt())
                    .withDeletedAt(e.getDeletedAt().orElse(null));
            case CreatedEvent e -> withId(id)
                    .withName(e.getName())
                    .withImage(e.getImage())
                    .withColors(e.getColors())
                    .withTopics(e.getTopics())
                    .withAvailability(e.getAvailability())
                    .withCreatedAt(metadata.getDate());
            case DeletedEvent ignored -> withDeletedAt(metadata.getDate());
            case PublishedEvent ignored -> withPublished(true);
            case UnpublishedEvent ignored -> withPublished(false);
            case ImageUpdatedEvent e -> withImage(e.getImage());
            case ColorsUpdatedEvent e -> withColors(e.getColors());
            case TopicsUpdatedEvent e -> withTopics(e.getTopics());
            case AvailabilityUpdatedEvent e -> withAvailability(e.getAvailability());
            case RenamedEvent e -> withName(e.getName());
            case ColorRemovedEvent e -> {
                Set<ColorId> updatedColors = new HashSet<>(getColors());
                updatedColors.remove(e.getColorId());
                yield withColors(updatedColors);
            }
            case TopicRemovedEvent e -> {
                Set<TopicId> updatedTopics = new HashSet<>(getTopics());
                updatedTopics.remove(e.getTopicId());
                yield withTopics(updatedTopics);
            }
            case FabricTypeRemovedEvent e -> {
                Set<FabricTypeAvailability> updatedAvailability = new HashSet<>(getAvailability());
                updatedAvailability.removeIf(a -> a.getTypeId().equals(e.getFabricTypeId()));
                yield withAvailability(updatedAvailability);
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

}
