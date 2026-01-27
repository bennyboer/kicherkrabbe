package de.bennyboer.kicherkrabbe.products;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.products.api.LinkTypeDTO;
import de.bennyboer.kicherkrabbe.products.api.requests.CreateProductRequest;
import de.bennyboer.kicherkrabbe.products.api.requests.RemoveLinkFromLookupRequest;
import de.bennyboer.kicherkrabbe.products.api.requests.UpdateLinkInLookupRequest;
import de.bennyboer.kicherkrabbe.products.samples.SampleFabricComposition;
import de.bennyboer.kicherkrabbe.products.samples.SampleLink;
import de.bennyboer.kicherkrabbe.products.samples.SampleNotes;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ManageLinksTest extends ProductsModuleTest {

    @Test
    void shouldAddLink() {
        // given: the system user is allowed to update links
        allowSystemUserToUpdateAndDeleteLinks();

        // and: the current user is allowed to read links
        allowUserToCreateProductsAndReadLinks("USER_ID");

        // and: a link is added
        var updateLinkInLookupRequest = new UpdateLinkInLookupRequest();
        updateLinkInLookupRequest.link = SampleLink.builder()
                .type(LinkTypeDTO.PATTERN)
                .id("PATTERN_ID")
                .name("Pattern")
                .build()
                .toDTO();
        updateLinkInLookup(updateLinkInLookupRequest, Agent.system());

        // then: the link is added
        var links = getLinks("", 0, 10, Agent.user(AgentId.of("USER_ID")));
        assertThat(links.links).hasSize(1);
        var link = links.links.get(0);
        assertThat(link.type).isEqualTo(LinkTypeDTO.PATTERN);
        assertThat(link.id).isEqualTo("PATTERN_ID");
        assertThat(link.name).isEqualTo("Pattern");
    }

    @Test
    void shouldRemoveLink() {
        // given: the system user is allowed to update links
        allowSystemUserToUpdateAndDeleteLinks();

        // and: the current user is allowed to read links
        allowUserToCreateProductsAndReadLinks("USER_ID");

        // and: some links
        var updateLinkInLookupRequest = new UpdateLinkInLookupRequest();
        updateLinkInLookupRequest.link = SampleLink.builder()
                .type(LinkTypeDTO.PATTERN)
                .id("PATTERN_ID")
                .name("Pattern")
                .build()
                .toDTO();
        updateLinkInLookup(updateLinkInLookupRequest, Agent.system());

        updateLinkInLookupRequest.link = SampleLink.builder()
                .type(LinkTypeDTO.FABRIC)
                .id("FABRIC_ID")
                .name("Fabric")
                .build()
                .toDTO();
        updateLinkInLookup(updateLinkInLookupRequest, Agent.system());

        // when: removing a link
        var removeLinkFromLookupRequest = new RemoveLinkFromLookupRequest();
        removeLinkFromLookupRequest.linkType = LinkTypeDTO.PATTERN;
        removeLinkFromLookupRequest.linkId = "PATTERN_ID";
        removeLinkFromLookup(removeLinkFromLookupRequest, Agent.system());

        // then: the link is removed
        var links = getLinks("", 0, 10, Agent.user(AgentId.of("USER_ID")));
        assertThat(links.links).hasSize(1);
        var link = links.links.get(0);
        assertThat(link.type).isEqualTo(LinkTypeDTO.FABRIC);
        assertThat(link.id).isEqualTo("FABRIC_ID");
        assertThat(link.name).isEqualTo("Fabric");
    }

    @Test
    void shouldCleanupLinkInProductsOnRemoval() {
        // given: the system user is allowed to update links
        allowSystemUserToUpdateAndDeleteLinks();

        // and: the current user is allowed to read links
        allowUserToCreateProductsAndReadLinks("USER_ID");

        // and: a link
        var updateLinkInLookupRequest = new UpdateLinkInLookupRequest();
        updateLinkInLookupRequest.link = SampleLink.builder()
                .type(LinkTypeDTO.PATTERN)
                .id("PATTERN_ID")
                .name("Pattern")
                .build()
                .toDTO();
        updateLinkInLookup(updateLinkInLookupRequest, Agent.system());

        // and: the links are added to a product
        var link = SampleLink.builder()
                .type(LinkTypeDTO.PATTERN)
                .id("PATTERN_ID")
                .name("Pattern")
                .build()
                .toDTO();

        var request = new CreateProductRequest();
        request.images = List.of();
        request.links = List.of(link);
        request.fabricComposition = SampleFabricComposition.builder().build().toDTO();
        request.notes = SampleNotes.builder().build().toDTO();
        request.producedAt = Instant.parse("2024-11-08T12:30:00.000Z");
        var id = createProduct(request, Agent.user(AgentId.of("USER_ID"))).id;

        // when: removing the link
        var removeLinkFromLookupRequest = new RemoveLinkFromLookupRequest();
        removeLinkFromLookupRequest.linkType = LinkTypeDTO.PATTERN;
        removeLinkFromLookupRequest.linkId = "PATTERN_ID";
        removeLinkFromLookup(removeLinkFromLookupRequest, Agent.system());

        // then: the link is removed from the product
        var product = getProduct(id, Agent.user(AgentId.of("USER_ID"))).product;
        assertThat(product.links).isEmpty();
    }

    @Test
    void shouldUpdateLinksInProducts() {
        // given: the system user is allowed to update links
        allowSystemUserToUpdateAndDeleteLinks();

        // and: the current user is allowed to read links
        allowUserToCreateProductsAndReadLinks("USER_ID");

        // and: a link
        var updateLinkInLookupRequest = new UpdateLinkInLookupRequest();
        updateLinkInLookupRequest.link = SampleLink.builder()
                .type(LinkTypeDTO.PATTERN)
                .id("PATTERN_ID")
                .name("Pattern")
                .build()
                .toDTO();
        updateLinkInLookup(updateLinkInLookupRequest, Agent.system());

        // and: the links are added to a product
        var link = SampleLink.builder()
                .type(LinkTypeDTO.PATTERN)
                .id("PATTERN_ID")
                .name("Pattern")
                .build()
                .toDTO();

        var request = new CreateProductRequest();
        request.images = List.of();
        request.links = List.of(link);
        request.fabricComposition = SampleFabricComposition.builder().build().toDTO();
        request.notes = SampleNotes.builder().build().toDTO();
        request.producedAt = Instant.parse("2024-11-08T12:30:00.000Z");
        var id = createProduct(request, Agent.user(AgentId.of("USER_ID"))).id;

        // when: updating the link
        updateLinkInLookupRequest.link.name = "New name";
        updateLinkInLookup(updateLinkInLookupRequest, Agent.system());

        // then: the link is updated in the product
        var product = getProduct(id, Agent.user(AgentId.of("USER_ID"))).product;
        assertThat(product.links).hasSize(1);
        assertThat(product.links.get(0).name).isEqualTo("New name");
    }

    @Test
    void shouldQueryLinks() {
        // given: the system user is allowed to update links
        allowSystemUserToUpdateAndDeleteLinks();

        // and: the current user is allowed to read links
        allowUserToCreateProductsAndReadLinks("USER_ID");

        // and: some links
        var updateLinkInLookupRequest = new UpdateLinkInLookupRequest();
        updateLinkInLookupRequest.link = SampleLink.builder()
                .type(LinkTypeDTO.PATTERN)
                .id("PATTERN_ID")
                .name("Pattern")
                .build()
                .toDTO();
        updateLinkInLookup(updateLinkInLookupRequest, Agent.system());

        updateLinkInLookupRequest.link = SampleLink.builder()
                .type(LinkTypeDTO.FABRIC)
                .id("FABRIC_ID")
                .name("Fabric")
                .build()
                .toDTO();
        updateLinkInLookup(updateLinkInLookupRequest, Agent.system());

        // when: querying all links
        var links = getLinks("", 0, 10, Agent.user(AgentId.of("USER_ID")));

        // then: all links are found ordered by name
        assertThat(links.links).hasSize(2);
        assertThat(links.links.get(0).id).isEqualTo("FABRIC_ID");
        assertThat(links.links.get(1).id).isEqualTo("PATTERN_ID");

        // when: querying links with a search term
        links = getLinks("Pattern", 0, 10, Agent.user(AgentId.of("USER_ID")));

        // then: only the first link is found
        assertThat(links.links).hasSize(1);
        assertThat(links.links.get(0).id).isEqualTo("PATTERN_ID");

        // when: querying links with paging
        links = getLinks("", 1, 1, Agent.user(AgentId.of("USER_ID")));

        // then: only the first link is found
        assertThat(links.links).hasSize(1);
        assertThat(links.links.get(0).id).isEqualTo("PATTERN_ID");
    }

}
