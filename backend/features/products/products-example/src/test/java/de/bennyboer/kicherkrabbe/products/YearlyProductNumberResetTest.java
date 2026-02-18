package de.bennyboer.kicherkrabbe.products;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.products.samples.SampleProduct;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

public class YearlyProductNumberResetTest extends ProductsModuleTest {

    @Test
    void shouldResetProductNumberForNewYear() {
        allowUserToCreateProductsAndReadLinks("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        setTime(Instant.parse("2024-11-08T12:00:00.000Z"));
        var p1 = createProduct(
                SampleProduct.builder().producedAt(Instant.parse("2024-11-08T12:00:00.000Z")).build(),
                agent
        );
        var p2 = createProduct(
                SampleProduct.builder().producedAt(Instant.parse("2024-11-09T12:00:00.000Z")).build(),
                agent
        );

        assertThat(getProduct(p1.id, agent).product.number).isEqualTo("2024-1");
        assertThat(getProduct(p2.id, agent).product.number).isEqualTo("2024-2");

        setTime(Instant.parse("2025-01-15T10:00:00.000Z"));
        var p3 = createProduct(
                SampleProduct.builder().producedAt(Instant.parse("2025-01-15T10:00:00.000Z")).build(),
                agent
        );

        assertThat(getProduct(p3.id, agent).product.number).isEqualTo("2025-1");
    }

}
