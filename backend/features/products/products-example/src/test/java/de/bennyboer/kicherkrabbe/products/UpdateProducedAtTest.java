package de.bennyboer.kicherkrabbe.products;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import de.bennyboer.kicherkrabbe.products.api.requests.UpdateProducedAtDateRequest;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UpdateProducedAtTest extends ProductsModuleTest {

    @Test
    void shouldUpdateProducedAtDate() {
        // given: the user is allowed to create products
        allowUserToCreateProductsAndReadLinks("USER_ID");

        // and: a product
        var result = createSampleProduct(Agent.user(AgentId.of("USER_ID")));

        // when: the products produced at date is updated
        var updateProducedAtRequest = new UpdateProducedAtDateRequest();
        updateProducedAtRequest.version = 0L;
        updateProducedAtRequest.producedAt = Instant.parse("2024-12-12T12:12:00.000Z");
        updateProducedAt(result.id, updateProducedAtRequest, Agent.user(AgentId.of("USER_ID")));

        // then: the products produced at date is updated
        var product = getProduct(result.id, Agent.user(AgentId.of("USER_ID"))).product;
        assertThat(product.producedAt).isEqualTo(Instant.parse("2024-12-12T12:12:00.000Z"));
    }

    @Test
    void shouldNotUpdateProducedAtDateGivenNoPermission() {
        // given: the user is allowed to create products
        allowUserToCreateProductsAndReadLinks("USER_ID");

        // and: a product
        var result = createSampleProduct(Agent.user(AgentId.of("USER_ID")));

        // when: the product is updated by another user; then: an exception is thrown
        var updateProducedAtRequest = new UpdateProducedAtDateRequest();
        updateProducedAtRequest.version = 0L;
        updateProducedAtRequest.producedAt = Instant.parse("2024-12-12T12:12:00.000Z");
        assertThatThrownBy(() -> updateProducedAt(
                result.id,
                updateProducedAtRequest,
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
        var updateProducedAtRequest = new UpdateProducedAtDateRequest();
        updateProducedAtRequest.version = result.version;
        updateProducedAtRequest.producedAt = Instant.parse("2024-12-12T12:12:00.000Z");
        updateProducedAt(result.id, updateProducedAtRequest, Agent.user(AgentId.of("USER_ID")));

        // when: the product is updated with an outdated version; then: an exception is thrown
        assertThatThrownBy(() -> updateProducedAt(
                result.id,
                updateProducedAtRequest,
                Agent.user(AgentId.of("USER_ID"))
        )).matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldRaiseErrorIfTryingToResetProducedAtDate() {
        // given: the user is allowed to create products
        allowUserToCreateProductsAndReadLinks("USER_ID");

        // and: a product
        var result = createSampleProduct(Agent.user(AgentId.of("USER_ID")));

        // and: the product is updated
        var updateProducedAtRequest = new UpdateProducedAtDateRequest();
        updateProducedAtRequest.version = result.version;
        updateProducedAtRequest.producedAt = Instant.parse("2024-12-12T12:12:00.000Z");
        updateProducedAt(result.id, updateProducedAtRequest, Agent.user(AgentId.of("USER_ID")));

        // when: the product is updated with a reset produced at date; then: an exception is thrown
        updateProducedAtRequest.version = 1L;
        updateProducedAtRequest.producedAt = null;
        assertThatThrownBy(() -> updateProducedAt(
                result.id,
                updateProducedAtRequest,
                Agent.user(AgentId.of("USER_ID"))
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRaiseMissingPermissionErrorIfProductDoesNotExist() {
        // when: a product that does not exist is updated; then: an exception is thrown
        var updateProducedAtRequest = new UpdateProducedAtDateRequest();
        updateProducedAtRequest.version = 0L;
        updateProducedAtRequest.producedAt = Instant.parse("2024-12-12T12:12:00.000Z");
        assertThatThrownBy(() -> updateProducedAt("PRODUCT_ID", updateProducedAtRequest, Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

}
