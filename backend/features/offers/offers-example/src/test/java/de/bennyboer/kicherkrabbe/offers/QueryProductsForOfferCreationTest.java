package de.bennyboer.kicherkrabbe.offers;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.offers.samples.SampleProductForLookup;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class QueryProductsForOfferCreationTest extends OffersModuleTest {

    @Test
    void shouldQueryProducts() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        setUpProduct(SampleProductForLookup.builder()
                .id("PRODUCT_1")
                .number(ProductNumber.of("P-001"))
                .build());
        setUpProduct(SampleProductForLookup.builder()
                .id("PRODUCT_2")
                .number(ProductNumber.of("P-002"))
                .build());

        var page = getProductsForOfferCreation("", 0, 10, agent);
        assertThat(page.getTotal()).isEqualTo(2);
        assertThat(page.getResults()).hasSize(2);
    }

    @Test
    void shouldReturnProductDetails() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        var patternLink = Link.of(LinkType.PATTERN, LinkId.of("PATTERN_ID"), LinkName.of("Test Pattern"));
        setUpProduct(SampleProductForLookup.builder()
                .number(ProductNumber.of("P-042"))
                .links(Set.of(patternLink))
                .fabricCompositionItems(Set.of(
                        FabricCompositionItem.of(FabricType.COTTON, LowPrecisionFloat.of(10000L))
                ))
                .build());

        var page = getProductsForOfferCreation("", 0, 10, agent);
        assertThat(page.getResults()).hasSize(1);

        var product = page.getResults().getFirst();
        assertThat(product.getNumber()).isEqualTo(ProductNumber.of("P-042"));
        assertThat(product.getLinks().getLinks()).containsExactly(patternLink);
        assertThat(product.getFabricComposition().getItems()).containsExactly(
                FabricCompositionItem.of(FabricType.COTTON, LowPrecisionFloat.of(10000L))
        );
    }

    @Test
    void shouldFilterBySearchTerm() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        setUpProduct(SampleProductForLookup.builder()
                .id("PRODUCT_1")
                .number(ProductNumber.of("P-001"))
                .build());
        setUpProduct(SampleProductForLookup.builder()
                .id("PRODUCT_2")
                .number(ProductNumber.of("P-002"))
                .build());

        var page = getProductsForOfferCreation("001", 0, 10, agent);
        assertThat(page.getTotal()).isEqualTo(1);
        assertThat(page.getResults()).hasSize(1);
        assertThat(page.getResults().getFirst().getNumber()).isEqualTo(ProductNumber.of("P-001"));
    }

    @Test
    void shouldPaginate() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        setUpProduct(SampleProductForLookup.builder().id("P1").number(ProductNumber.of("P-001")).build());
        setUpProduct(SampleProductForLookup.builder().id("P2").number(ProductNumber.of("P-002")).build());
        setUpProduct(SampleProductForLookup.builder().id("P3").number(ProductNumber.of("P-003")).build());

        var page = getProductsForOfferCreation("", 0, 2, agent);
        assertThat(page.getTotal()).isEqualTo(3);
        assertThat(page.getResults()).hasSize(2);

        var page2 = getProductsForOfferCreation("", 2, 2, agent);
        assertThat(page2.getTotal()).isEqualTo(3);
        assertThat(page2.getResults()).hasSize(1);
    }

    @Test
    void shouldNotAllowQueryingProductsWithoutPermission() {
        var agent = Agent.user(AgentId.of("USER_ID"));

        assertThatThrownBy(() -> getProductsForOfferCreation("", 0, 10, agent))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

}
