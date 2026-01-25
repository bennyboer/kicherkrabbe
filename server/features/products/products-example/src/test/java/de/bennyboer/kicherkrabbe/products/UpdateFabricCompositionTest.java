package de.bennyboer.kicherkrabbe.products;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import de.bennyboer.kicherkrabbe.products.api.FabricTypeDTO;
import de.bennyboer.kicherkrabbe.products.api.requests.UpdateFabricCompositionRequest;
import de.bennyboer.kicherkrabbe.products.samples.SampleFabricComposition;
import de.bennyboer.kicherkrabbe.products.samples.SampleFabricCompositionItem;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UpdateFabricCompositionTest extends ProductsModuleTest {

    @Test
    void shouldUpdateFabricComposition() {
        // given: the user is allowed to create products
        allowUserToCreateProductsAndReadLinks("USER_ID");

        // and: a product
        var result = createSampleProduct(Agent.user(AgentId.of("USER_ID")));

        // when: the products fabric composition are updated
        var fabricComposition = SampleFabricComposition.builder()
                .item(SampleFabricCompositionItem.builder()
                        .fabricType(FabricTypeDTO.COTTON)
                        .percentage(8000)
                        .build())
                .item(SampleFabricCompositionItem.builder()
                        .fabricType(FabricTypeDTO.POLYESTER)
                        .percentage(2000)
                        .build())
                .build()
                .toDTO();

        var updateFabricCompositionRequest = new UpdateFabricCompositionRequest();
        updateFabricCompositionRequest.version = 0L;
        updateFabricCompositionRequest.fabricComposition = fabricComposition;
        updateFabricComposition(result.id, updateFabricCompositionRequest, Agent.user(AgentId.of("USER_ID")));

        // then: the products fabric composition are updated
        var product = getProduct(result.id, Agent.user(AgentId.of("USER_ID"))).product;
        assertThat(product.fabricComposition.items).containsExactlyInAnyOrderElementsOf(fabricComposition.items);
    }

    @Test
    void shouldNotUpdateFabricCompositionGivenNoPermission() {
        // given: the user is allowed to create products
        allowUserToCreateProductsAndReadLinks("USER_ID");

        // and: a product
        var result = createSampleProduct(Agent.user(AgentId.of("USER_ID")));

        // when: the product is updated by another user; then: an exception is thrown
        var updateFabricCompositionRequest = new UpdateFabricCompositionRequest();
        updateFabricCompositionRequest.version = 0L;
        updateFabricCompositionRequest.fabricComposition = SampleFabricComposition.builder().build().toDTO();
        assertThatThrownBy(() -> updateFabricComposition(
                result.id,
                updateFabricCompositionRequest,
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
        var updateFabricCompositionRequest = new UpdateFabricCompositionRequest();
        updateFabricCompositionRequest.version = 0L;
        updateFabricCompositionRequest.fabricComposition = SampleFabricComposition.builder().build().toDTO();
        updateFabricComposition(result.id, updateFabricCompositionRequest, Agent.user(AgentId.of("USER_ID")));

        // when: the product is updated with an outdated version; then: an exception is thrown
        assertThatThrownBy(() -> updateFabricComposition(
                result.id,
                updateFabricCompositionRequest,
                Agent.user(AgentId.of("USER_ID"))
        )).matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldRaiseErrorIfTryingToResetFabricComposition() {
        // given: the user is allowed to create products
        allowUserToCreateProductsAndReadLinks("USER_ID");

        // and: a product
        var result = createSampleProduct(Agent.user(AgentId.of("USER_ID")));

        // and: the product is updated
        var updateFabricCompositionRequest = new UpdateFabricCompositionRequest();
        updateFabricCompositionRequest.version = 0L;
        updateFabricCompositionRequest.fabricComposition = SampleFabricComposition.builder().build().toDTO();
        updateFabricComposition(result.id, updateFabricCompositionRequest, Agent.user(AgentId.of("USER_ID")));

        // when: the product is updated with a reset produced at date; then: an exception is thrown
        updateFabricCompositionRequest.version = 1L;
        updateFabricCompositionRequest.fabricComposition = null;
        assertThatThrownBy(() -> updateFabricComposition(
                result.id,
                updateFabricCompositionRequest,
                Agent.user(AgentId.of("USER_ID"))
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRaiseMissingPermissionErrorIfProductDoesNotExist() {
        // when: a product that does not exist is updated; then: an exception is thrown
        var updateFabricCompositionRequest = new UpdateFabricCompositionRequest();
        updateFabricCompositionRequest.version = 0L;
        updateFabricCompositionRequest.fabricComposition = SampleFabricComposition.builder().build().toDTO();
        assertThatThrownBy(() -> updateFabricComposition("PRODUCT_ID", updateFabricCompositionRequest, Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

}
