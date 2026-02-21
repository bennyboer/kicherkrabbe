package de.bennyboer.kicherkrabbe.offers;

import de.bennyboer.kicherkrabbe.eventsourcing.EventSourcingService;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateIdAndVersion;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateService;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.EventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.money.Money;
import de.bennyboer.kicherkrabbe.offers.archive.ArchiveCmd;
import de.bennyboer.kicherkrabbe.offers.categories.remove.RemoveCategoryCmd;
import de.bennyboer.kicherkrabbe.offers.categories.update.UpdateCategoriesCmd;
import de.bennyboer.kicherkrabbe.offers.create.CreateCmd;
import de.bennyboer.kicherkrabbe.offers.delete.DeleteCmd;
import de.bennyboer.kicherkrabbe.offers.discount.add.AddDiscountCmd;
import de.bennyboer.kicherkrabbe.offers.discount.remove.RemoveDiscountCmd;
import de.bennyboer.kicherkrabbe.offers.images.update.UpdateImagesCmd;
import de.bennyboer.kicherkrabbe.offers.notes.update.UpdateNotesCmd;
import de.bennyboer.kicherkrabbe.offers.price.update.UpdatePriceCmd;
import de.bennyboer.kicherkrabbe.offers.publish.PublishCmd;
import de.bennyboer.kicherkrabbe.offers.reserve.ReserveCmd;
import de.bennyboer.kicherkrabbe.offers.size.update.UpdateSizeCmd;
import de.bennyboer.kicherkrabbe.offers.title.update.UpdateTitleCmd;
import de.bennyboer.kicherkrabbe.offers.unpublish.UnpublishCmd;
import de.bennyboer.kicherkrabbe.offers.unreserve.UnreserveCmd;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.util.List;
import java.util.Set;

public class OfferService extends AggregateService<Offer, OfferId> {

    public OfferService(EventSourcingRepo repo, EventPublisher eventPublisher, Clock clock) {
        super(new EventSourcingService<>(
                Offer.TYPE,
                Offer.init(),
                repo,
                eventPublisher,
                List.of(),
                clock
        ));
    }

    public Mono<AggregateIdAndVersion<OfferId>> create(
            OfferTitle title,
            OfferSize size,
            Set<OfferCategoryId> categories,
            ProductId productId,
            List<ImageId> images,
            Notes notes,
            Money price,
            Agent agent
    ) {
        var id = OfferId.create();
        var cmd = CreateCmd.of(title, size, categories, productId, images, notes, price);

        return dispatchCommandToLatest(id, agent, cmd)
                .map(version -> AggregateIdAndVersion.of(id, version));
    }

    public Mono<Version> delete(OfferId id, Version version, Agent agent) {
        return dispatchCommand(id, version, agent, DeleteCmd.of());
    }

    public Mono<Version> publish(OfferId id, Version version, Agent agent) {
        return dispatchCommand(id, version, agent, PublishCmd.of());
    }

    public Mono<Version> unpublish(OfferId id, Version version, Agent agent) {
        return dispatchCommand(id, version, agent, UnpublishCmd.of());
    }

    public Mono<Version> reserve(OfferId id, Version version, Agent agent) {
        return dispatchCommand(id, version, agent, ReserveCmd.of());
    }

    public Mono<Version> unreserve(OfferId id, Version version, Agent agent) {
        return dispatchCommand(id, version, agent, UnreserveCmd.of());
    }

    public Mono<Version> archive(OfferId id, Version version, Agent agent) {
        return dispatchCommand(id, version, agent, ArchiveCmd.of());
    }

    public Mono<Version> updateImages(OfferId id, Version version, List<ImageId> images, Agent agent) {
        return dispatchCommand(id, version, agent, UpdateImagesCmd.of(images));
    }

    public Mono<Version> updateNotes(OfferId id, Version version, Notes notes, Agent agent) {
        return dispatchCommand(id, version, agent, UpdateNotesCmd.of(notes));
    }

    public Mono<Version> updatePrice(OfferId id, Version version, Money price, Agent agent) {
        return dispatchCommand(id, version, agent, UpdatePriceCmd.of(price));
    }

    public Mono<Version> addDiscount(OfferId id, Version version, Money discountedPrice, Agent agent) {
        return dispatchCommand(id, version, agent, AddDiscountCmd.of(discountedPrice));
    }

    public Mono<Version> removeDiscount(OfferId id, Version version, Agent agent) {
        return dispatchCommand(id, version, agent, RemoveDiscountCmd.of());
    }

    public Mono<Version> updateTitle(OfferId id, Version version, OfferTitle title, Agent agent) {
        return dispatchCommand(id, version, agent, UpdateTitleCmd.of(title));
    }

    public Mono<Version> updateSize(OfferId id, Version version, OfferSize size, Agent agent) {
        return dispatchCommand(id, version, agent, UpdateSizeCmd.of(size));
    }

    public Mono<Version> updateCategories(OfferId id, Version version, Set<OfferCategoryId> categories, Agent agent) {
        return dispatchCommand(id, version, agent, UpdateCategoriesCmd.of(categories));
    }

    public Mono<Version> removeCategory(OfferId id, Version version, OfferCategoryId categoryId, Agent agent) {
        return dispatchCommand(id, version, agent, RemoveCategoryCmd.of(categoryId));
    }

    @Override
    protected AggregateType getAggregateType() {
        return Offer.TYPE;
    }

    @Override
    protected AggregateId toAggregateId(OfferId offerId) {
        return AggregateId.of(offerId.getValue());
    }

    @Override
    protected boolean isRemoved(Offer aggregate) {
        return aggregate.isDeleted();
    }

}
