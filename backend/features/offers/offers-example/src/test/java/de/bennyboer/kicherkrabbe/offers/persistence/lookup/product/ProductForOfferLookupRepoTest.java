package de.bennyboer.kicherkrabbe.offers.persistence.lookup.product;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.offers.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class ProductForOfferLookupRepoTest {

    private ProductForOfferLookupRepo repo;

    protected abstract ProductForOfferLookupRepo createRepo();

    @BeforeEach
    public void setUp() {
        repo = createRepo();
    }

    @Test
    void shouldSaveAndFindProductById() {
        var product = LookupProduct.of(
                ProductId.of("PRODUCT_1"),
                Version.zero(),
                ProductNumber.of("P-001"),
                List.of(ImageId.of("IMG_1")),
                Links.of(Set.of(Link.of(LinkType.PATTERN, LinkId.of("PATTERN_1"), LinkName.of("Pattern")))),
                FabricComposition.of(Set.of(FabricCompositionItem.of(FabricType.COTTON, LowPrecisionFloat.of(10000L))))
        );
        update(product);

        var found = findById(ProductId.of("PRODUCT_1"));
        assertThat(found).isEqualTo(product);
    }

    @Test
    void shouldRemoveProduct() {
        var product1 = LookupProduct.of(
                ProductId.of("PRODUCT_1"),
                Version.zero(),
                ProductNumber.of("P-001"),
                List.of(ImageId.of("IMG_1")),
                Links.of(Set.of()),
                FabricComposition.of(Set.of(FabricCompositionItem.of(FabricType.COTTON, LowPrecisionFloat.of(10000L))))
        );
        var product2 = LookupProduct.of(
                ProductId.of("PRODUCT_2"),
                Version.zero(),
                ProductNumber.of("P-002"),
                List.of(ImageId.of("IMG_2")),
                Links.of(Set.of()),
                FabricComposition.of(Set.of(FabricCompositionItem.of(FabricType.COTTON, LowPrecisionFloat.of(10000L))))
        );
        update(product1);
        update(product2);

        remove(ProductId.of("PRODUCT_1"));

        assertThat(findById(ProductId.of("PRODUCT_1"))).isNull();
        assertThat(findById(ProductId.of("PRODUCT_2"))).isEqualTo(product2);
    }

    @Test
    void shouldFindAllProducts() {
        var product1 = LookupProduct.of(
                ProductId.of("PRODUCT_1"),
                Version.zero(),
                ProductNumber.of("P-002"),
                List.of(ImageId.of("IMG_1")),
                Links.of(Set.of()),
                FabricComposition.of(Set.of(FabricCompositionItem.of(FabricType.COTTON, LowPrecisionFloat.of(10000L))))
        );
        var product2 = LookupProduct.of(
                ProductId.of("PRODUCT_2"),
                Version.zero(),
                ProductNumber.of("P-001"),
                List.of(ImageId.of("IMG_2")),
                Links.of(Set.of()),
                FabricComposition.of(Set.of(FabricCompositionItem.of(FabricType.COTTON, LowPrecisionFloat.of(10000L))))
        );
        update(product1);
        update(product2);

        var page = findAll("", 0, 10);
        assertThat(page.getTotal()).isEqualTo(2);
        assertThat(page.getResults()).containsExactly(product1, product2);
    }

    @Test
    void shouldFindAllProductsWithSearchTerm() {
        var product1 = LookupProduct.of(
                ProductId.of("PRODUCT_1"),
                Version.zero(),
                ProductNumber.of("P-001"),
                List.of(ImageId.of("IMG_1")),
                Links.of(Set.of()),
                FabricComposition.of(Set.of(FabricCompositionItem.of(FabricType.COTTON, LowPrecisionFloat.of(10000L))))
        );
        var product2 = LookupProduct.of(
                ProductId.of("PRODUCT_2"),
                Version.zero(),
                ProductNumber.of("P-002"),
                List.of(ImageId.of("IMG_2")),
                Links.of(Set.of()),
                FabricComposition.of(Set.of(FabricCompositionItem.of(FabricType.COTTON, LowPrecisionFloat.of(10000L))))
        );
        update(product1);
        update(product2);

        var page = findAll("P-001", 0, 10);
        assertThat(page.getTotal()).isEqualTo(1);
        assertThat(page.getResults()).containsExactly(product1);

        page = findAll("P-00", 0, 10);
        assertThat(page.getTotal()).isEqualTo(2);
    }

    @Test
    void shouldFindAllProductsWithPaging() {
        var product1 = LookupProduct.of(
                ProductId.of("PRODUCT_1"),
                Version.zero(),
                ProductNumber.of("P-003"),
                List.of(ImageId.of("IMG_1")),
                Links.of(Set.of()),
                FabricComposition.of(Set.of(FabricCompositionItem.of(FabricType.COTTON, LowPrecisionFloat.of(10000L))))
        );
        var product2 = LookupProduct.of(
                ProductId.of("PRODUCT_2"),
                Version.zero(),
                ProductNumber.of("P-002"),
                List.of(ImageId.of("IMG_2")),
                Links.of(Set.of()),
                FabricComposition.of(Set.of(FabricCompositionItem.of(FabricType.COTTON, LowPrecisionFloat.of(10000L))))
        );
        var product3 = LookupProduct.of(
                ProductId.of("PRODUCT_3"),
                Version.zero(),
                ProductNumber.of("P-001"),
                List.of(ImageId.of("IMG_3")),
                Links.of(Set.of()),
                FabricComposition.of(Set.of(FabricCompositionItem.of(FabricType.COTTON, LowPrecisionFloat.of(10000L))))
        );
        update(product1);
        update(product2);
        update(product3);

        var page = findAll("", 0, 2);
        assertThat(page.getTotal()).isEqualTo(3);
        assertThat(page.getResults()).hasSize(2);
        assertThat(page.getResults()).containsExactly(product1, product2);

        page = findAll("", 2, 2);
        assertThat(page.getTotal()).isEqualTo(3);
        assertThat(page.getResults()).hasSize(1);
        assertThat(page.getResults()).containsExactly(product3);
    }

    private void update(LookupProduct product) {
        repo.update(product).block();
    }

    private void remove(ProductId id) {
        repo.remove(id).block();
    }

    private LookupProduct findById(ProductId id) {
        return repo.findById(id).block();
    }

    private LookupProductPage findAll(String searchTerm, long skip, long limit) {
        return repo.findAll(searchTerm, skip, limit).block();
    }

}
