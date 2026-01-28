package de.bennyboer.kicherkrabbe.products;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import de.bennyboer.kicherkrabbe.products.api.requests.UpdateProducedAtDateRequest;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DeleteProductTest extends ProductsModuleTest {

    @Test
    void shouldDeleteProduct() {
        // given: a user is allowed to create products
        allowUserToCreateProductsAndReadLinks("USER_ID");

        // and: a product
        var result = createSampleProduct(Agent.user(AgentId.of("USER_ID")));

        // when: deleting the product
        deleteProduct(result.id, result.version, Agent.user(AgentId.of("USER_ID")));

        // then: the product is deleted; then: a missing permissions exception is raised
        assertThatThrownBy(() -> getProduct(result.id, Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldNotDeleteProductWhenUserDoesNotHavePermission() {
        // given: a user is allowed to create products
        allowUserToCreateProductsAndReadLinks("USER_ID");

        // and: a product
        var result = createSampleProduct(Agent.user(AgentId.of("USER_ID")));

        // when: another user tries to delete the product; then: an exception is raised
        assertThatThrownBy(() -> deleteProduct(result.id, result.version, Agent.user(AgentId.of("OTHER_USER_ID"))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldRaiseErrorWhenTheVersionIsNotUpToDate() {
        // given: the user is allowed to create products
        allowUserToCreateProductsAndReadLinks("USER_ID");

        // and: a product
        var result = createSampleProduct(Agent.user(AgentId.of("USER_ID")));

        // and: the product is updated
        var updateProducedAtRequest = new UpdateProducedAtDateRequest();
        updateProducedAtRequest.version = 0L;
        updateProducedAtRequest.producedAt = Instant.parse("2024-12-12T12:12:00.000Z");
        updateProducedAt(result.id, updateProducedAtRequest, Agent.user(AgentId.of("USER_ID")));

        // when: the product is deleted with an outdated version; then: an exception is raised
        assertThatThrownBy(() -> deleteProduct(result.id, 0L, Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldAllowNoFurtherOperationsOnDeletedProduct() {
        // given: a user is allowed to create products
        allowUserToCreateProductsAndReadLinks("USER_ID");

        // and: a deleted product
        var result = createSampleProduct(Agent.user(AgentId.of("USER_ID")));
        var version = deleteProduct(result.id, result.version, Agent.user(AgentId.of("USER_ID"))).version;

        // when: trying to update the product; then: an exception is raised
        var updateProducedAtRequest = new UpdateProducedAtDateRequest();
        updateProducedAtRequest.version = version;
        updateProducedAtRequest.producedAt = Instant.parse("2024-12-12T12:12:00.000Z");
        assertThatThrownBy(() -> updateProducedAt(result.id, updateProducedAtRequest, Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldRaiseMissingPermissionErrorIfProductDoesNotExist() {
        // when: a product that does not exist is deleted; then: an exception is raised
        assertThatThrownBy(() -> deleteProduct("PRODUCT_ID", 0L, Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

}
