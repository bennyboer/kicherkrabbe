package de.bennyboer.kicherkrabbe.products.product;

import de.bennyboer.kicherkrabbe.eventsourcing.EventSourcingService;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateIdAndVersion;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateService;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.EventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.products.product.create.CreateCmd;
import de.bennyboer.kicherkrabbe.products.product.delete.DeleteCmd;
import de.bennyboer.kicherkrabbe.products.product.fabric.composition.update.UpdateFabricCompositionCmd;
import de.bennyboer.kicherkrabbe.products.product.images.update.UpdateImagesCmd;
import de.bennyboer.kicherkrabbe.products.product.links.add.AddLinkCmd;
import de.bennyboer.kicherkrabbe.products.product.links.remove.RemoveLinkCmd;
import de.bennyboer.kicherkrabbe.products.product.notes.update.UpdateNotesCmd;
import de.bennyboer.kicherkrabbe.products.product.produced.update.UpdateProducedAtCmd;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

public class ProductService extends AggregateService<Product, ProductId> {

    public ProductService(EventSourcingRepo repo, EventPublisher eventPublisher, Clock clock) {
        super(new EventSourcingService<>(
                Product.TYPE,
                Product.init(),
                repo,
                eventPublisher,
                List.of(),
                clock
        ));
    }

    public Mono<AggregateIdAndVersion<ProductId>> create(
            ProductNumber number,
            List<ImageId> images,
            Links links,
            FabricComposition fabricComposition,
            Notes notes,
            Instant producedAt,
            Agent agent
    ) {
        var id = ProductId.create();
        var cmd = CreateCmd.of(
                number,
                images,
                links,
                fabricComposition,
                notes,
                producedAt
        );

        return dispatchCommandToLatest(id, agent, cmd)
                .map(version -> AggregateIdAndVersion.of(id, version));
    }

    public Mono<Version> addLink(ProductId id, Version version, Link link, Agent agent) {
        return dispatchCommand(id, version, agent, AddLinkCmd.of(link));
    }

    public Mono<Version> removeLink(ProductId id, Version version, LinkType linkType, LinkId linkId, Agent agent) {
        return dispatchCommand(id, version, agent, RemoveLinkCmd.of(linkType, linkId));
    }

    public Mono<Version> updateFabricComposition(ProductId id, Version version, FabricComposition fabricComposition, Agent agent) {
        return dispatchCommand(id, version, agent, UpdateFabricCompositionCmd.of(fabricComposition));
    }

    public Mono<Version> updateImages(ProductId id, Version version, List<ImageId> images, Agent agent) {
        return dispatchCommand(id, version, agent, UpdateImagesCmd.of(images));
    }

    public Mono<Version> updateProducedAt(ProductId id, Version version, Instant producedAt, Agent agent) {
        return dispatchCommand(id, version, agent, UpdateProducedAtCmd.of(producedAt));
    }

    public Mono<Version> updateNotes(ProductId id, Version version, Notes notes, Agent agent) {
        return dispatchCommand(id, version, agent, UpdateNotesCmd.of(notes));
    }

    public Mono<Version> delete(ProductId id, Version version, Agent agent) {
        return dispatchCommand(id, Version.zero(), agent, DeleteCmd.of());
    }

    @Override
    protected AggregateType getAggregateType() {
        return Product.TYPE;
    }

    @Override
    protected AggregateId toAggregateId(ProductId id) {
        return AggregateId.of(id.getValue());
    }

    @Override
    protected boolean isRemoved(Product product) {
        return product.isDeleted();
    }

}
