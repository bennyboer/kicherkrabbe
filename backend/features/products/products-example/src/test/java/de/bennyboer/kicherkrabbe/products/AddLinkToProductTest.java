package de.bennyboer.kicherkrabbe.products;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import de.bennyboer.kicherkrabbe.products.api.LinkTypeDTO;
import de.bennyboer.kicherkrabbe.products.api.requests.AddLinkToProductRequest;
import de.bennyboer.kicherkrabbe.products.api.requests.UpdateLinkInLookupRequest;
import de.bennyboer.kicherkrabbe.products.samples.SampleLink;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AddLinkToProductTest extends ProductsModuleTest {

    @Test
    void shouldAddLinkToProduct() {
        // given: the user is allowed to create products
        allowUserToCreateProductsAndReadLinks("USER_ID");

        // and: the system user is allowed to update and delete links
        allowSystemUserToUpdateAndDeleteLinks();

        // and: the link to add is available
        var updateLinkInLookupRequest = new UpdateLinkInLookupRequest();
        updateLinkInLookupRequest.link = SampleLink.builder()
                .type(LinkTypeDTO.PATTERN)
                .id("SOME_PATTERN_ID")
                .name("Some Pattern")
                .build()
                .toDTO();
        updateLinkInLookup(updateLinkInLookupRequest, Agent.system());

        // and: a product
        var result = createSampleProduct(Agent.user(AgentId.of("USER_ID")));

        // when: a link is added to the product
        var addLinkToProductRequest = new AddLinkToProductRequest();
        addLinkToProductRequest.version = 0L;
        addLinkToProductRequest.linkType = LinkTypeDTO.PATTERN;
        addLinkToProductRequest.linkId = "SOME_PATTERN_ID";
        addLinkToProduct(result.id, addLinkToProductRequest, Agent.user(AgentId.of("USER_ID")));

        // then: a link is added to the product
        var product = getProduct(result.id, Agent.user(AgentId.of("USER_ID"))).product;
        var link = product.links
                .stream()
                .filter(l -> l.id == addLinkToProductRequest.linkId)
                .findFirst()
                .orElse(null);
        assertThat(link).isNotNull();
    }

    @Test
    void shouldNotAddLinkToProductGivenNoPermission() {
        // given: the user is allowed to create products
        allowUserToCreateProductsAndReadLinks("USER_ID");

        // and: a product
        var result = createSampleProduct(Agent.user(AgentId.of("USER_ID")));

        // when: the product is updated by another user; then: an exception is thrown
        var addLinkToProductRequest = new AddLinkToProductRequest();
        addLinkToProductRequest.version = 0L;
        addLinkToProductRequest.linkType = LinkTypeDTO.PATTERN;
        addLinkToProductRequest.linkId = "SOME_PATTERN_ID";
        assertThatThrownBy(() -> addLinkToProduct(
                result.id,
                addLinkToProductRequest,
                Agent.user(AgentId.of("OTHER_USER_ID"))
        )).matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldRaiseErrorWhenTheVersionIsNotUpToDate() {
        // given: the user is allowed to create products
        allowUserToCreateProductsAndReadLinks("USER_ID");

        // and: the system user is allowed to update and delete links
        allowSystemUserToUpdateAndDeleteLinks();

        // and: the link to add is available
        var updateLinkInLookupRequest = new UpdateLinkInLookupRequest();
        updateLinkInLookupRequest.link = SampleLink.builder()
                .type(LinkTypeDTO.PATTERN)
                .id("SOME_PATTERN_ID")
                .name("Some Pattern")
                .build()
                .toDTO();
        updateLinkInLookup(updateLinkInLookupRequest, Agent.system());

        // and: a product
        var result = createSampleProduct(Agent.user(AgentId.of("USER_ID")));

        // and: the product is updated
        var addLinkToProductRequest = new AddLinkToProductRequest();
        addLinkToProductRequest.version = result.version;
        addLinkToProductRequest.linkType = LinkTypeDTO.PATTERN;
        addLinkToProductRequest.linkId = "SOME_PATTERN_ID";
        addLinkToProduct(result.id, addLinkToProductRequest, Agent.user(AgentId.of("USER_ID")));

        // when: the product is updated with an outdated version; then: an exception is thrown
        assertThatThrownBy(() -> addLinkToProduct(
                result.id,
                addLinkToProductRequest,
                Agent.user(AgentId.of("USER_ID"))
        )).matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldRaiseMissingPermissionErrorIfProductDoesNotExist() {
        // when: a product that does not exist is updated; then: an exception is thrown
        var addLinkToProductRequest = new AddLinkToProductRequest();
        addLinkToProductRequest.version = 0L;
        addLinkToProductRequest.linkType = LinkTypeDTO.PATTERN;
        addLinkToProductRequest.linkId = "SOME_PATTERN_ID";
        assertThatThrownBy(() -> addLinkToProduct("PRODUCT_ID", addLinkToProductRequest, Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

}
