package de.bennyboer.kicherkrabbe.offers.persistence.lookup.inmemory;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.inmemory.InMemoryEventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.offers.OfferId;
import de.bennyboer.kicherkrabbe.offers.ProductId;
import de.bennyboer.kicherkrabbe.offers.persistence.lookup.LookupOffer;
import de.bennyboer.kicherkrabbe.offers.persistence.lookup.LookupOfferPage;
import de.bennyboer.kicherkrabbe.offers.persistence.lookup.OfferLookupRepo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Comparator;
import java.util.Locale;

public class InMemoryOfferLookupRepo extends InMemoryEventSourcingReadModelRepo<OfferId, LookupOffer>
        implements OfferLookupRepo {

    @Override
    protected boolean allowSameVersionUpdate() {
        return true;
    }

    @Override
    public Mono<LookupOfferPage> find(Collection<OfferId> offerIds, String searchTerm, long skip, long limit) {
        return getAll()
                .filter(offer -> offerIds.contains(offer.getId()))
                .filter(offer -> matchesSearchTerm(offer, searchTerm))
                .sort(Comparator.comparing(LookupOffer::getCreatedAt).reversed())
                .collectList()
                .flatMap(offers -> {
                    long total = offers.size();

                    return Flux.fromIterable(offers)
                            .skip(skip)
                            .take(limit)
                            .collectList()
                            .map(results -> LookupOfferPage.of(skip, limit, total, results));
                });
    }

    @Override
    public Mono<LookupOfferPage> findPublished(String searchTerm, long skip, long limit) {
        return getAll()
                .filter(offer -> offer.isPublished() && offer.getArchivedAt().isEmpty())
                .filter(offer -> matchesSearchTerm(offer, searchTerm))
                .sort(Comparator.comparing(LookupOffer::getCreatedAt).reversed())
                .collectList()
                .flatMap(offers -> {
                    long total = offers.size();

                    return Flux.fromIterable(offers)
                            .skip(skip)
                            .take(limit)
                            .collectList()
                            .map(results -> LookupOfferPage.of(skip, limit, total, results));
                });
    }

    @Override
    public Mono<LookupOffer> findPublished(OfferId id) {
        return getAll()
                .filter(offer -> offer.getId().equals(id) && offer.isPublished() && offer.getArchivedAt().isEmpty())
                .singleOrEmpty();
    }

    @Override
    public Flux<LookupOffer> findByProductId(ProductId productId) {
        return getAll()
                .filter(offer -> offer.getProduct().getId().equals(productId));
    }

    private boolean matchesSearchTerm(LookupOffer offer, String searchTerm) {
        if (searchTerm == null || searchTerm.isBlank()) {
            return true;
        }

        var term = searchTerm.toLowerCase(Locale.ROOT);

        return offer.getNotes().getDescription().getValue().toLowerCase(Locale.ROOT).contains(term)
                || offer.getProduct().getNumber().getValue().toLowerCase(Locale.ROOT).contains(term);
    }

}
