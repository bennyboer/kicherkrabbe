package de.bennyboer.kicherkrabbe.products.persistence.lookup.product;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.products.product.*;
import jakarta.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class ProductLookupRepoTest {

    private ProductLookupRepo repo;

    protected abstract ProductLookupRepo createRepo();

    @BeforeEach
    public void setUp() {
        repo = createRepo();
    }

    @Test
    void shouldUpdateProduct() {
        // given: a product to update
        var product = LookupProduct.of(
                ProductId.create(),
                Version.zero(),
                ProductNumber.of("00042"),
                List.of(ImageId.of("IMAGE_ID_1"), ImageId.of("IMAGE_ID_2")),
                Links.of(Set.of(
                        Link.of(LinkType.PATTERN, LinkId.of("PATTERN_ID"), LinkName.of("Pattern")),
                        Link.of(LinkType.FABRIC, LinkId.of("FABRIC_ID"), LinkName.of("Fabric"))
                )),
                FabricComposition.of(Set.of(
                        FabricCompositionItem.of(FabricType.COTTON, LowPrecisionFloat.of(8000)),
                        FabricCompositionItem.of(FabricType.ELASTANE, LowPrecisionFloat.of(2000))
                )),
                Notes.of(
                        Note.of("Contains"),
                        Note.of("Care"),
                        Note.of("Safety")
                ),
                Instant.parse("2023-10-12T19:15:00.000Z"),
                Instant.parse("2023-10-12T19:30:00.000Z")
        );

        // when: updating the product
        update(product);

        // then: the product is updated
        var actualProduct = findById(product.getId());
        assertThat(actualProduct).isEqualTo(product);
    }

    @Test
    void shouldRemoveProduct() {
        // given: some products
        var product1 = LookupProduct.of(
                ProductId.create(),
                Version.zero(),
                ProductNumber.of("00042"),
                List.of(ImageId.of("IMAGE_ID_1"), ImageId.of("IMAGE_ID_2")),
                Links.of(Set.of(
                        Link.of(LinkType.PATTERN, LinkId.of("PATTERN_ID"), LinkName.of("Pattern")),
                        Link.of(LinkType.FABRIC, LinkId.of("FABRIC_ID"), LinkName.of("Fabric"))
                )),
                FabricComposition.of(Set.of(
                        FabricCompositionItem.of(FabricType.COTTON, LowPrecisionFloat.of(8000)),
                        FabricCompositionItem.of(FabricType.ELASTANE, LowPrecisionFloat.of(2000))
                )),
                Notes.of(
                        Note.of("Contains"),
                        Note.of("Care"),
                        Note.of("Safety")
                ),
                Instant.parse("2023-10-12T19:15:00.000Z"),
                Instant.parse("2023-10-12T19:30:00.000Z")
        );
        var product2 = LookupProduct.of(
                ProductId.create(),
                Version.zero(),
                ProductNumber.of("00387"),
                List.of(ImageId.of("IMAGE_ID_3")),
                Links.of(Set.of(
                        Link.of(LinkType.PATTERN, LinkId.of("PATTERN_ID"), LinkName.of("Pattern"))
                )),
                FabricComposition.of(Set.of(
                        FabricCompositionItem.of(FabricType.POLYESTER, LowPrecisionFloat.of(6000)),
                        FabricCompositionItem.of(FabricType.ELASTANE, LowPrecisionFloat.of(4000))
                )),
                Notes.of(
                        Note.of(""),
                        Note.of(""),
                        Note.of("")
                ),
                Instant.parse("2023-10-12T20:30:00.000Z"),
                Instant.parse("2023-10-12T21:10:00.000Z")
        );
        update(product1);
        update(product2);

        // when: removing a product
        remove(product1.getId());

        // then: the product is removed
        assertThat(findById(product1.getId())).isNull();
        assertThat(findById(product2.getId())).isEqualTo(product2);
    }

    @Test
    void shouldQueryProducts() {
        // given: some products
        var product1 = LookupProduct.of(
                ProductId.create(),
                Version.zero(),
                ProductNumber.of("00983"),
                List.of(ImageId.of("IMAGE_ID_1"), ImageId.of("IMAGE_ID_2")),
                Links.of(Set.of(
                        Link.of(LinkType.PATTERN, LinkId.of("PATTERN_ID"), LinkName.of("Pattern")),
                        Link.of(LinkType.FABRIC, LinkId.of("FABRIC_ID"), LinkName.of("Fabric"))
                )),
                FabricComposition.of(Set.of(
                        FabricCompositionItem.of(FabricType.COTTON, LowPrecisionFloat.of(8000)),
                        FabricCompositionItem.of(FabricType.ELASTANE, LowPrecisionFloat.of(2000))
                )),
                Notes.of(
                        Note.of("Contains"),
                        Note.of("Care"),
                        Note.of("Safety")
                ),
                Instant.parse("2023-10-12T19:15:00.000Z"),
                Instant.parse("2023-10-12T19:30:00.000Z")
        );
        var product2 = LookupProduct.of(
                ProductId.create(),
                Version.zero(),
                ProductNumber.of("00387"),
                List.of(ImageId.of("IMAGE_ID_3")),
                Links.of(Set.of(
                        Link.of(LinkType.PATTERN, LinkId.of("PATTERN_ID"), LinkName.of("Pattern"))
                )),
                FabricComposition.of(Set.of(
                        FabricCompositionItem.of(FabricType.POLYESTER, LowPrecisionFloat.of(6000)),
                        FabricCompositionItem.of(FabricType.ELASTANE, LowPrecisionFloat.of(4000))
                )),
                Notes.of(
                        Note.of(""),
                        Note.of(""),
                        Note.of("")
                ),
                Instant.parse("2023-10-12T20:30:00.000Z"),
                Instant.parse("2023-10-12T21:10:00.000Z")
        );
        update(product1);
        update(product2);

        // when: querying all products
        var page = findByIds(
                Set.of(product1.getId(), product2.getId()),
                "",
                null,
                null,
                0,
                10
        );

        // then: all products are found ordered reversed by creation date
        assertThat(page.getTotal()).isEqualTo(2);
        var productIds = page.getProducts()
                .stream()
                .map(LookupProduct::getId)
                .toList();
        assertThat(productIds).containsExactly(product2.getId(), product1.getId());

        // when: querying products with paging
        page = findByIds(
                Set.of(product1.getId(), product2.getId()),
                "",
                null,
                null,
                1,
                1
        );

        // then: only the first product is found
        assertThat(page.getTotal()).isEqualTo(2);
        productIds = page.getProducts()
                .stream()
                .map(LookupProduct::getId)
                .toList();
        assertThat(productIds).containsExactly(product1.getId());

        // when: querying products with a date range filter
        page = findByIds(
                Set.of(product1.getId(), product2.getId()),
                "",
                Instant.parse("2023-10-12T19:15:00.000Z"),
                Instant.parse("2023-10-12T19:45:00.000Z"),
                0,
                10
        );

        // then: only the first product is found
        assertThat(page.getTotal()).isEqualTo(1);
        productIds = page.getProducts()
                .stream()
                .map(LookupProduct::getId)
                .toList();
        assertThat(productIds).containsExactly(product1.getId());

        // when: querying products with another date range filter
        page = findByIds(
                Set.of(product1.getId(), product2.getId()),
                "",
                Instant.parse("2023-10-12T20:00:00.000Z"),
                null,
                0,
                10
        );

        // then: only the second product is found
        assertThat(page.getTotal()).isEqualTo(1);
        productIds = page.getProducts()
                .stream()
                .map(LookupProduct::getId)
                .toList();
        assertThat(productIds).containsExactly(product2.getId());

        // when: querying products with another date range filter
        page = findByIds(
                Set.of(product1.getId(), product2.getId()),
                "",
                null,
                Instant.parse("2023-10-12T20:00:00.000Z"),
                0,
                10
        );

        // then: only the first product is found
        assertThat(page.getTotal()).isEqualTo(1);
        productIds = page.getProducts()
                .stream()
                .map(LookupProduct::getId)
                .toList();
        assertThat(productIds).containsExactly(product1.getId());

        // when: querying products by search term
        page = findByIds(
                Set.of(product1.getId(), product2.getId()),
                "00983",
                null,
                null,
                0,
                10
        );

        // then: only the first product is found
        assertThat(page.getTotal()).isEqualTo(1);
        productIds = page.getProducts()
                .stream()
                .map(LookupProduct::getId)
                .toList();
        assertThat(productIds).containsExactly(product1.getId());
    }

    @Test
    void shouldFindProductsByLink() {
        // given: some products
        var product1 = LookupProduct.of(
                ProductId.create(),
                Version.zero(),
                ProductNumber.of("00983"),
                List.of(ImageId.of("IMAGE_ID_1"), ImageId.of("IMAGE_ID_2")),
                Links.of(Set.of(
                        Link.of(LinkType.PATTERN, LinkId.of("PATTERN_ID"), LinkName.of("Pattern")),
                        Link.of(LinkType.FABRIC, LinkId.of("FABRIC_ID"), LinkName.of("Fabric"))
                )),
                FabricComposition.of(Set.of(
                        FabricCompositionItem.of(FabricType.COTTON, LowPrecisionFloat.of(8000)),
                        FabricCompositionItem.of(FabricType.ELASTANE, LowPrecisionFloat.of(2000))
                )),
                Notes.of(
                        Note.of("Contains"),
                        Note.of("Care"),
                        Note.of("Safety")
                ),
                Instant.parse("2023-10-12T19:15:00.000Z"),
                Instant.parse("2023-10-12T19:30:00.000Z")
        );
        var product2 = LookupProduct.of(
                ProductId.create(),
                Version.zero(),
                ProductNumber.of("00387"),
                List.of(ImageId.of("IMAGE_ID_3")),
                Links.of(Set.of(
                        Link.of(LinkType.PATTERN, LinkId.of("PATTERN_ID"), LinkName.of("Pattern"))
                )),
                FabricComposition.of(Set.of(
                        FabricCompositionItem.of(FabricType.POLYESTER, LowPrecisionFloat.of(6000)),
                        FabricCompositionItem.of(FabricType.ELASTANE, LowPrecisionFloat.of(4000))
                )),
                Notes.of(
                        Note.of(""),
                        Note.of(""),
                        Note.of("")
                ),
                Instant.parse("2023-10-12T20:30:00.000Z"),
                Instant.parse("2023-10-12T21:10:00.000Z")
        );
        update(product1);
        update(product2);

        // when: querying products by link
        var products = findByLink(LinkType.PATTERN, LinkId.of("PATTERN_ID"));

        // then: all products are found
        assertThat(products).hasSize(2);
        assertThat(products).containsExactlyInAnyOrder(product1, product2);

        // when: querying products by another link
        products = findByLink(LinkType.FABRIC, LinkId.of("FABRIC_ID"));

        // then: only the first product is found
        assertThat(products).hasSize(1);
        assertThat(products).containsExactly(product1);

        // when: querying products by another link
        products = findByLink(LinkType.FABRIC, LinkId.of("FABRIC_ID_2"));

        // then: no product is found
        assertThat(products).isEmpty();
    }

    private void update(LookupProduct product) {
        repo.update(product).block();
    }

    private void remove(ProductId productId) {
        repo.remove(productId).block();
    }

    private LookupProduct findById(ProductId id) {
        return repo.findById(id).block();
    }

    private List<LookupProduct> findByLink(LinkType linkType, LinkId linkId) {
        return repo.findByLink(linkType, linkId).collectList().block();
    }

    private LookupProductPage findByIds(
            Set<ProductId> productIds,
            String searchTerm,
            @Nullable Instant from,
            @Nullable Instant to,
            long skip,
            long limit
    ) {
        return repo.findByIds(productIds, searchTerm, from, to, skip, limit).block();
    }

}
