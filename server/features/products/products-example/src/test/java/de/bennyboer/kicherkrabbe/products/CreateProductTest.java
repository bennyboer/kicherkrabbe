package de.bennyboer.kicherkrabbe.products;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import de.bennyboer.kicherkrabbe.products.api.*;
import de.bennyboer.kicherkrabbe.products.api.requests.CreateProductRequest;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CreateProductTest extends ProductsModuleTest {

    @Test
    void shouldCreateProduct() {
        // given: a user is allowed to create products
        allowUserToCreateProducts("USER_ID");

        // and: we are at a fixed point in time
        setTime(Instant.parse("2024-11-08T12:45:00.000Z"));

        // and: a request to create a product
        var link1 = new LinkDTO();
        link1.type = LinkTypeDTO.PATTERN;
        link1.id = "PATTERN_ID_1";
        link1.name = "Pattern 1";

        var link2 = new LinkDTO();
        link2.type = LinkTypeDTO.FABRIC;
        link2.id = "FABRIC_ID_1";
        link2.name = "Fabric 1";

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

        var request = new CreateProductRequest();
        request.images = List.of("IMAGE_ID_1", "IMAGE_ID_2");
        request.links = List.of(link1, link2);
        request.fabricComposition = fabricComposition;
        request.notes = new NotesDTO();
        request.notes.contains = "Contains";
        request.notes.care = "Care";
        request.notes.safety = "Safety";
        request.producedAt = Instant.parse("2024-11-08T12:30:00.000Z");

        // when: the user creates the product
        var result = createProduct(request, Agent.user(AgentId.of("USER_ID")));

        // then: the product is created
        assertThat(result.id).isNotNull();
        assertThat(result.version).isEqualTo(0L);

        // and: the product is correct
        var product = getProduct(result.id, Agent.user(AgentId.of("USER_ID"))).product;
        assertThat(product.id).isEqualTo(result.id);
        assertThat(product.version).isEqualTo(0L);
        assertThat(product.number).isEqualTo("0000000001");
        assertThat(product.images).containsExactly("IMAGE_ID_1", "IMAGE_ID_2");
        assertThat(product.links).containsExactlyInAnyOrder(link1, link2);
        assertThat(product.fabricComposition)
                .usingRecursiveComparison()
                .ignoringCollectionOrderInFields("items")
                .isEqualTo(fabricComposition);
        assertThat(product.notes).isEqualTo(request.notes);
        assertThat(product.producedAt).isEqualTo(request.producedAt);
        assertThat(product.createdAt).isEqualTo(Instant.parse("2024-11-08T12:45:00.000Z"));

        // when: creating the product anew
        var result2 = createProduct(request, Agent.user(AgentId.of("USER_ID")));

        // then: the product is created
        assertThat(result2.id).isNotNull();
        assertThat(result2.id).isNotSameAs(result.id);
        assertThat(result2.version).isEqualTo(0L);

        // and: the number is incremented
        var product2 = getProduct(result2.id, Agent.user(AgentId.of("USER_ID"))).product;
        assertThat(product2.number).isEqualTo("0000000002");
    }

    @Test
    void shouldNotCreateProductWhenUserDoesNotHavePermission() {
        // given: a user is not allowed to create products

        // and: a request to create a product
        var link1 = new LinkDTO();
        link1.type = LinkTypeDTO.PATTERN;
        link1.id = "PATTERN_ID_1";
        link1.name = "Pattern 1";

        var link2 = new LinkDTO();
        link2.type = LinkTypeDTO.FABRIC;
        link2.id = "FABRIC_ID_1";
        link2.name = "Fabric 1";

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

        var request = new CreateProductRequest();
        request.images = List.of("IMAGE_ID_1", "IMAGE_ID_2");
        request.links = List.of(link1, link2);
        request.fabricComposition = fabricComposition;
        request.notes = new NotesDTO();
        request.notes.contains = "Contains";
        request.notes.care = "Care";
        request.notes.safety = "Safety";
        request.producedAt = Instant.parse("2024-11-08T12:30:00.000Z");

        // when: the user creates a product; then: an exception is thrown
        assertThatThrownBy(() -> createProduct(request, Agent.system()))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldNotAcceptInvalidRequests() {
        // given: a user is allowed to create products
        allowUserToCreateProducts("USER_ID");

        // and: a request to create a product
        var link1 = new LinkDTO();
        link1.type = LinkTypeDTO.PATTERN;
        link1.id = "PATTERN_ID_1";
        link1.name = "Pattern 1";

        var link2 = new LinkDTO();
        link2.type = LinkTypeDTO.FABRIC;
        link2.id = "FABRIC_ID_1";
        link2.name = "Fabric 1";

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

        var request = new CreateProductRequest();
        request.images = List.of("IMAGE_ID_1", "IMAGE_ID_2");
        request.links = List.of(link1, link2);
        request.fabricComposition = fabricComposition;
        request.notes = new NotesDTO();
        request.notes.contains = "Contains";
        request.notes.care = "Care";
        request.notes.safety = "Safety";
        request.producedAt = Instant.parse("2024-11-08T12:30:00.000Z");

        // when: the product ist to be created with invalid images; then: an exception is thrown
        request.images = null;
        assertThatThrownBy(() -> createProduct(request, Agent.user(AgentId.of("USER_ID"))))
                .isInstanceOf(IllegalArgumentException.class);
        request.images = List.of("IMAGE_ID_1", "IMAGE_ID_2");

        // when: the product ist to be created with invalid links; then: an exception is thrown
        request.links = null;
        assertThatThrownBy(() -> createProduct(request, Agent.user(AgentId.of("USER_ID"))))
                .isInstanceOf(IllegalArgumentException.class);
        request.links = List.of(link1, link2);

        // when: the product ist to be created with invalid fabric composition; then: an exception is thrown
        request.fabricComposition = null;
        assertThatThrownBy(() -> createProduct(request, Agent.user(AgentId.of("USER_ID"))))
                .isInstanceOf(IllegalArgumentException.class);

        // when: the product ist to be created with invalid fabric composition; then: an exception is thrown
        request.fabricComposition = new FabricCompositionDTO();
        assertThatThrownBy(() -> createProduct(request, Agent.user(AgentId.of("USER_ID"))))
                .isInstanceOf(IllegalArgumentException.class);

        // when: the product ist to be created with invalid fabric composition; then: an exception is thrown
        request.fabricComposition.items = null;
        assertThatThrownBy(() -> createProduct(request, Agent.user(AgentId.of("USER_ID"))))
                .isInstanceOf(IllegalArgumentException.class);

        // when: the product ist to be created with invalid fabric composition; then: an exception is thrown
        request.fabricComposition.items = new ArrayList<>();
        assertThatThrownBy(() -> createProduct(request, Agent.user(AgentId.of("USER_ID"))))
                .isInstanceOf(IllegalArgumentException.class);

        // when: the product ist to be created with invalid fabric composition; then: an exception is thrown
        var item = new FabricCompositionItemDTO();
        request.fabricComposition.items.add(item);
        assertThatThrownBy(() -> createProduct(request, Agent.user(AgentId.of("USER_ID"))))
                .isInstanceOf(IllegalArgumentException.class);

        // when: the product ist to be created with invalid fabric composition; then: an exception is thrown
        item.fabricType = null;
        item.percentage = 8000;
        assertThatThrownBy(() -> createProduct(request, Agent.user(AgentId.of("USER_ID"))))
                .isInstanceOf(IllegalArgumentException.class);

        // when: the product ist to be created with invalid fabric composition; then: an exception is thrown
        item.fabricType = FabricTypeDTO.COTTON;
        assertThatThrownBy(() -> createProduct(request, Agent.user(AgentId.of("USER_ID"))))
                .isInstanceOf(IllegalArgumentException.class);
        var secondItem = new FabricCompositionItemDTO();
        secondItem.fabricType = FabricTypeDTO.POLYESTER;
        secondItem.percentage = 2000;
        request.fabricComposition.items.add(secondItem);

        // when: the product ist to be created with invalid notes; then: an exception is thrown
        request.notes = null;
        assertThatThrownBy(() -> createProduct(request, Agent.user(AgentId.of("USER_ID"))))
                .isInstanceOf(IllegalArgumentException.class);

        // when: the product ist to be created with invalid notes; then: an exception is thrown
        request.notes = new NotesDTO();
        assertThatThrownBy(() -> createProduct(request, Agent.user(AgentId.of("USER_ID"))))
                .isInstanceOf(IllegalArgumentException.class);

        // when: the product ist to be created with invalid notes; then: an exception is thrown
        request.notes.contains = null;
        assertThatThrownBy(() -> createProduct(request, Agent.user(AgentId.of("USER_ID"))))
                .isInstanceOf(IllegalArgumentException.class);

        // when: the product ist to be created with invalid notes; then: an exception is thrown
        request.notes.contains = "";
        request.notes.care = null;
        assertThatThrownBy(() -> createProduct(request, Agent.user(AgentId.of("USER_ID"))))
                .isInstanceOf(IllegalArgumentException.class);

        // when: the product ist to be created with invalid notes; then: an exception is thrown
        request.notes.care = "";
        request.notes.safety = null;
        assertThatThrownBy(() -> createProduct(request, Agent.user(AgentId.of("USER_ID"))))
                .isInstanceOf(IllegalArgumentException.class);
        request.notes.safety = "";

        // when: the product ist to be created with invalid produced at date; then: an exception is thrown
        request.producedAt = null;
        assertThatThrownBy(() -> createProduct(request, Agent.user(AgentId.of("USER_ID"))))
                .isInstanceOf(IllegalArgumentException.class);
    }

}
