package de.bennyboer.kicherkrabbe.offers;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.offers.api.NotesDTO;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UpdateNotesTest extends OffersModuleTest {

    @Test
    void shouldUpdateOfferNotesAsUser() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));
        String offerId = createSampleOffer(agent);

        var notes = new NotesDTO();
        notes.description = "New description";
        notes.contains = "New contains";
        notes.care = "New care";
        notes.safety = "New safety";
        updateOfferNotes(offerId, 0L, notes, agent);

        var offers = getOffers(agent);
        assertThat(offers).hasSize(1);
        var offer = offers.getFirst();
        assertThat(offer.getVersion()).isEqualTo(Version.of(1));
        assertThat(offer.getNotes().getDescription()).isEqualTo(Note.of("New description"));
        assertThat(offer.getNotes().getContains()).isPresent();
        assertThat(offer.getNotes().getContains().get()).isEqualTo(Note.of("New contains"));
        assertThat(offer.getNotes().getCare()).isPresent();
        assertThat(offer.getNotes().getCare().get()).isEqualTo(Note.of("New care"));
        assertThat(offer.getNotes().getSafety()).isPresent();
        assertThat(offer.getNotes().getSafety().get()).isEqualTo(Note.of("New safety"));
    }

    @Test
    void shouldUpdateNotesWithNullableFields() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));
        String offerId = createSampleOffer(agent);

        var notes = new NotesDTO();
        notes.description = "Only description";
        updateOfferNotes(offerId, 0L, notes, agent);

        var offer = getOffer(offerId, agent);
        assertThat(offer.getNotes().getDescription()).isEqualTo(Note.of("Only description"));
        assertThat(offer.getNotes().getContains()).isEmpty();
        assertThat(offer.getNotes().getCare()).isEmpty();
        assertThat(offer.getNotes().getSafety()).isEmpty();
    }

    @Test
    void shouldNotUpdateNotesGivenAnOutdatedVersion() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));
        String offerId = createSampleOffer(agent);

        var notes = new NotesDTO();
        notes.description = "Updated";
        updateOfferNotes(offerId, 0L, notes, agent);

        var outdatedNotes = new NotesDTO();
        outdatedNotes.description = "Outdated";
        assertThatThrownBy(() -> updateOfferNotes(offerId, 0L, outdatedNotes, agent))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldNotUpdateNotesWhenUserIsNotAllowed() {
        var notes = new NotesDTO();
        notes.description = "Description";
        assertThatThrownBy(() -> updateOfferNotes("OFFER_ID", 0L, notes, Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

}
