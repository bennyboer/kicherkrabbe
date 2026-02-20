package de.bennyboer.kicherkrabbe.offers.persistence.lookup;

import de.bennyboer.kicherkrabbe.offers.OfferId;
import de.bennyboer.kicherkrabbe.offers.ProductId;
import de.bennyboer.kicherkrabbe.offers.samples.SampleLookupOffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
                .notes(de.bennyboer.kicherkrabbe.offers.Notes.of(
                        de.bennyboer.kicherkrabbe.offers.Note.of("Cotton summer dress"), null, null, null
                ))
                .createdAt(Instant.parse("2024-03-12T13:00:00.00Z"))
                .build()
                .toModel();
        var offer2 = SampleLookupOffer.builder()
                .notes(de.bennyboer.kicherkrabbe.offers.Notes.of(
                        de.bennyboer.kicherkrabbe.offers.Note.of("Silk evening gown"), null, null, null
                ))
                .createdAt(Instant.parse("2024-03-12T12:00:00.00Z"))
                .build()
                .toModel();
        var offer3 = SampleLookupOffer.builder()
                .notes(de.bennyboer.kicherkrabbe.offers.Notes.of(
                        de.bennyboer.kicherkrabbe.offers.Note.of("Cotton winter coat"), null, null, null
                ))
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
    void shouldFindOffersBySearchTermCaseInsensitive() {
        var offer = SampleLookupOffer.builder()
                .notes(de.bennyboer.kicherkrabbe.offers.Notes.of(
                        de.bennyboer.kicherkrabbe.offers.Note.of("Cotton summer dress"), null, null, null
                ))
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
                .notes(de.bennyboer.kicherkrabbe.offers.Notes.of(
                        de.bennyboer.kicherkrabbe.offers.Note.of("Cotton summer dress"), null, null, null
                ))
                .published(true)
                .createdAt(Instant.parse("2024-03-12T13:00:00.00Z"))
                .build()
                .toModel();
        var offer2 = SampleLookupOffer.builder()
                .notes(de.bennyboer.kicherkrabbe.offers.Notes.of(
                        de.bennyboer.kicherkrabbe.offers.Note.of("Silk evening gown"), null, null, null
                ))
                .published(true)
                .createdAt(Instant.parse("2024-03-12T12:00:00.00Z"))
                .build()
                .toModel();
        var offer3 = SampleLookupOffer.builder()
                .notes(de.bennyboer.kicherkrabbe.offers.Notes.of(
                        de.bennyboer.kicherkrabbe.offers.Note.of("Cotton winter coat"), null, null, null
                ))
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
        return repo.findPublished(searchTerm, skip, limit).block();
    }

    private List<LookupOffer> findByProductId(ProductId productId) {
        return repo.findByProductId(productId).collectList().block();
    }

}
