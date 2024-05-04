package de.bennyboer.kicherkrabbe.fabrics.aggregate;

import de.bennyboer.kicherkrabbe.eventsourcing.EventSourcingService;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateIdAndVersion;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateService;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.EventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.fabrics.aggregate.create.CreateCmd;
import de.bennyboer.kicherkrabbe.fabrics.aggregate.delete.DeleteCmd;
import de.bennyboer.kicherkrabbe.fabrics.aggregate.publish.PublishCmd;
import de.bennyboer.kicherkrabbe.fabrics.aggregate.rename.RenameCmd;
import de.bennyboer.kicherkrabbe.fabrics.aggregate.unpublish.UnpublishCmd;
import de.bennyboer.kicherkrabbe.fabrics.aggregate.update.availability.UpdateAvailabilityCmd;
import de.bennyboer.kicherkrabbe.fabrics.aggregate.update.colors.UpdateColorsCmd;
import de.bennyboer.kicherkrabbe.fabrics.aggregate.update.image.UpdateImageCmd;
import de.bennyboer.kicherkrabbe.fabrics.aggregate.update.themes.UpdateThemesCmd;
import de.bennyboer.kicherkrabbe.fabrics.colors.ColorId;
import de.bennyboer.kicherkrabbe.fabrics.themes.ThemeId;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

public class FabricService extends AggregateService<Fabric, FabricId> {

    public FabricService(EventSourcingRepo repo, EventPublisher eventPublisher) {
        super(new EventSourcingService<>(
                Fabric.TYPE,
                Fabric.init(),
                repo,
                eventPublisher,
                List.of()
        ));
    }

    public Mono<AggregateIdAndVersion<FabricId>> create(
            FabricName name,
            ImageId image,
            Set<ColorId> colors,
            Set<ThemeId> themes,
            Set<FabricTypeAvailability> availability,
            Agent agent
    ) {
        var id = FabricId.create();
        var cmd = CreateCmd.of(
                name,
                image,
                colors,
                themes,
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

    public Mono<Version> updateThemes(FabricId id, Version version, Set<ThemeId> themes, Agent agent) {
        return dispatchCommand(id, version, agent, UpdateThemesCmd.of(themes));
    }

    public Mono<Version> updateAvailability(
            FabricId id,
            Version version,
            Set<FabricTypeAvailability> availability,
            Agent agent
    ) {
        return dispatchCommand(id, version, agent, UpdateAvailabilityCmd.of(availability));
    }

    public Mono<Version> delete(FabricId id, Version version, Agent agent) {
        return dispatchCommand(id, version, agent, DeleteCmd.of());
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
