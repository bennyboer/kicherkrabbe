package de.bennyboer.kicherkrabbe.offers;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.LoggingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.inmemory.InMemoryEventSourcingRepo;
import de.bennyboer.kicherkrabbe.money.Currency;
import de.bennyboer.kicherkrabbe.money.Money;
import de.bennyboer.kicherkrabbe.offers.archive.NotReservedForArchiveError;
import de.bennyboer.kicherkrabbe.offers.delete.CannotDeleteNonDraftError;
import de.bennyboer.kicherkrabbe.offers.publish.AlreadyPublishedError;
import de.bennyboer.kicherkrabbe.offers.reserve.AlreadyReservedError;
import de.bennyboer.kicherkrabbe.offers.reserve.NotPublishedError;
import de.bennyboer.kicherkrabbe.offers.samples.SampleOffer;
import de.bennyboer.kicherkrabbe.offers.unpublish.AlreadyUnpublishedError;
import de.bennyboer.kicherkrabbe.offers.unpublish.CannotUnpublishReservedError;
import de.bennyboer.kicherkrabbe.offers.unreserve.NotReservedError;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OfferServiceTest {

    private final EventSourcingRepo repo = new InMemoryEventSourcingRepo();

    private final LoggingEventPublisher eventPublisher = new LoggingEventPublisher();

    private final OfferService offerService = new OfferService(repo, eventPublisher, Clock.systemUTC());

    @Test
    void shouldCreateOffer() {
        var sample = SampleOffer.builder().build();
        var id = create(sample);

        var offer = get(id);
        assertThat(offer.getId()).isEqualTo(id);
        assertThat(offer.getVersion()).isEqualTo(Version.zero());
        assertThat(offer.getProductId()).isEqualTo(sample.getProductId());
        assertThat(offer.getImages()).isEqualTo(sample.getImageIds());
        assertThat(offer.getNotes()).isEqualTo(sample.getNotes());
        assertThat(offer.getPricing().getPrice()).isEqualTo(sample.getPrice());
        assertThat(offer.isPublished()).isFalse();
        assertThat(offer.isReserved()).isFalse();
        assertThat(offer.isNotDeleted()).isTrue();
    }

    @Test
    void shouldDeleteOffer() {
        var id = create();

        delete(id, Version.zero());

        var offer = get(id);
        assertThat(offer).isNull();
    }

    @Test
    void shouldNotDeleteOfferGivenAnOutdatedVersion() {
        var id = create();

        updateNotes(id, Version.zero(), Notes.of(Note.of("updated"), null, null, null));

        assertThatThrownBy(() -> delete(id, Version.zero()))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldNotDeletePublishedOffer() {
        var id = create();

        publish(id, Version.zero());

        assertThatThrownBy(() -> delete(id, Version.of(1)))
                .isInstanceOf(CannotDeleteNonDraftError.class);
    }

    @Test
    void shouldPublishOffer() {
        var id = create();

        var updatedVersion = publish(id, Version.zero());

        var offer = get(id);
        assertThat(offer.getVersion()).isEqualTo(updatedVersion);
        assertThat(offer.isPublished()).isTrue();
    }

    @Test
    void shouldNotPublishOfferGivenAnOutdatedVersion() {
        var id = create();

        updateNotes(id, Version.zero(), Notes.of(Note.of("updated"), null, null, null));

        assertThatThrownBy(() -> publish(id, Version.zero()))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldRaiseErrorIfOfferAlreadyPublished() {
        var id = create();

        publish(id, Version.zero());

        assertThatThrownBy(() -> publish(id, Version.of(1)))
                .isInstanceOf(AlreadyPublishedError.class);
    }

    @Test
    void shouldUnpublishOffer() {
        var id = create();

        publish(id, Version.zero());

        var updatedVersion = unpublish(id, Version.of(1));

        var offer = get(id);
        assertThat(offer.getVersion()).isEqualTo(updatedVersion);
        assertThat(offer.isPublished()).isFalse();
    }

    @Test
    void shouldNotUnpublishOfferGivenAnOutdatedVersion() {
        var id = create();

        var version = publish(id, Version.zero());

        updateNotes(id, version, Notes.of(Note.of("updated"), null, null, null));

        assertThatThrownBy(() -> unpublish(id, Version.of(1)))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldNotUnpublishAlreadyUnpublishedOffer() {
        var id = create();

        assertThatThrownBy(() -> unpublish(id, Version.zero()))
                .isInstanceOf(AlreadyUnpublishedError.class);
    }

    @Test
    void shouldNotUnpublishReservedOffer() {
        var id = create();

        publish(id, Version.zero());
        reserve(id, Version.of(1));

        assertThatThrownBy(() -> unpublish(id, Version.of(2)))
                .isInstanceOf(CannotUnpublishReservedError.class);
    }

    @Test
    void shouldReserveOffer() {
        var id = create();

        publish(id, Version.zero());
        var updatedVersion = reserve(id, Version.of(1));

        var offer = get(id);
        assertThat(offer.getVersion()).isEqualTo(updatedVersion);
        assertThat(offer.isReserved()).isTrue();
    }

    @Test
    void shouldNotReserveOfferGivenAnOutdatedVersion() {
        var id = create();

        publish(id, Version.zero());
        updateNotes(id, Version.of(1), Notes.of(Note.of("updated"), null, null, null));

        assertThatThrownBy(() -> reserve(id, Version.of(1)))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldNotReserveUnpublishedOffer() {
        var id = create();

        assertThatThrownBy(() -> reserve(id, Version.zero()))
                .isInstanceOf(NotPublishedError.class);
    }

    @Test
    void shouldRaiseErrorIfOfferAlreadyReserved() {
        var id = create();

        publish(id, Version.zero());
        reserve(id, Version.of(1));

        assertThatThrownBy(() -> reserve(id, Version.of(2)))
                .isInstanceOf(AlreadyReservedError.class);
    }

    @Test
    void shouldUnreserveOffer() {
        var id = create();

        publish(id, Version.zero());
        reserve(id, Version.of(1));
        var updatedVersion = unreserve(id, Version.of(2));

        var offer = get(id);
        assertThat(offer.getVersion()).isEqualTo(updatedVersion);
        assertThat(offer.isReserved()).isFalse();
    }

    @Test
    void shouldNotUnreserveOfferGivenAnOutdatedVersion() {
        var id = create();

        publish(id, Version.zero());
        reserve(id, Version.of(1));
        updateNotes(id, Version.of(2), Notes.of(Note.of("updated"), null, null, null));

        assertThatThrownBy(() -> unreserve(id, Version.of(2)))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldNotUnreserveNotReservedOffer() {
        var id = create();

        assertThatThrownBy(() -> unreserve(id, Version.zero()))
                .isInstanceOf(NotReservedError.class);
    }

    @Test
    void shouldArchiveOffer() {
        var id = create();

        publish(id, Version.zero());
        reserve(id, Version.of(1));
        var updatedVersion = archive(id, Version.of(2));

        var offer = get(id);
        assertThat(offer).isNotNull();
        assertThat(offer.getVersion()).isEqualTo(updatedVersion);
        assertThat(offer.isArchived()).isTrue();
        assertThat(offer.isPublished()).isFalse();
        assertThat(offer.isReserved()).isFalse();
    }

    @Test
    void shouldNotArchiveNotReservedOffer() {
        var id = create();

        assertThatThrownBy(() -> archive(id, Version.zero()))
                .isInstanceOf(NotReservedForArchiveError.class);
    }

    @Test
    void shouldUpdateImages() {
        var id = create();

        var updatedVersion = updateImages(id, Version.zero(), List.of(ImageId.of("IMAGE_1"), ImageId.of("IMAGE_2")));

        var offer = get(id);
        assertThat(offer.getVersion()).isEqualTo(updatedVersion);
        assertThat(offer.getImages()).containsExactly(ImageId.of("IMAGE_1"), ImageId.of("IMAGE_2"));
    }

    @Test
    void shouldNotUpdateImagesGivenAnOutdatedVersion() {
        var id = create();

        updateImages(id, Version.zero(), List.of(ImageId.of("IMAGE_1")));

        assertThatThrownBy(() -> updateImages(id, Version.zero(), List.of(ImageId.of("IMAGE_2"))))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldUpdateNotes() {
        var id = create();

        var updatedNotes = Notes.of(Note.of("updated description"), Note.of("contains info"), Note.of("care info"), Note.of("safety info"));
        var updatedVersion = updateNotes(id, Version.zero(), updatedNotes);

        var offer = get(id);
        assertThat(offer.getVersion()).isEqualTo(updatedVersion);
        assertThat(offer.getNotes()).isEqualTo(updatedNotes);
    }

    @Test
    void shouldNotUpdateNotesGivenAnOutdatedVersion() {
        var id = create();

        updateNotes(id, Version.zero(), Notes.of(Note.of("updated"), null, null, null));

        assertThatThrownBy(() -> updateNotes(id, Version.zero(), Notes.of(Note.of("updated again"), null, null, null)))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldUpdatePrice() {
        var id = create();

        var updatedVersion = updatePrice(id, Version.zero(), Money.of(2999L, Currency.euro()));

        var offer = get(id);
        assertThat(offer.getVersion()).isEqualTo(updatedVersion);
        assertThat(offer.getPricing().getPrice()).isEqualTo(Money.of(2999L, Currency.euro()));
        assertThat(offer.getPricing().getPriceHistory()).hasSize(1);
        assertThat(offer.getPricing().getPriceHistory().getFirst().getPrice()).isEqualTo(Money.of(1999L, Currency.euro()));
    }

    @Test
    void shouldNotUpdatePriceGivenAnOutdatedVersion() {
        var id = create();

        updatePrice(id, Version.zero(), Money.of(2999L, Currency.euro()));

        assertThatThrownBy(() -> updatePrice(id, Version.zero(), Money.of(3999L, Currency.euro())))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldAddDiscount() {
        var id = create();

        var updatedVersion = addDiscount(id, Version.zero(), Money.of(999L, Currency.euro()));

        var offer = get(id);
        assertThat(offer.getVersion()).isEqualTo(updatedVersion);
        assertThat(offer.getPricing().getDiscountedPrice()).isPresent();
        assertThat(offer.getPricing().getDiscountedPrice().get()).isEqualTo(Money.of(999L, Currency.euro()));
    }

    @Test
    void shouldNotAddDiscountGivenAnOutdatedVersion() {
        var id = create();

        addDiscount(id, Version.zero(), Money.of(999L, Currency.euro()));

        assertThatThrownBy(() -> addDiscount(id, Version.zero(), Money.of(899L, Currency.euro())))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldRemoveDiscount() {
        var id = create();

        addDiscount(id, Version.zero(), Money.of(999L, Currency.euro()));
        var updatedVersion = removeDiscount(id, Version.of(1));

        var offer = get(id);
        assertThat(offer.getVersion()).isEqualTo(updatedVersion);
        assertThat(offer.getPricing().getDiscountedPrice()).isEmpty();
    }

    @Test
    void shouldNotRemoveDiscountGivenAnOutdatedVersion() {
        var id = create();

        addDiscount(id, Version.zero(), Money.of(999L, Currency.euro()));
        updateNotes(id, Version.of(1), Notes.of(Note.of("updated"), null, null, null));

        assertThatThrownBy(() -> removeDiscount(id, Version.of(1)))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldSnapshotEvery100Events() {
        var id = create();

        var version = Version.zero();
        for (int i = 0; i < 200; i++) {
            version = updateNotes(id, version, Notes.of(Note.of("description " + i), null, null, null));
        }

        var offer = get(id);
        assertThat(offer.getVersion()).isEqualTo(Version.of(202));
        assertThat(offer.getNotes()).isEqualTo(Notes.of(Note.of("description 199"), null, null, null));

        var events = repo.findEventsByAggregateIdAndType(
                AggregateId.of(id.getValue()),
                Offer.TYPE,
                Version.zero()
        ).collectList().block();
        var snapshotEvents = events.stream().filter(e -> e.getMetadata().isSnapshot()).toList();
        assertThat(snapshotEvents).hasSize(2);
        assertThat(snapshotEvents.getFirst().getMetadata().getAggregateVersion()).isEqualTo(Version.of(100));
        assertThat(snapshotEvents.getLast().getMetadata().getAggregateVersion()).isEqualTo(Version.of(200));
    }

    private Offer get(OfferId id) {
        return offerService.get(id).block();
    }

    private OfferId create() {
        var sample = SampleOffer.builder().build();
        return create(sample);
    }

    private OfferId create(SampleOffer sample) {
        return offerService.create(
                sample.getProductId(),
                sample.getImageIds(),
                sample.getNotes(),
                sample.getPrice(),
                Agent.system()
        ).block().getId();
    }

    private Version delete(OfferId id, Version version) {
        return offerService.delete(id, version, Agent.system()).block();
    }

    private Version publish(OfferId id, Version version) {
        return offerService.publish(id, version, Agent.system()).block();
    }

    private Version unpublish(OfferId id, Version version) {
        return offerService.unpublish(id, version, Agent.system()).block();
    }

    private Version reserve(OfferId id, Version version) {
        return offerService.reserve(id, version, Agent.system()).block();
    }

    private Version unreserve(OfferId id, Version version) {
        return offerService.unreserve(id, version, Agent.system()).block();
    }

    private Version archive(OfferId id, Version version) {
        return offerService.archive(id, version, Agent.system()).block();
    }

    private Version updateImages(OfferId id, Version version, List<ImageId> images) {
        return offerService.updateImages(id, version, images, Agent.system()).block();
    }

    private Version updateNotes(OfferId id, Version version, Notes notes) {
        return offerService.updateNotes(id, version, notes, Agent.system()).block();
    }

    private Version updatePrice(OfferId id, Version version, Money price) {
        return offerService.updatePrice(id, version, price, Agent.system()).block();
    }

    private Version addDiscount(OfferId id, Version version, Money discountedPrice) {
        return offerService.addDiscount(id, version, discountedPrice, Agent.system()).block();
    }

    private Version removeDiscount(OfferId id, Version version) {
        return offerService.removeDiscount(id, version, Agent.system()).block();
    }

}
