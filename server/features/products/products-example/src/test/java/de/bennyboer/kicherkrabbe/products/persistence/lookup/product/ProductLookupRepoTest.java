package de.bennyboer.kicherkrabbe.products.persistence.lookup.product;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.products.product.*;
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
                ProductNumber.of("0000000042"),
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
                ProductNumber.of("0000000042"),
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
                ProductNumber.of("0000000387"),
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

    private void update(LookupProduct product) {
        repo.update(product).block();
    }

    private void remove(ProductId productId) {
        repo.remove(productId).block();
    }

    private LookupProduct findById(ProductId id) {
        return repo.findById(id).block();
    }

}
