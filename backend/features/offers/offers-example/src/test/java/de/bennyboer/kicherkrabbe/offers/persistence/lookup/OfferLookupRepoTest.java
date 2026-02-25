package de.bennyboer.kicherkrabbe.offers.persistence.lookup;

import de.bennyboer.kicherkrabbe.money.Currency;
import de.bennyboer.kicherkrabbe.money.Money;
import de.bennyboer.kicherkrabbe.offers.*;
import de.bennyboer.kicherkrabbe.offers.samples.SampleLookupOffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class OfferLookupRepoTest {

    private OfferLookupRepo repo;

    protected abstract OfferLookupRepo createRepo();

    @BeforeEach
    public void setUp() {
        repo = createRepo();
    }

    @Test
    void shouldUpdateOffer() {
        var offer = SampleLookupOffer.builder()
                .createdAt(Instant.parse("2024-03-12T13:00:00.00Z"))
                .build()
                .toModel();
        update(offer);

        var offers = find(Set.of(offer.getId()));
        assertThat(offers).containsExactly(offer);
    }

    @Test
    void shouldRemoveOffer() {
        var offer1 = SampleLookupOffer.builder()
                .createdAt(Instant.parse("2024-03-12T13:00:00.00Z"))
                .build()
                .toModel();
        var offer2 = SampleLookupOffer.builder()
                .createdAt(Instant.parse("2024-03-12T12:00:00.00Z"))
                .build()
                .toModel();
        update(offer1);
        update(offer2);

        remove(offer1.getId());

        var offers = find(Set.of(offer1.getId(), offer2.getId()));
        assertThat(offers).containsExactly(offer2);
    }

    @Test
    void shouldFindOffers() {
        var offer1 = SampleLookupOffer.builder()
                .createdAt(Instant.parse("2024-03-12T13:00:00.00Z"))
                .build()
                .toModel();
        var offer2 = SampleLookupOffer.builder()
                .createdAt(Instant.parse("2024-03-12T12:00:00.00Z"))
                .build()
                .toModel();
        update(offer1);
        update(offer2);

        var offers = find(Set.of(offer1.getId(), offer2.getId()));
        assertThat(offers).containsExactly(offer1, offer2);
    }

    @Test
    void shouldFindOffersWithPaging() {
        var offer1 = SampleLookupOffer.builder()
                .createdAt(Instant.parse("2024-03-12T13:00:00.00Z"))
                .build()
                .toModel();
        var offer2 = SampleLookupOffer.builder()
                .createdAt(Instant.parse("2024-03-12T09:00:00.00Z"))
                .build()
                .toModel();
        var offer3 = SampleLookupOffer.builder()
                .createdAt(Instant.parse("2024-03-12T11:00:00.00Z"))
                .build()
                .toModel();
        update(offer1);
        update(offer2);
        update(offer3);

        var offerIds = Set.of(offer1.getId(), offer2.getId(), offer3.getId());
        assertThat(find(offerIds, 0, 2)).containsExactly(offer1, offer3);
        assertThat(find(offerIds, 2, 2)).containsExactly(offer2);
        assertThat(find(offerIds, 3, 2)).isEmpty();
    }

    @Test
    void shouldFindPublishedOffer() {
        var offer1 = SampleLookupOffer.builder()
                .published(true)
                .createdAt(Instant.parse("2024-03-12T13:00:00.00Z"))
                .build()
                .toModel();
        var offer2 = SampleLookupOffer.builder()
                .createdAt(Instant.parse("2024-03-12T12:00:00.00Z"))
                .build()
                .toModel();
        update(offer1);
        update(offer2);

        var foundOffer1 = findPublished(offer1.getId());
        assertThat(foundOffer1).isEqualTo(offer1);

        var foundOffer2 = findPublished(offer2.getId());
        assertThat(foundOffer2).isNull();
    }

    @Test
    void shouldFindPublishedOffers() {
        var offer1 = SampleLookupOffer.builder()
                .published(true)
                .createdAt(Instant.parse("2024-03-12T13:00:00.00Z"))
                .build()
                .toModel();
        var offer2 = SampleLookupOffer.builder()
                .published(true)
                .createdAt(Instant.parse("2024-03-12T09:00:00.00Z"))
                .build()
                .toModel();
        var offer3 = SampleLookupOffer.builder()
                .createdAt(Instant.parse("2024-03-12T11:00:00.00Z"))
                .build()
                .toModel();
        update(offer1);
        update(offer2);
        update(offer3);

        var result = findPublished("", 0, 10);
        assertThat(result.getResults()).containsExactly(offer1, offer2);
        assertThat(result.getTotal()).isEqualTo(2);
    }

    @Test
    void shouldFindOffersBySearchTerm() {
        var offer1 = SampleLookupOffer.builder()
                .notes(Notes.of(Note.of("Cotton summer dress"), null, null, null))
                .createdAt(Instant.parse("2024-03-12T13:00:00.00Z"))
                .build()
                .toModel();
        var offer2 = SampleLookupOffer.builder()
                .notes(Notes.of(Note.of("Silk evening gown"), null, null, null))
                .createdAt(Instant.parse("2024-03-12T12:00:00.00Z"))
                .build()
                .toModel();
        var offer3 = SampleLookupOffer.builder()
                .notes(Notes.of(Note.of("Cotton winter coat"), null, null, null))
                .createdAt(Instant.parse("2024-03-12T11:00:00.00Z"))
                .build()
                .toModel();
        update(offer1);
        update(offer2);
        update(offer3);

        var allIds = Set.of(offer1.getId(), offer2.getId(), offer3.getId());
        assertThat(find(allIds, "cotton", 0, 10)).containsExactly(offer1, offer3);
        assertThat(find(allIds, "silk", 0, 10)).containsExactly(offer2);
        assertThat(find(allIds, "nonexistent", 0, 10)).isEmpty();
    }

    @Test
    void shouldFindOffersByProductNumber() {
        var offer1 = SampleLookupOffer.builder()
                .productNumber("P-001")
                .notes(Notes.of(Note.of("Some description"), null, null, null))
                .createdAt(Instant.parse("2024-03-12T13:00:00.00Z"))
                .build()
                .toModel();
        var offer2 = SampleLookupOffer.builder()
                .productNumber("P-002")
                .notes(Notes.of(Note.of("Other description"), null, null, null))
                .createdAt(Instant.parse("2024-03-12T12:00:00.00Z"))
                .build()
                .toModel();
        update(offer1);
        update(offer2);

        var allIds = Set.of(offer1.getId(), offer2.getId());
        assertThat(find(allIds, "P-001", 0, 10)).containsExactly(offer1);
        assertThat(find(allIds, "P-002", 0, 10)).containsExactly(offer2);
        assertThat(find(allIds, "P-00", 0, 10)).containsExactly(offer1, offer2);
    }

    @Test
    void shouldFindPublishedOffersByProductNumber() {
        var offer1 = SampleLookupOffer.builder()
                .productNumber("P-100")
                .notes(Notes.of(Note.of("Cotton dress"), null, null, null))
                .published(true)
                .createdAt(Instant.parse("2024-03-12T13:00:00.00Z"))
                .build()
                .toModel();
        var offer2 = SampleLookupOffer.builder()
                .productNumber("P-200")
                .notes(Notes.of(Note.of("Silk gown"), null, null, null))
                .published(true)
                .createdAt(Instant.parse("2024-03-12T12:00:00.00Z"))
                .build()
                .toModel();
        update(offer1);
        update(offer2);

        var result = findPublished("P-100", 0, 10);
        assertThat(result.getResults()).containsExactly(offer1);
        assertThat(result.getTotal()).isEqualTo(1);
    }

    @Test
    void shouldFindOffersBySearchTermCaseInsensitive() {
        var offer = SampleLookupOffer.builder()
                .notes(Notes.of(Note.of("Cotton summer dress"), null, null, null))
                .createdAt(Instant.parse("2024-03-12T13:00:00.00Z"))
                .build()
                .toModel();
        update(offer);

        var ids = Set.of(offer.getId());
        assertThat(find(ids, "COTTON", 0, 10)).containsExactly(offer);
        assertThat(find(ids, "cotton", 0, 10)).containsExactly(offer);
        assertThat(find(ids, "Cotton", 0, 10)).containsExactly(offer);
    }

    @Test
    void shouldFindPublishedOffersBySearchTerm() {
        var offer1 = SampleLookupOffer.builder()
                .notes(Notes.of(Note.of("Cotton summer dress"), null, null, null))
                .published(true)
                .createdAt(Instant.parse("2024-03-12T13:00:00.00Z"))
                .build()
                .toModel();
        var offer2 = SampleLookupOffer.builder()
                .notes(Notes.of(Note.of("Silk evening gown"), null, null, null))
                .published(true)
                .createdAt(Instant.parse("2024-03-12T12:00:00.00Z"))
                .build()
                .toModel();
        var offer3 = SampleLookupOffer.builder()
                .notes(Notes.of(Note.of("Cotton winter coat"), null, null, null))
                .createdAt(Instant.parse("2024-03-12T11:00:00.00Z"))
                .build()
                .toModel();
        update(offer1);
        update(offer2);
        update(offer3);

        var result = findPublished("cotton", 0, 10);
        assertThat(result.getResults()).containsExactly(offer1);
        assertThat(result.getTotal()).isEqualTo(1);

        result = findPublished("silk", 0, 10);
        assertThat(result.getResults()).containsExactly(offer2);
        assertThat(result.getTotal()).isEqualTo(1);
    }

    @Test
    void shouldFindPublishedOffersWithPaging() {
        var offer1 = SampleLookupOffer.builder()
                .published(true)
                .createdAt(Instant.parse("2024-03-12T13:00:00.00Z"))
                .build()
                .toModel();
        var offer2 = SampleLookupOffer.builder()
                .published(true)
                .createdAt(Instant.parse("2024-03-12T09:00:00.00Z"))
                .build()
                .toModel();
        update(offer1);
        update(offer2);

        var result = findPublished("", 0, 1);
        assertThat(result.getResults()).containsExactly(offer1);
        assertThat(result.getTotal()).isEqualTo(2);

        result = findPublished("", 1, 1);
        assertThat(result.getResults()).containsExactly(offer2);
        assertThat(result.getTotal()).isEqualTo(2);
    }

    @Test
    void shouldFindOffersByProductId() {
        var productId1 = ProductId.of("PRODUCT_1");
        var productId2 = ProductId.of("PRODUCT_2");

        var offer1 = SampleLookupOffer.builder()
                .productId("PRODUCT_1")
                .createdAt(Instant.parse("2024-03-12T13:00:00.00Z"))
                .build()
                .toModel();
        var offer2 = SampleLookupOffer.builder()
                .productId("PRODUCT_1")
                .createdAt(Instant.parse("2024-03-12T12:00:00.00Z"))
                .build()
                .toModel();
        var offer3 = SampleLookupOffer.builder()
                .productId("PRODUCT_2")
                .createdAt(Instant.parse("2024-03-12T11:00:00.00Z"))
                .build()
                .toModel();
        update(offer1);
        update(offer2);
        update(offer3);

        var offers = findByProductId(productId1);
        assertThat(offers).containsExactlyInAnyOrder(offer1, offer2);

        offers = findByProductId(productId2);
        assertThat(offers).containsExactly(offer3);

        offers = findByProductId(ProductId.of("UNKNOWN"));
        assertThat(offers).isEmpty();
    }

    @Test
    void shouldFilterPublishedOffersByCategory() {
        var offer1 = SampleLookupOffer.builder()
                .categories(Set.of(OfferCategoryId.of("CAT_1")))
                .published(true)
                .createdAt(Instant.parse("2024-03-12T13:00:00.00Z"))
                .build()
                .toModel();
        var offer2 = SampleLookupOffer.builder()
                .categories(Set.of(OfferCategoryId.of("CAT_2")))
                .published(true)
                .createdAt(Instant.parse("2024-03-12T12:00:00.00Z"))
                .build()
                .toModel();
        var offer3 = SampleLookupOffer.builder()
                .categories(Set.of(OfferCategoryId.of("CAT_1"), OfferCategoryId.of("CAT_2")))
                .published(true)
                .createdAt(Instant.parse("2024-03-12T11:00:00.00Z"))
                .build()
                .toModel();
        update(offer1);
        update(offer2);
        update(offer3);

        var result = findPublished("", Set.of(OfferCategoryId.of("CAT_1")), Set.of(), null, null, null, null, 0, 100);
        assertThat(result.getTotal()).isEqualTo(2);
        assertThat(result.getResults()).containsExactly(offer1, offer3);

        result = findPublished("", Set.of(OfferCategoryId.of("CAT_2")), Set.of(), null, null, null, null, 0, 100);
        assertThat(result.getTotal()).isEqualTo(2);
        assertThat(result.getResults()).containsExactly(offer2, offer3);

        result = findPublished("", Set.of(OfferCategoryId.of("CAT_1"), OfferCategoryId.of("CAT_2")), Set.of(), null, null, null, null, 0, 100);
        assertThat(result.getTotal()).isEqualTo(3);
    }

    @Test
    void shouldFilterPublishedOffersBySize() {
        var offer1 = SampleLookupOffer.builder()
                .size(OfferSize.of("S"))
                .published(true)
                .createdAt(Instant.parse("2024-03-12T13:00:00.00Z"))
                .build()
                .toModel();
        var offer2 = SampleLookupOffer.builder()
                .size(OfferSize.of("M"))
                .published(true)
                .createdAt(Instant.parse("2024-03-12T12:00:00.00Z"))
                .build()
                .toModel();
        var offer3 = SampleLookupOffer.builder()
                .size(OfferSize.of("L"))
                .published(true)
                .createdAt(Instant.parse("2024-03-12T11:00:00.00Z"))
                .build()
                .toModel();
        update(offer1);
        update(offer2);
        update(offer3);

        var result = findPublished("", Set.of(), Set.of(OfferSize.of("S")), null, null, null, null, 0, 100);
        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getResults()).containsExactly(offer1);

        result = findPublished("", Set.of(), Set.of(OfferSize.of("S"), OfferSize.of("L")), null, null, null, null, 0, 100);
        assertThat(result.getTotal()).isEqualTo(2);
        assertThat(result.getResults()).containsExactly(offer1, offer3);
    }

    @Test
    void shouldFilterPublishedOffersByPriceRange() {
        var offer1 = SampleLookupOffer.builder()
                .pricing(Pricing.of(Money.of(1000L, Currency.euro())))
                .published(true)
                .createdAt(Instant.parse("2024-03-12T13:00:00.00Z"))
                .build()
                .toModel();
        var offer2 = SampleLookupOffer.builder()
                .pricing(Pricing.of(Money.of(2000L, Currency.euro())))
                .published(true)
                .createdAt(Instant.parse("2024-03-12T12:00:00.00Z"))
                .build()
                .toModel();
        var offer3 = SampleLookupOffer.builder()
                .pricing(Pricing.of(Money.of(3000L, Currency.euro())))
                .published(true)
                .createdAt(Instant.parse("2024-03-12T11:00:00.00Z"))
                .build()
                .toModel();
        update(offer1);
        update(offer2);
        update(offer3);

        var result = findPublished("", Set.of(), Set.of(), 1500L, 2500L, null, null, 0, 100);
        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getResults()).containsExactly(offer2);

        result = findPublished("", Set.of(), Set.of(), 2000L, null, null, null, 0, 100);
        assertThat(result.getTotal()).isEqualTo(2);

        result = findPublished("", Set.of(), Set.of(), null, 2000L, null, null, 0, 100);
        assertThat(result.getTotal()).isEqualTo(2);
    }

    @Test
    void shouldFilterByEffectivePriceWhenDiscounted() {
        var offer1 = SampleLookupOffer.builder()
                .pricing(Pricing.of(
                        Money.of(3000L, Currency.euro()),
                        Money.of(1500L, Currency.euro()),
                        List.of()
                ))
                .published(true)
                .createdAt(Instant.parse("2024-03-12T13:00:00.00Z"))
                .build()
                .toModel();
        var offer2 = SampleLookupOffer.builder()
                .pricing(Pricing.of(Money.of(2000L, Currency.euro())))
                .published(true)
                .createdAt(Instant.parse("2024-03-12T12:00:00.00Z"))
                .build()
                .toModel();
        update(offer1);
        update(offer2);

        var result = findPublished("", Set.of(), Set.of(), null, 1600L, null, null, 0, 100);
        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getResults()).containsExactly(offer1);

        result = findPublished("", Set.of(), Set.of(), 1600L, null, null, null, 0, 100);
        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getResults()).containsExactly(offer2);
    }

    @Test
    void shouldSortPublishedOffersByAlphabetical() {
        var offer1 = SampleLookupOffer.builder()
                .title(OfferTitle.of("Banana"))
                .published(true)
                .createdAt(Instant.parse("2024-03-12T13:00:00.00Z"))
                .build()
                .toModel();
        var offer2 = SampleLookupOffer.builder()
                .title(OfferTitle.of("Apple"))
                .published(true)
                .createdAt(Instant.parse("2024-03-12T12:00:00.00Z"))
                .build()
                .toModel();
        var offer3 = SampleLookupOffer.builder()
                .title(OfferTitle.of("Cherry"))
                .published(true)
                .createdAt(Instant.parse("2024-03-12T11:00:00.00Z"))
                .build()
                .toModel();
        update(offer1);
        update(offer2);
        update(offer3);

        var result = findPublished("", Set.of(), Set.of(), null, null, OfferSortProperty.ALPHABETICAL, OfferSortDirection.ASCENDING, 0, 100);
        assertThat(result.getResults()).containsExactly(offer2, offer1, offer3);

        result = findPublished("", Set.of(), Set.of(), null, null, OfferSortProperty.ALPHABETICAL, OfferSortDirection.DESCENDING, 0, 100);
        assertThat(result.getResults()).containsExactly(offer3, offer1, offer2);
    }

    @Test
    void shouldSortPublishedOffersByPrice() {
        var offer1 = SampleLookupOffer.builder()
                .pricing(Pricing.of(Money.of(3000L, Currency.euro())))
                .published(true)
                .createdAt(Instant.parse("2024-03-12T13:00:00.00Z"))
                .build()
                .toModel();
        var offer2 = SampleLookupOffer.builder()
                .pricing(Pricing.of(Money.of(1000L, Currency.euro())))
                .published(true)
                .createdAt(Instant.parse("2024-03-12T12:00:00.00Z"))
                .build()
                .toModel();
        var offer3 = SampleLookupOffer.builder()
                .pricing(Pricing.of(Money.of(2000L, Currency.euro())))
                .published(true)
                .createdAt(Instant.parse("2024-03-12T11:00:00.00Z"))
                .build()
                .toModel();
        update(offer1);
        update(offer2);
        update(offer3);

        var result = findPublished("", Set.of(), Set.of(), null, null, OfferSortProperty.PRICE, OfferSortDirection.ASCENDING, 0, 100);
        assertThat(result.getResults()).containsExactly(offer2, offer3, offer1);

        result = findPublished("", Set.of(), Set.of(), null, null, OfferSortProperty.PRICE, OfferSortDirection.DESCENDING, 0, 100);
        assertThat(result.getResults()).containsExactly(offer1, offer3, offer2);
    }

    @Test
    void shouldSortPublishedOffersByEffectivePrice() {
        var offer1 = SampleLookupOffer.builder()
                .pricing(Pricing.of(
                        Money.of(5000L, Currency.euro()),
                        Money.of(1000L, Currency.euro()),
                        List.of()
                ))
                .published(true)
                .createdAt(Instant.parse("2024-03-12T13:00:00.00Z"))
                .build()
                .toModel();
        var offer2 = SampleLookupOffer.builder()
                .pricing(Pricing.of(Money.of(2000L, Currency.euro())))
                .published(true)
                .createdAt(Instant.parse("2024-03-12T12:00:00.00Z"))
                .build()
                .toModel();
        update(offer1);
        update(offer2);

        var result = findPublished("", Set.of(), Set.of(), null, null, OfferSortProperty.PRICE, OfferSortDirection.ASCENDING, 0, 100);
        assertThat(result.getResults()).containsExactly(offer1, offer2);
    }

    @Test
    void shouldFindDistinctPublishedSizes() {
        var offer1 = SampleLookupOffer.builder()
                .size(OfferSize.of("M"))
                .published(true)
                .createdAt(Instant.parse("2024-03-12T13:00:00.00Z"))
                .build()
                .toModel();
        var offer2 = SampleLookupOffer.builder()
                .size(OfferSize.of("S"))
                .published(true)
                .createdAt(Instant.parse("2024-03-12T12:00:00.00Z"))
                .build()
                .toModel();
        var offer3 = SampleLookupOffer.builder()
                .size(OfferSize.of("M"))
                .published(true)
                .createdAt(Instant.parse("2024-03-12T11:00:00.00Z"))
                .build()
                .toModel();
        var offer4 = SampleLookupOffer.builder()
                .size(OfferSize.of("L"))
                .published(false)
                .createdAt(Instant.parse("2024-03-12T10:00:00.00Z"))
                .build()
                .toModel();
        update(offer1);
        update(offer2);
        update(offer3);
        update(offer4);

        var sizes = findDistinctPublishedSizes();
        assertThat(sizes).containsExactly("M", "S");
    }

    private void update(LookupOffer offer) {
        repo.update(offer).block();
    }

    private void remove(OfferId id) {
        repo.remove(id).block();
    }

    private List<LookupOffer> find(Collection<OfferId> offerIds) {
        return find(offerIds, 0, Integer.MAX_VALUE);
    }

    private List<LookupOffer> find(Collection<OfferId> offerIds, long skip, long limit) {
        return find(offerIds, "", skip, limit);
    }

    private List<LookupOffer> find(Collection<OfferId> offerIds, String searchTerm, long skip, long limit) {
        return repo.find(offerIds, searchTerm, skip, limit).block().getResults();
    }

    private LookupOffer findPublished(OfferId id) {
        return repo.findPublished(id).block();
    }

    private LookupOfferPage findPublished(String searchTerm, long skip, long limit) {
        return repo.findPublished(PublishedOfferQuery.of(searchTerm, Set.of(), Set.of(), null, null, null, null, skip, limit)).block();
    }

    private LookupOfferPage findPublished(
            String searchTerm,
            Set<OfferCategoryId> categories,
            Set<OfferSize> sizes,
            Long minPrice,
            Long maxPrice,
            OfferSortProperty sortProperty,
            OfferSortDirection sortDirection,
            long skip,
            long limit
    ) {
        return repo.findPublished(PublishedOfferQuery.of(searchTerm, categories, sizes, minPrice, maxPrice, sortProperty, sortDirection, skip, limit)).block();
    }

    private List<LookupOffer> findByProductId(ProductId productId) {
        return repo.findByProductId(productId).collectList().block();
    }

    private List<String> findDistinctPublishedSizes() {
        return repo.findDistinctPublishedSizes().collectList().block();
    }

}
