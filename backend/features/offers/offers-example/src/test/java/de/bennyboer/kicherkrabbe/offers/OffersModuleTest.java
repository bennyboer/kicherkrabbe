package de.bennyboer.kicherkrabbe.offers;

import de.bennyboer.kicherkrabbe.changes.ResourceChangesTracker;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.LoggingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.inmemory.InMemoryEventSourcingRepo;
import de.bennyboer.kicherkrabbe.offers.api.MoneyDTO;
import de.bennyboer.kicherkrabbe.offers.api.NotesDTO;
import de.bennyboer.kicherkrabbe.offers.persistence.lookup.OfferLookupRepo;
import de.bennyboer.kicherkrabbe.offers.persistence.lookup.inmemory.InMemoryOfferLookupRepo;
import de.bennyboer.kicherkrabbe.offers.persistence.lookup.product.ProductForOfferLookupRepo;
import de.bennyboer.kicherkrabbe.offers.persistence.lookup.product.inmemory.InMemoryProductForOfferLookupRepo;
import de.bennyboer.kicherkrabbe.offers.samples.SampleOffer;
import de.bennyboer.kicherkrabbe.offers.samples.SampleProductForLookup;
import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import de.bennyboer.kicherkrabbe.permissions.persistence.PermissionsRepo;
import de.bennyboer.kicherkrabbe.permissions.persistence.inmemory.InMemoryPermissionsRepo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.util.List;
import java.util.Set;

public class OffersModuleTest {

    private final EventSourcingRepo eventSourcingRepo = new InMemoryEventSourcingRepo();

    private final OfferService offerService = new OfferService(
            eventSourcingRepo,
            new LoggingEventPublisher(),
            Clock.systemUTC()
    );

    private final PermissionsRepo permissionsRepo = new InMemoryPermissionsRepo();

    private final PermissionsService permissionsService = new PermissionsService(
            permissionsRepo,
            ignored -> Mono.empty()
    );

    private final OfferLookupRepo offerLookupRepo = new InMemoryOfferLookupRepo();

    private final ProductForOfferLookupRepo productForOfferLookupRepo = new InMemoryProductForOfferLookupRepo();

    private final ResourceChangesTracker changesTracker = ignored -> Flux.empty();

    private final OffersModule module = new OffersModule(
            offerService,
            permissionsService,
            offerLookupRepo,
            productForOfferLookupRepo,
            changesTracker
    );

    public void setUpProduct(SampleProductForLookup sample) {
        module.updateProductInLookup(
                sample.getId(),
                0L,
                sample.getNumber(),
                sample.getLinks(),
                sample.getFabricComposition()
        ).block();
    }

    public void setUpDefaultProduct() {
        setUpProduct(SampleProductForLookup.builder().build());
    }

    public String createOffer(
            String productId,
            List<String> imageIds,
            NotesDTO notes,
            MoneyDTO price,
            Agent agent
    ) {
        String offerId = module.createOffer(productId, imageIds, notes, price, agent).block();

        module.updateOfferInLookup(offerId).block();
        if (agent.getType() == AgentType.USER) {
            module.allowUserToManageOffer(offerId, agent.getId().getValue()).block();
        }

        return offerId;
    }

    public String createOffer(SampleOffer sample, Agent agent) {
        return createOffer(
                sample.getProductId(),
                sample.getImages(),
                sample.getNotesDTO(),
                sample.getPriceDTO(),
                agent
        );
    }

    public String createSampleOffer(Agent agent) {
        setUpDefaultProduct();
        return createOffer(SampleOffer.builder().build(), agent);
    }

    public void deleteOffer(String offerId, long version, Agent agent) {
        module.deleteOffer(offerId, version, agent).block();

        module.removeOfferFromLookup(offerId).block();
        module.removePermissionsOnOffer(offerId).block();
    }

    public void publishOffer(String offerId, long version, Agent agent) {
        module.publishOffer(offerId, version, agent).block();

        module.updateOfferInLookup(offerId).block();
        module.allowAnonymousAndSystemUsersToReadPublishedOffer(offerId).block();
    }

    public void unpublishOffer(String offerId, long version, Agent agent) {
        module.unpublishOffer(offerId, version, agent).block();

        module.updateOfferInLookup(offerId).block();
        module.disallowAnonymousAndSystemUsersToReadPublishedOffer(offerId).block();
    }

    public void reserveOffer(String offerId, long version, Agent agent) {
        module.reserveOffer(offerId, version, agent).block();

        module.updateOfferInLookup(offerId).block();
    }

    public void unreserveOffer(String offerId, long version, Agent agent) {
        module.unreserveOffer(offerId, version, agent).block();

        module.updateOfferInLookup(offerId).block();
    }

    public void archiveOffer(String offerId, long version, Agent agent) {
        module.archiveOffer(offerId, version, agent).block();

        module.updateOfferInLookup(offerId).block();
        module.disallowAnonymousAndSystemUsersToReadPublishedOffer(offerId).block();
    }

    public void updateOfferImages(String offerId, long version, List<String> imageIds, Agent agent) {
        module.updateOfferImages(offerId, version, imageIds, agent).block();

        module.updateOfferInLookup(offerId).block();
    }

    public void updateOfferNotes(String offerId, long version, NotesDTO notes, Agent agent) {
        module.updateOfferNotes(offerId, version, notes, agent).block();

        module.updateOfferInLookup(offerId).block();
    }

    public void updateOfferPrice(String offerId, long version, MoneyDTO price, Agent agent) {
        module.updateOfferPrice(offerId, version, price, agent).block();

        module.updateOfferInLookup(offerId).block();
    }

    public void addOfferDiscount(String offerId, long version, MoneyDTO discountedPrice, Agent agent) {
        module.addOfferDiscount(offerId, version, discountedPrice, agent).block();

        module.updateOfferInLookup(offerId).block();
    }

    public void removeOfferDiscount(String offerId, long version, Agent agent) {
        module.removeOfferDiscount(offerId, version, agent).block();

        module.updateOfferInLookup(offerId).block();
    }

    public List<OfferDetails> getOffers(Agent agent) {
        return getOffers("", 0, Integer.MAX_VALUE, agent).getResults();
    }

    public OffersPage getOffers(String searchTerm, long skip, long limit, Agent agent) {
        return module.getOffers(searchTerm, skip, limit, agent).block();
    }

    public OfferDetails getOffer(String offerId, Agent agent) {
        return module.getOffer(offerId, agent).block();
    }

    public PublishedOffersPage getPublishedOffers(String searchTerm, long skip, long limit, Agent agent) {
        return module.getPublishedOffers(searchTerm, skip, limit, agent).block();
    }

    public PublishedOffer getPublishedOffer(String offerId, Agent agent) {
        return module.getPublishedOffer(offerId, agent).block();
    }

    public void allowUserToCreateOffers(String userId) {
        module.allowUserToCreateOffers(userId).block();
    }

    public NotesDTO sampleNotes() {
        var notes = new NotesDTO();
        notes.description = "Sample description";
        return notes;
    }

    public MoneyDTO samplePrice() {
        var price = new MoneyDTO();
        price.amount = 1999L;
        price.currency = "EUR";
        return price;
    }

    public void addProductLink(String productId, long version, Link link) {
        module.addProductLinkInLookup(productId, version, link).block();
    }

    public void removeProductLink(String productId, long version, LinkType linkType, String linkId) {
        module.removeProductLinkFromLookup(productId, version, linkType, linkId).block();
    }

    public void updateProductLink(String productId, long version, Link updatedLink) {
        module.updateProductLinkInLookup(productId, version, updatedLink).block();
    }

    public void updateProductFabricComposition(String productId, long version, FabricComposition fabricComposition) {
        module.updateProductFabricCompositionInLookup(productId, version, fabricComposition).block();
    }

    public void updateProductNumber(String productId, long version, ProductNumber number) {
        module.updateProductNumberInLookup(productId, version, number).block();
    }

}
