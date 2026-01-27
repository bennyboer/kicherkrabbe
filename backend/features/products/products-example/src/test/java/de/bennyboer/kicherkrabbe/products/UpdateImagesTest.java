package de.bennyboer.kicherkrabbe.products;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import de.bennyboer.kicherkrabbe.products.api.requests.UpdateImagesRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UpdateImagesTest extends ProductsModuleTest {

    @Test
    void shouldUpdateImages() {
        // given: the user is allowed to create products
        allowUserToCreateProductsAndReadLinks("USER_ID");

        // and: a product
        var result = createSampleProduct(Agent.user(AgentId.of("USER_ID")));

        // when: the products images are updated
        var updateImagesRequest = new UpdateImagesRequest();
        updateImagesRequest.version = 0L;
        updateImagesRequest.images = List.of("SOME_IMAGE_ID_1", "SOME_IMAGE_ID_2", "SOME_IMAGE_ID_3");
        updateImages(result.id, updateImagesRequest, Agent.user(AgentId.of("USER_ID")));

        // then: the products images are updated
        var product = getProduct(result.id, Agent.user(AgentId.of("USER_ID"))).product;
        assertThat(product.images).containsExactly("SOME_IMAGE_ID_1", "SOME_IMAGE_ID_2", "SOME_IMAGE_ID_3");
    }

    @Test
    void shouldNotUpdateImagesGivenNoPermission() {
        // given: the user is allowed to create products
        allowUserToCreateProductsAndReadLinks("USER_ID");

        // and: a product
        var result = createSampleProduct(Agent.user(AgentId.of("USER_ID")));

        // when: the product is updated by another user; then: an exception is thrown
        var updateImagesRequest = new UpdateImagesRequest();
        updateImagesRequest.version = 0L;
        updateImagesRequest.images = List.of("SOME_IMAGE_ID_1", "SOME_IMAGE_ID_2", "SOME_IMAGE_ID_3");
        assertThatThrownBy(() -> updateImages(
                result.id,
                updateImagesRequest,
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
        var updateImagesRequest = new UpdateImagesRequest();
        updateImagesRequest.version = result.version;
        updateImagesRequest.images = List.of("SOME_IMAGE_ID_1", "SOME_IMAGE_ID_2", "SOME_IMAGE_ID_3");
        updateImages(result.id, updateImagesRequest, Agent.user(AgentId.of("USER_ID")));

        // when: the product is updated with an outdated version; then: an exception is thrown
        assertThatThrownBy(() -> updateImages(
                result.id,
                updateImagesRequest,
                Agent.user(AgentId.of("USER_ID"))
        )).matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldRaiseErrorIfTryingToResetImages() {
        // given: the user is allowed to create products
        allowUserToCreateProductsAndReadLinks("USER_ID");

        // and: a product
        var result = createSampleProduct(Agent.user(AgentId.of("USER_ID")));

        // and: the product is updated
        var updateImagesRequest = new UpdateImagesRequest();
        updateImagesRequest.version = result.version;
        updateImagesRequest.images = List.of("SOME_IMAGE_ID_1", "SOME_IMAGE_ID_2", "SOME_IMAGE_ID_3");
        updateImages(result.id, updateImagesRequest, Agent.user(AgentId.of("USER_ID")));

        // when: the product is updated with a reset produced at date; then: an exception is thrown
        updateImagesRequest.version = 1L;
        updateImagesRequest.images = null;
        assertThatThrownBy(() -> updateImages(
                result.id,
                updateImagesRequest,
                Agent.user(AgentId.of("USER_ID"))
        )).isInstanceOf(IllegalArgumentException.class);

        // when: the product is updated with an empty list of images
        updateImagesRequest.version = 1L;
        updateImagesRequest.images = List.of();
        updateImages(result.id, updateImagesRequest, Agent.user(AgentId.of("USER_ID")));

        // then: the product has no images
        var product = getProduct(result.id, Agent.user(AgentId.of("USER_ID"))).product;
        assertThat(product.images).isEmpty();
    }

    @Test
    void shouldRaiseMissingPermissionErrorIfProductDoesNotExist() {
        // when: a product that does not exist is updated; then: an exception is thrown
        var updateImagesRequest = new UpdateImagesRequest();
        updateImagesRequest.version = 0L;
        updateImagesRequest.images = List.of("SOME_IMAGE_ID_1", "SOME_IMAGE_ID_2", "SOME_IMAGE_ID_3");
        assertThatThrownBy(() -> updateImages("PRODUCT_ID", updateImagesRequest, Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

}
