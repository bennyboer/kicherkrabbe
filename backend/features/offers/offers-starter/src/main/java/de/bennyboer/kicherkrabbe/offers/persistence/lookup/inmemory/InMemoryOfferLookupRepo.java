package de.bennyboer.kicherkrabbe.offers.persistence.lookup.inmemory;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.inmemory.InMemoryEventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.money.Money;
import de.bennyboer.kicherkrabbe.offers.OfferAlias;
import de.bennyboer.kicherkrabbe.offers.OfferCategoryId;
import de.bennyboer.kicherkrabbe.offers.OfferId;
import de.bennyboer.kicherkrabbe.offers.OfferSize;
import de.bennyboer.kicherkrabbe.offers.ProductId;
import de.bennyboer.kicherkrabbe.offers.persistence.lookup.*;
import jakarta.annotation.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Comparator;
import java.util.Locale;
import java.util.Set;

import static java.util.Objects.requireNonNull;

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
    public Mono<LookupOfferPage> findPublished(PublishedOfferQuery query) {
        requireNonNull(query);

        return getAll()
                .filter(offer -> offer.isPublished() && offer.getArchivedAt().isEmpty())
                .filter(offer -> matchesSearchTerm(offer, query.getSearchTerm()))
                .filter(offer -> matchesCategories(offer, query.getCategories()))
                .filter(offer -> matchesSizes(offer, query.getSizes()))
                .filter(offer -> matchesPriceRange(offer, query.getMinPrice(), query.getMaxPrice()))
                .sort(resolveComparator(query.getSortProperty(), query.getSortDirection()))
                .collectList()
                .flatMap(offers -> {
                    long total = offers.size();

                    return Flux.fromIterable(offers)
                            .skip(query.getSkip())
                            .take(query.getLimit())
                            .collectList()
                            .map(results -> LookupOfferPage.of(query.getSkip(), query.getLimit(), total, results));
                });
    }

    @Override
    public Mono<LookupOffer> findPublished(OfferId id) {
        return getAll()
                .filter(offer -> offer.getId().equals(id) && offer.isPublished() && offer.getArchivedAt().isEmpty())
                .singleOrEmpty();
    }

    @Override
    public Mono<LookupOffer> findPublishedByAlias(OfferAlias alias) {
        return getAll()
                .filter(offer -> offer.getAlias().equals(alias) && offer.isPublished() && offer.getArchivedAt().isEmpty())
                .singleOrEmpty();
    }

    @Override
    public Mono<LookupOffer> findByAlias(OfferAlias alias) {
        return getAll()
                .filter(offer -> offer.getAlias().equals(alias))
                .singleOrEmpty();
    }

    @Override
    public Flux<LookupOffer> findByProductId(ProductId productId) {
        return getAll()
                .filter(offer -> offer.getProduct().getId().equals(productId));
    }

    @Override
    public Flux<LookupOffer> findByCategoryId(OfferCategoryId categoryId) {
        return getAll()
                .filter(offer -> offer.getCategories().contains(categoryId));
    }

    @Override
    public Flux<String> findDistinctPublishedSizes() {
        return getAll()
                .filter(offer -> offer.isPublished() && offer.getArchivedAt().isEmpty())
                .map(offer -> offer.getSize().getValue())
                .distinct()
                .sort();
    }

    private boolean matchesSearchTerm(LookupOffer offer, String searchTerm) {
        if (searchTerm == null || searchTerm.isBlank()) {
            return true;
        }

        var term = searchTerm.toLowerCase(Locale.ROOT);

        return offer.getTitle().getValue().toLowerCase(Locale.ROOT).contains(term)
                || offer.getNotes().getDescription().getValue().toLowerCase(Locale.ROOT).contains(term)
                || offer.getProduct().getNumber().getValue().toLowerCase(Locale.ROOT).contains(term);
    }

    private boolean matchesCategories(LookupOffer offer, Set<OfferCategoryId> categories) {
        if (categories == null || categories.isEmpty()) {
            return true;
        }

        return offer.getCategories().stream().anyMatch(categories::contains);
    }

    private boolean matchesSizes(LookupOffer offer, Set<OfferSize> sizes) {
        if (sizes == null || sizes.isEmpty()) {
            return true;
        }

        return sizes.contains(offer.getSize());
    }

    private boolean matchesPriceRange(LookupOffer offer, @Nullable Long minPrice, @Nullable Long maxPrice) {
        long priceAmount = offer.getPricing().getDiscountedPrice()
                .map(Money::getAmount)
                .orElse(offer.getPricing().getPrice().getAmount());

        if (minPrice != null && priceAmount < minPrice) {
            return false;
        }

        return maxPrice == null || priceAmount <= maxPrice;
    }

    private Comparator<LookupOffer> resolveComparator(
            @Nullable OfferSortProperty property,
            @Nullable OfferSortDirection direction
    ) {
        var effectiveProperty = property != null ? property : OfferSortProperty.NEWEST;
        var effectiveDirection = direction != null ? direction : OfferSortDirection.DESCENDING;

        Comparator<LookupOffer> comparator = switch (effectiveProperty) {
            case ALPHABETICAL -> Comparator.comparing(
                    o -> o.getTitle().getValue().toLowerCase(Locale.ROOT)
            );
            case NEWEST -> Comparator.comparing(LookupOffer::getCreatedAt);
            case PRICE -> Comparator.comparingLong(
                    o -> o.getPricing().getDiscountedPrice()
                            .map(Money::getAmount)
                            .orElse(o.getPricing().getPrice().getAmount())
            );
        };

        if (effectiveDirection == OfferSortDirection.DESCENDING) {
            comparator = comparator.reversed();
        }

        return comparator;
    }

}
