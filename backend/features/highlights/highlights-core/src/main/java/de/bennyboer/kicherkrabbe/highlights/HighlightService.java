package de.bennyboer.kicherkrabbe.highlights;

import de.bennyboer.kicherkrabbe.eventsourcing.EventSourcingService;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateIdAndVersion;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateService;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.EventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.highlights.create.CreateCmd;
import de.bennyboer.kicherkrabbe.highlights.delete.DeleteCmd;
import de.bennyboer.kicherkrabbe.highlights.image.UpdateImageCmd;
import de.bennyboer.kicherkrabbe.highlights.links.add.AddLinkCmd;
import de.bennyboer.kicherkrabbe.highlights.links.remove.RemoveLinkCmd;
import de.bennyboer.kicherkrabbe.highlights.links.update.UpdateLinkCmd;
import de.bennyboer.kicherkrabbe.highlights.publish.PublishCmd;
import de.bennyboer.kicherkrabbe.highlights.sort.UpdateSortOrderCmd;
import de.bennyboer.kicherkrabbe.highlights.unpublish.UnpublishCmd;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.util.List;

public class HighlightService extends AggregateService<Highlight, HighlightId> {

    public HighlightService(EventSourcingRepo repo, EventPublisher eventPublisher, Clock clock) {
        super(new EventSourcingService<>(
                Highlight.TYPE,
                Highlight.init(),
                repo,
                eventPublisher,
                List.of(),
                clock
        ));
    }

    public Mono<AggregateIdAndVersion<HighlightId>> create(ImageId imageId, long sortOrder, Agent agent) {
        HighlightId id = HighlightId.create();

        return dispatchCommandToLatest(id, agent, CreateCmd.of(imageId, sortOrder))
                .map(version -> AggregateIdAndVersion.of(id, version));
    }

    public Mono<Version> updateImage(HighlightId id, Version version, ImageId imageId, Agent agent) {
        return dispatchCommand(id, version, agent, UpdateImageCmd.of(imageId));
    }

    public Mono<Version> addLink(HighlightId id, Version version, Link link, Agent agent) {
        return dispatchCommand(id, version, agent, AddLinkCmd.of(link));
    }

    public Mono<Version> updateLink(HighlightId id, Version version, Link link, Agent agent) {
        return dispatchCommand(id, version, agent, UpdateLinkCmd.of(link));
    }

    public Mono<Version> removeLink(HighlightId id, Version version, LinkType linkType, LinkId linkId, Agent agent) {
        return dispatchCommand(id, version, agent, RemoveLinkCmd.of(linkType, linkId));
    }

    public Mono<Version> publish(HighlightId id, Version version, Agent agent) {
        return dispatchCommand(id, version, agent, PublishCmd.of());
    }

    public Mono<Version> unpublish(HighlightId id, Version version, Agent agent) {
        return dispatchCommand(id, version, agent, UnpublishCmd.of());
    }

    public Mono<Version> updateSortOrder(HighlightId id, Version version, long sortOrder, Agent agent) {
        return dispatchCommand(id, version, agent, UpdateSortOrderCmd.of(sortOrder));
    }

    public Mono<Version> delete(HighlightId id, Version version, Agent agent) {
        return dispatchCommand(id, version, agent, DeleteCmd.of());
    }

    @Override
    protected AggregateType getAggregateType() {
        return Highlight.TYPE;
    }

    @Override
    protected AggregateId toAggregateId(HighlightId id) {
        return AggregateId.of(id.getValue());
    }

    @Override
    protected boolean isRemoved(Highlight aggregate) {
        return aggregate.isDeleted();
    }

}
