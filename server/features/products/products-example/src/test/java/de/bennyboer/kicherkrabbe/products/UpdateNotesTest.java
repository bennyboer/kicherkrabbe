package de.bennyboer.kicherkrabbe.products;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import de.bennyboer.kicherkrabbe.products.api.NotesDTO;
import de.bennyboer.kicherkrabbe.products.api.requests.UpdateNotesRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UpdateNotesTest extends ProductsModuleTest {

    @Test
    void shouldUpdateNotes() {
        // given: the user is allowed to create products
        allowUserToCreateProductsAndReadLinks("USER_ID");

        // and: a product
        var result = createSampleProduct(Agent.user(AgentId.of("USER_ID")));

        // when: the products notes are updated
        var updateNotesRequest = new UpdateNotesRequest();
        updateNotesRequest.version = 0L;
        updateNotesRequest.notes = new NotesDTO();
        updateNotesRequest.notes.contains = "Hello world";
        updateNotesRequest.notes.care = "I don't really care";
        updateNotesRequest.notes.safety = "This is important";
        updateNotes(result.id, updateNotesRequest, Agent.user(AgentId.of("USER_ID")));

        // then: the products notes are updated
        var product = getProduct(result.id, Agent.user(AgentId.of("USER_ID"))).product;
        assertThat(product.notes.contains).isEqualTo("Hello world");
        assertThat(product.notes.care).isEqualTo("I don't really care");
        assertThat(product.notes.safety).isEqualTo("This is important");
    }

    @Test
    void shouldNotUpdateNotesGivenNoPermission() {
        // given: the user is allowed to create products
        allowUserToCreateProductsAndReadLinks("USER_ID");

        // and: a product
        var result = createSampleProduct(Agent.user(AgentId.of("USER_ID")));

        // when: the product is updated by another user; then: an exception is thrown
        var updateNotesRequest = new UpdateNotesRequest();
        updateNotesRequest.version = 0L;
        updateNotesRequest.notes = new NotesDTO();
        updateNotesRequest.notes.contains = "Hello world";
        updateNotesRequest.notes.care = "I don't really care";
        updateNotesRequest.notes.safety = "This is important";
        assertThatThrownBy(() -> updateNotes(
                result.id,
                updateNotesRequest,
                Agent.user(AgentId.of("OTHER_USER_ID"))
        )).matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldRaiseErrorWhenTheVersionIsNotUpToDate() {
        // given: the user is allowed to create products
        allowUserToCreateProductsAndReadLinks("USER_ID");

        // and: a product
        var result = createSampleProduct(Agent.user(AgentId.of("USER_ID")));

        // and: the product is updated
        var updateNotesRequest = new UpdateNotesRequest();
        updateNotesRequest.version = result.version;
        updateNotesRequest.notes = new NotesDTO();
        updateNotesRequest.notes.contains = "Hello world";
        updateNotesRequest.notes.care = "I don't really care";
        updateNotesRequest.notes.safety = "This is important";
        updateNotes(result.id, updateNotesRequest, Agent.user(AgentId.of("USER_ID")));

        // when: the product is updated with an outdated version; then: an exception is thrown
        assertThatThrownBy(() -> updateNotes(
                result.id,
                updateNotesRequest,
                Agent.user(AgentId.of("USER_ID"))
        )).matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldRaiseErrorIfTryingToResetNotes() {
        // given: the user is allowed to create products
        allowUserToCreateProductsAndReadLinks("USER_ID");

        // and: a product
        var result = createSampleProduct(Agent.user(AgentId.of("USER_ID")));

        // and: the product is updated
        var updateNotesRequest = new UpdateNotesRequest();
        updateNotesRequest.version = result.version;
        updateNotesRequest.notes = new NotesDTO();
        updateNotesRequest.notes.contains = "Hello world";
        updateNotesRequest.notes.care = "I don't really care";
        updateNotesRequest.notes.safety = "This is important";
        updateNotes(result.id, updateNotesRequest, Agent.user(AgentId.of("USER_ID")));

        // when: the product is updated with a reset produced at date; then: an exception is thrown
        updateNotesRequest.version = 1L;
        updateNotesRequest.notes = null;
        assertThatThrownBy(() -> updateNotes(
                result.id,
                updateNotesRequest,
                Agent.user(AgentId.of("USER_ID"))
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRaiseMissingPermissionErrorIfProductDoesNotExist() {
        // when: a product that does not exist is updated; then: an exception is thrown
        var updateNotesRequest = new UpdateNotesRequest();
        updateNotesRequest.version = 0L;
        updateNotesRequest.notes = new NotesDTO();
        updateNotesRequest.notes.contains = "Hello world";
        updateNotesRequest.notes.care = "I don't really care";
        updateNotesRequest.notes.safety = "This is important";
        assertThatThrownBy(() -> updateNotes("PRODUCT_ID", updateNotesRequest, Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

}
