package de.bennyboer.kicherkrabbe.products;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.products.api.LinkTypeDTO;
import de.bennyboer.kicherkrabbe.products.api.requests.CreateProductRequest;
import de.bennyboer.kicherkrabbe.products.samples.SampleFabricComposition;
import de.bennyboer.kicherkrabbe.products.samples.SampleLink;
import de.bennyboer.kicherkrabbe.products.samples.SampleNotes;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class QueryProductsTest extends ProductsModuleTest {

    @Test
    void shouldQueryProducts() {
        // given: the current user is allowed to create products
        allowUserToCreateProductsAndReadLinks("USER_ID");

        // and: there are some products created at different times
        var link1 = SampleLink.builder()
                .type(LinkTypeDTO.PATTERN)
                .id("PATTERN_ID_1")
                .name("Pattern 1")
                .build()
                .toDTO();

        var link2 = SampleLink.builder()
                .type(LinkTypeDTO.FABRIC)
                .id("FABRIC_ID_1")
                .name("Fabric 1")
                .build()
                .toDTO();

        var fabricComposition = SampleFabricComposition.builder().build().toDTO();

        var request = new CreateProductRequest();
        request.images = List.of("IMAGE_ID_1", "IMAGE_ID_2");
        request.links = List.of(link1, link2);
        request.fabricComposition = fabricComposition;
        request.notes = SampleNotes.builder().build().toDTO();
        request.producedAt = Instant.parse("2024-11-09T17:15:00.000Z");
        setTime(Instant.parse("2024-11-09T17:15:00.000Z"));
        createProduct(request, Agent.user(AgentId.of("USER_ID")));

        request.images = List.of("IMAGE_ID_3", "IMAGE_ID_4");
        request.links = List.of();
        request.producedAt = Instant.parse("2024-11-10T12:30:00.000Z");
        setTime(Instant.parse("2024-11-10T12:30:00.000Z"));
        createProduct(request, Agent.user(AgentId.of("USER_ID")));

        request.images = List.of("IMAGE_ID_5");
        request.producedAt = Instant.parse("2024-11-12T09:00:00.000Z");
        setTime(Instant.parse("2024-11-12T09:00:00.000Z"));
        createProduct(request, Agent.user(AgentId.of("USER_ID")));

        // when: querying products without filter
        var result = getProducts(
                "",
                null,
                null,
                0,
                10,
                Agent.user(AgentId.of("USER_ID"))
        );

        // then: the result contains all products ordered by creation date
        assertThat(result.total).isEqualTo(3);
        var products = result.products;
        assertThat(products).hasSize(3);

        var product1 = products.get(0);
        assertThat(product1.number).isEqualTo("2024-3");
        assertThat(product1.images).containsExactly("IMAGE_ID_5");
        assertThat(product1.links).isEmpty();
        assertThat(product1.fabricComposition)
                .usingRecursiveComparison()
                .ignoringCollectionOrderInFields("items")
                .isEqualTo(fabricComposition);
        assertThat(product1.notes).isEqualTo(request.notes);
        assertThat(product1.producedAt).isEqualTo(Instant.parse("2024-11-12T09:00:00.000Z"));
        assertThat(product1.createdAt).isEqualTo(Instant.parse("2024-11-12T09:00:00.000Z"));

        var product2 = products.get(1);
        assertThat(product2.number).isEqualTo("2024-2");
        assertThat(product2.images).containsExactly("IMAGE_ID_3", "IMAGE_ID_4");
        assertThat(product2.links).isEmpty();
        assertThat(product2.fabricComposition)
                .usingRecursiveComparison()
                .ignoringCollectionOrderInFields("items")
                .isEqualTo(fabricComposition);
        assertThat(product2.notes).isEqualTo(request.notes);
        assertThat(product2.producedAt).isEqualTo(Instant.parse("2024-11-10T12:30:00.000Z"));
        assertThat(product2.createdAt).isEqualTo(Instant.parse("2024-11-10T12:30:00.000Z"));

        var product3 = products.get(2);
        assertThat(product3.number).isEqualTo("2024-1");
        assertThat(product3.images).containsExactly("IMAGE_ID_1", "IMAGE_ID_2");
        assertThat(product3.links).containsExactlyInAnyOrder(link1, link2);
        assertThat(product3.fabricComposition)
                .usingRecursiveComparison()
                .ignoringCollectionOrderInFields("items")
                .isEqualTo(fabricComposition);
        assertThat(product3.notes).isEqualTo(request.notes);
        assertThat(product3.producedAt).isEqualTo(Instant.parse("2024-11-09T17:15:00.000Z"));
        assertThat(product3.createdAt).isEqualTo(Instant.parse("2024-11-09T17:15:00.000Z"));

        // when: querying products by search term
        result = getProducts(
                "2024-2",
                null,
                null,
                0,
                10,
                Agent.user(AgentId.of("USER_ID"))
        );

        // then: the result contains only the product with the matching number
        assertThat(result.total).isEqualTo(1);
        products = result.products;
        assertThat(products).hasSize(1);
        assertThat(products.get(0).number).isEqualTo("2024-2");

        // when: querying products by date range
        result = getProducts(
                "",
                Instant.parse("2024-11-09T00:00:00.000Z"),
                Instant.parse("2024-11-11T00:00:00.000Z"),
                0,
                10,
                Agent.user(AgentId.of("USER_ID"))
        );

        // then: the result contains only the products created within the date range
        assertThat(result.total).isEqualTo(2);
        products = result.products;
        assertThat(products).hasSize(2);
        var productNumbers = products.stream().map(p -> p.number).toList();
        assertThat(productNumbers).containsExactly("2024-2", "2024-1");

        // when: querying products with pagination
        result = getProducts(
                "",
                null,
                null,
                1,
                1,
                Agent.user(AgentId.of("USER_ID"))
        );

        // then: the result contains only the second product
        assertThat(result.total).isEqualTo(3);
        products = result.products;
        assertThat(products).hasSize(1);
        assertThat(products.get(0).number).isEqualTo("2024-2");
    }

    @Test
    void shouldReturnEmptyListIfNoProductIsCreatedYet() {
        // when: querying products
        var result = getProducts("", null, null, 0, 10, Agent.user(AgentId.of("USER_ID")));

        // then: the result is an empty list
        assertThat(result.total).isEqualTo(0);
        assertThat(result.products).isEmpty();
    }

}
