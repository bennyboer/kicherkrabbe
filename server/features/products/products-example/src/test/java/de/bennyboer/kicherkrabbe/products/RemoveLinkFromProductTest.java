package de.bennyboer.kicherkrabbe.products;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import de.bennyboer.kicherkrabbe.products.api.LinkDTO;
import de.bennyboer.kicherkrabbe.products.api.LinkTypeDTO;
import de.bennyboer.kicherkrabbe.products.api.requests.AddLinkToProductRequest;
import de.bennyboer.kicherkrabbe.products.api.requests.UpdateLinkInLookupRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RemoveLinkFromProductTest extends ProductsModuleTest {

    @Test
    void shouldRemoveLinkFromProduct() {
        // given: the user is allowed to create products
        allowUserToCreateProductsAndReadLinks("USER_ID");

        // and: the system user is allowed to update and delete links
        allowSystemUserToUpdateAndDeleteLinks();

        // and: the link to add is available
        var updateLinkInLookupRequest = new UpdateLinkInLookupRequest();
        updateLinkInLookupRequest.link = new LinkDTO();
        updateLinkInLookupRequest.link.id = "SOME_PATTERN_ID";
        updateLinkInLookupRequest.link.type = LinkTypeDTO.PATTERN;
        updateLinkInLookupRequest.link.name = "Some Pattern";
        updateLinkInLookup(updateLinkInLookupRequest, Agent.system());

        // and: a product
        var result = createSampleProduct(Agent.user(AgentId.of("USER_ID")));

        // and: a link is added to the product
        var addLinkToProductRequest = new AddLinkToProductRequest();
        addLinkToProductRequest.version = 0L;
        addLinkToProductRequest.linkType = LinkTypeDTO.PATTERN;
        addLinkToProductRequest.linkId = "SOME_PATTERN_ID";
        var version = addLinkToProduct(result.id, addLinkToProductRequest, Agent.user(AgentId.of("USER_ID"))).version;

        // when: a link is to be removed from the product
        removeLinkFromProduct(
                result.id,
                version,
                LinkTypeDTO.PATTERN,
                "SOME_PATTERN_ID",
                Agent.user(AgentId.of("USER_ID"))
        );

        // then: the link is removed from the product
        var product = getProduct(result.id, Agent.user(AgentId.of("USER_ID"))).product;
        var link = product.links
                .stream()
                .filter(l -> l.id == addLinkToProductRequest.linkId)
                .findFirst()
                .orElse(null);
        assertThat(link).isNull();
    }

    @Test
    void shouldNotRemoveLinkFromProductGivenNoPermission() {
        // given: the user is allowed to create products
        allowUserToCreateProductsAndReadLinks("USER_ID");

        // and: a product
        var result = createSampleProduct(Agent.user(AgentId.of("USER_ID")));

        // when: the product is updated by another user; then: an exception is thrown
        assertThatThrownBy(() -> removeLinkFromProduct(
                result.id,
                0L,
                LinkTypeDTO.PATTERN,
                "SOME_PATTERN_ID",
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
        updateLinkInLookupRequest.link = new LinkDTO();
        updateLinkInLookupRequest.link.id = "SOME_PATTERN_ID";
        updateLinkInLookupRequest.link.type = LinkTypeDTO.PATTERN;
        updateLinkInLookupRequest.link.name = "Some Pattern";
        updateLinkInLookup(updateLinkInLookupRequest, Agent.system());

        // and: a product
        var result = createSampleProduct(Agent.user(AgentId.of("USER_ID")));

        // and: a link is added to the product
        var addLinkToProductRequest = new AddLinkToProductRequest();
        addLinkToProductRequest.version = 0L;
        addLinkToProductRequest.linkType = LinkTypeDTO.PATTERN;
        addLinkToProductRequest.linkId = "SOME_PATTERN_ID";
        addLinkToProduct(result.id, addLinkToProductRequest, Agent.user(AgentId.of("USER_ID")));

        // when: the product is updated with an outdated version; then: an exception is thrown
        assertThatThrownBy(() -> removeLinkFromProduct(
                result.id,
                0L,
                LinkTypeDTO.PATTERN,
                "SOME_PATTERN_ID",
                Agent.user(AgentId.of("USER_ID"))
        )).matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldRaiseMissingPermissionErrorIfProductDoesNotExist() {
        // when: a product that does not exist is updated; then: an exception is thrown
        assertThatThrownBy(() -> removeLinkFromProduct(
                "PRODUCT_ID",
                0L,
                LinkTypeDTO.PATTERN,
                "SOME_PATTERN_ID",
                Agent.user(AgentId.of("USER_ID"))
        )).matches(e -> e.getCause() instanceof MissingPermissionError);
    }

}
