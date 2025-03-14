package de.bennyboer.kicherkrabbe.fabrics;

import de.bennyboer.kicherkrabbe.eventsourcing.EventSourcingService;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateIdAndVersion;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateService;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.EventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.fabrics.create.CreateCmd;
import de.bennyboer.kicherkrabbe.fabrics.delete.DeleteCmd;
import de.bennyboer.kicherkrabbe.fabrics.delete.colors.RemoveColorCmd;
import de.bennyboer.kicherkrabbe.fabrics.delete.fabrictype.RemoveFabricTypeCmd;
import de.bennyboer.kicherkrabbe.fabrics.delete.topics.RemoveTopicCmd;
import de.bennyboer.kicherkrabbe.fabrics.publish.PublishCmd;
import de.bennyboer.kicherkrabbe.fabrics.rename.RenameCmd;
import de.bennyboer.kicherkrabbe.fabrics.unpublish.UnpublishCmd;
import de.bennyboer.kicherkrabbe.fabrics.update.availability.UpdateAvailabilityCmd;
import de.bennyboer.kicherkrabbe.fabrics.update.colors.UpdateColorsCmd;
import de.bennyboer.kicherkrabbe.fabrics.update.image.UpdateImageCmd;
import de.bennyboer.kicherkrabbe.fabrics.update.topics.UpdateTopicsCmd;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.util.List;
import java.util.Set;

public class FabricService extends AggregateService<Fabric, FabricId> {

    public FabricService(EventSourcingRepo repo, EventPublisher eventPublisher, Clock clock) {
        super(new EventSourcingService<>(
                Fabric.TYPE,
                Fabric.init(),
                repo,
                eventPublisher,
                List.of(),
                clock
        ));
    }

    public Mono<AggregateIdAndVersion<FabricId>> create(
            FabricName name,
            ImageId image,
            Set<ColorId> colors,
            Set<TopicId> topics,
            Set<FabricTypeAvailability> availability,
            Agent agent
    ) {
        var id = FabricId.create();
        var cmd = CreateCmd.of(
                name,
                image,
                colors,
                topics,
                availability
        );

        return dispatchCommandToLatest(id, agent, cmd)
                .map(version -> AggregateIdAndVersion.of(id, version));
    }

    public Mono<Version> rename(FabricId id, Version version, FabricName name, Agent agent) {
        return dispatchCommand(id, version, agent, RenameCmd.of(name));
    }

    public Mono<Version> publish(FabricId id, Version version, Agent agent) {
        return dispatchCommand(id, version, agent, PublishCmd.of());
    }

    public Mono<Version> unpublish(FabricId id, Version version, Agent agent) {
        return dispatchCommand(id, version, agent, UnpublishCmd.of());
    }

    public Mono<Version> updateImage(FabricId id, Version version, ImageId image, Agent agent) {
        return dispatchCommand(id, version, agent, UpdateImageCmd.of(image));
    }

    public Mono<Version> updateColors(FabricId id, Version version, Set<ColorId> colors, Agent agent) {
        return dispatchCommand(id, version, agent, UpdateColorsCmd.of(colors));
    }

    public Mono<Version> updateTopics(FabricId id, Version version, Set<TopicId> topics, Agent agent) {
        return dispatchCommand(id, version, agent, UpdateTopicsCmd.of(topics));
    }

    public Mono<Version> updateAvailability(
            FabricId id,
            Version version,
            Set<FabricTypeAvailability> availability,
            Agent agent
    ) {
        return dispatchCommand(id, version, agent, UpdateAvailabilityCmd.of(availability));
    }

    public Mono<Version> removeTopic(FabricId id, Version version, TopicId topicId, Agent agent) {
        return dispatchCommand(id, version, agent, RemoveTopicCmd.of(topicId));
    }

    public Mono<Version> removeColor(FabricId id, Version version, ColorId colorId, Agent agent) {
        return dispatchCommand(id, version, agent, RemoveColorCmd.of(colorId));
    }

    public Mono<Version> removeFabricType(FabricId id, Version version, FabricTypeId fabricTypeId, Agent agent) {
        return dispatchCommand(id, version, agent, RemoveFabricTypeCmd.of(fabricTypeId));
    }

    public Mono<Version> delete(FabricId id, Version version, Agent agent) {
        return dispatchCommand(id, version, agent, DeleteCmd.of());
    }

    @Override
    protected AggregateType getAggregateType() {
        return Fabric.TYPE;
    }

    @Override
    protected AggregateId toAggregateId(FabricId fabricId) {
        return AggregateId.of(fabricId.getValue());
    }

    @Override
    protected boolean isRemoved(Fabric aggregate) {
        return aggregate.isDeleted();
    }

}
