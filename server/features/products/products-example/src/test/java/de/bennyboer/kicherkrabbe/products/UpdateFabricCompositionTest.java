package de.bennyboer.kicherkrabbe.products;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import de.bennyboer.kicherkrabbe.products.api.FabricCompositionDTO;
import de.bennyboer.kicherkrabbe.products.api.FabricCompositionItemDTO;
import de.bennyboer.kicherkrabbe.products.api.FabricTypeDTO;
import de.bennyboer.kicherkrabbe.products.api.requests.UpdateFabricCompositionRequest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

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
        var fabricComposition = new FabricCompositionDTO();
        fabricComposition.items = new ArrayList<>();
        var fabricCompositionItem1 = new FabricCompositionItemDTO();
        fabricCompositionItem1.fabricType = FabricTypeDTO.COTTON;
        fabricCompositionItem1.percentage = 8000;
        fabricComposition.items.add(fabricCompositionItem1);
        var fabricCompositionItem2 = new FabricCompositionItemDTO();
        fabricCompositionItem2.fabricType = FabricTypeDTO.POLYESTER;
        fabricCompositionItem2.percentage = 2000;
        fabricComposition.items.add(fabricCompositionItem2);

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
        var fabricComposition = new FabricCompositionDTO();
        fabricComposition.items = new ArrayList<>();
        var fabricCompositionItem1 = new FabricCompositionItemDTO();
        fabricCompositionItem1.fabricType = FabricTypeDTO.COTTON;
        fabricCompositionItem1.percentage = 8000;
        fabricComposition.items.add(fabricCompositionItem1);
        var fabricCompositionItem2 = new FabricCompositionItemDTO();
        fabricCompositionItem2.fabricType = FabricTypeDTO.POLYESTER;
        fabricCompositionItem2.percentage = 2000;
        fabricComposition.items.add(fabricCompositionItem2);

        var updateFabricCompositionRequest = new UpdateFabricCompositionRequest();
        updateFabricCompositionRequest.version = 0L;
        updateFabricCompositionRequest.fabricComposition = fabricComposition;
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
        var fabricComposition = new FabricCompositionDTO();
        fabricComposition.items = new ArrayList<>();
        var fabricCompositionItem1 = new FabricCompositionItemDTO();
        fabricCompositionItem1.fabricType = FabricTypeDTO.COTTON;
        fabricCompositionItem1.percentage = 8000;
        fabricComposition.items.add(fabricCompositionItem1);
        var fabricCompositionItem2 = new FabricCompositionItemDTO();
        fabricCompositionItem2.fabricType = FabricTypeDTO.POLYESTER;
        fabricCompositionItem2.percentage = 2000;
        fabricComposition.items.add(fabricCompositionItem2);

        var updateFabricCompositionRequest = new UpdateFabricCompositionRequest();
        updateFabricCompositionRequest.version = 0L;
        updateFabricCompositionRequest.fabricComposition = fabricComposition;
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
        var fabricComposition = new FabricCompositionDTO();
        fabricComposition.items = new ArrayList<>();
        var fabricCompositionItem1 = new FabricCompositionItemDTO();
        fabricCompositionItem1.fabricType = FabricTypeDTO.COTTON;
        fabricCompositionItem1.percentage = 8000;
        fabricComposition.items.add(fabricCompositionItem1);
        var fabricCompositionItem2 = new FabricCompositionItemDTO();
        fabricCompositionItem2.fabricType = FabricTypeDTO.POLYESTER;
        fabricCompositionItem2.percentage = 2000;
        fabricComposition.items.add(fabricCompositionItem2);

        var updateFabricCompositionRequest = new UpdateFabricCompositionRequest();
        updateFabricCompositionRequest.version = 0L;
        updateFabricCompositionRequest.fabricComposition = fabricComposition;
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
        var fabricComposition = new FabricCompositionDTO();
        fabricComposition.items = new ArrayList<>();
        var fabricCompositionItem1 = new FabricCompositionItemDTO();
        fabricCompositionItem1.fabricType = FabricTypeDTO.COTTON;
        fabricCompositionItem1.percentage = 8000;
        fabricComposition.items.add(fabricCompositionItem1);
        var fabricCompositionItem2 = new FabricCompositionItemDTO();
        fabricCompositionItem2.fabricType = FabricTypeDTO.POLYESTER;
        fabricCompositionItem2.percentage = 2000;
        fabricComposition.items.add(fabricCompositionItem2);

        var updateFabricCompositionRequest = new UpdateFabricCompositionRequest();
        updateFabricCompositionRequest.version = 0L;
        updateFabricCompositionRequest.fabricComposition = fabricComposition;
        assertThatThrownBy(() -> updateFabricComposition("PRODUCT_ID", updateFabricCompositionRequest, Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

}
