package de.bennyboer.kicherkrabbe.embroideries;

import de.bennyboer.kicherkrabbe.embroideries.create.CreateCmd;
import de.bennyboer.kicherkrabbe.embroideries.delete.DeleteCmd;
import de.bennyboer.kicherkrabbe.embroideries.delete.category.RemoveCategoryCmd;
import de.bennyboer.kicherkrabbe.embroideries.feature.FeatureCmd;
import de.bennyboer.kicherkrabbe.embroideries.publish.PublishCmd;
import de.bennyboer.kicherkrabbe.embroideries.rename.RenameCmd;
import de.bennyboer.kicherkrabbe.embroideries.unfeature.UnfeatureCmd;
import de.bennyboer.kicherkrabbe.embroideries.unpublish.UnpublishCmd;
import de.bennyboer.kicherkrabbe.embroideries.update.categories.UpdateCategoriesCmd;
import de.bennyboer.kicherkrabbe.embroideries.update.image.UpdateImageCmd;
import de.bennyboer.kicherkrabbe.eventsourcing.EventSourcingService;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateIdAndVersion;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateService;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.EventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.util.List;
import java.util.Set;

public class EmbroideryService extends AggregateService<Embroidery, EmbroideryId> {

    public EmbroideryService(EventSourcingRepo repo, EventPublisher eventPublisher, Clock clock) {
        super(new EventSourcingService<>(
                Embroidery.TYPE,
                Embroidery.init(),
                repo,
                eventPublisher,
                List.of(),
                clock
        ));
    }

    public Mono<AggregateIdAndVersion<EmbroideryId>> create(
            EmbroideryName name,
            ImageId image,
            Set<EmbroideryCategoryId> categories,
            Agent agent
    ) {
        var id = EmbroideryId.create();
        var cmd = CreateCmd.of(name, image, categories);

        return dispatchCommandToLatest(id, agent, cmd)
                .map(version -> AggregateIdAndVersion.of(id, version));
    }

    public Mono<Version> rename(EmbroideryId id, Version version, EmbroideryName name, Agent agent) {
        return dispatchCommand(id, version, agent, RenameCmd.of(name));
    }

    public Mono<Version> publish(EmbroideryId id, Version version, Agent agent) {
        return dispatchCommand(id, version, agent, PublishCmd.of());
    }

    public Mono<Version> unpublish(EmbroideryId id, Version version, Agent agent) {
        return dispatchCommand(id, version, agent, UnpublishCmd.of());
    }

    public Mono<Version> feature(EmbroideryId id, Version version, Agent agent) {
        return dispatchCommand(id, version, agent, FeatureCmd.of());
    }

    public Mono<Version> unfeature(EmbroideryId id, Version version, Agent agent) {
        return dispatchCommand(id, version, agent, UnfeatureCmd.of());
    }

    public Mono<Version> updateImage(EmbroideryId id, Version version, ImageId image, Agent agent) {
        return dispatchCommand(id, version, agent, UpdateImageCmd.of(image));
    }

    public Mono<Version> updateCategories(
            EmbroideryId id,
            Version version,
            Set<EmbroideryCategoryId> categories,
            Agent agent
    ) {
        return dispatchCommand(id, version, agent, UpdateCategoriesCmd.of(categories));
    }

    public Mono<Version> removeCategory(EmbroideryId id, Version version, EmbroideryCategoryId categoryId, Agent agent) {
        return dispatchCommand(id, version, agent, RemoveCategoryCmd.of(categoryId));
    }

    public Mono<Version> delete(EmbroideryId id, Version version, Agent agent) {
        return dispatchCommand(id, version, agent, DeleteCmd.of());
    }

    @Override
    protected AggregateType getAggregateType() {
        return Embroidery.TYPE;
    }

    @Override
    protected AggregateId toAggregateId(EmbroideryId embroideryId) {
        return AggregateId.of(embroideryId.getValue());
    }

    @Override
    protected boolean isRemoved(Embroidery aggregate) {
        return aggregate.isDeleted();
    }

}
