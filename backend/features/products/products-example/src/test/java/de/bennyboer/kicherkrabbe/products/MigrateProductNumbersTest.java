package de.bennyboer.kicherkrabbe.products;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.products.samples.SampleProduct;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

public class MigrateProductNumbersTest extends ProductsModuleTest {

    @Test
    void shouldMigrateLegacyProductNumbersToYearlyFormat() {
        allowUserToCreateProductsAndReadLinks("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        setTime(Instant.parse("2024-03-15T10:00:00.000Z"));
        var p1 = createProduct(
                SampleProduct.builder().producedAt(Instant.parse("2024-03-15T10:00:00.000Z")).build(),
                agent
        );

        setTime(Instant.parse("2024-06-20T14:00:00.000Z"));
        var p2 = createProduct(
                SampleProduct.builder().producedAt(Instant.parse("2024-06-20T14:00:00.000Z")).build(),
                agent
        );

        setTime(Instant.parse("2025-01-10T08:00:00.000Z"));
        var p3 = createProduct(
                SampleProduct.builder().producedAt(Instant.parse("2025-01-10T08:00:00.000Z")).build(),
                agent
        );

        setLegacyProductNumber(p1.id, "00001");
        setLegacyProductNumber(p2.id, "00002");
        setLegacyProductNumber(p3.id, "00003");

        assertThat(getProduct(p1.id, agent).product.number).isEqualTo("00001");
        assertThat(getProduct(p2.id, agent).product.number).isEqualTo("00002");
        assertThat(getProduct(p3.id, agent).product.number).isEqualTo("00003");

        migrateProductNumbers();

        assertThat(getProduct(p1.id, agent).product.number).isEqualTo("2024-1");
        assertThat(getProduct(p2.id, agent).product.number).isEqualTo("2024-2");
        assertThat(getProduct(p3.id, agent).product.number).isEqualTo("2025-1");
    }

    @Test
    void shouldContinueNumberingAfterMigration() {
        allowUserToCreateProductsAndReadLinks("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        setTime(Instant.parse("2024-05-01T10:00:00.000Z"));
        var p1 = createProduct(
                SampleProduct.builder().producedAt(Instant.parse("2024-05-01T10:00:00.000Z")).build(),
                agent
        );
        var p2 = createProduct(
                SampleProduct.builder().producedAt(Instant.parse("2024-08-01T10:00:00.000Z")).build(),
                agent
        );

        setLegacyProductNumber(p1.id, "00001");
        setLegacyProductNumber(p2.id, "00002");

        migrateProductNumbers();

        assertThat(getProduct(p1.id, agent).product.number).isEqualTo("2024-1");
        assertThat(getProduct(p2.id, agent).product.number).isEqualTo("2024-2");

        setTime(Instant.parse("2024-12-01T10:00:00.000Z"));
        var p3 = createProduct(
                SampleProduct.builder().producedAt(Instant.parse("2024-12-01T10:00:00.000Z")).build(),
                agent
        );

        assertThat(getProduct(p3.id, agent).product.number).isEqualTo("2024-3");
    }

    @Test
    void shouldNotMigrateAlreadyYearlyNumbers() {
        allowUserToCreateProductsAndReadLinks("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        setTime(Instant.parse("2024-05-01T10:00:00.000Z"));
        var p1 = createProduct(
                SampleProduct.builder().producedAt(Instant.parse("2024-05-01T10:00:00.000Z")).build(),
                agent
        );

        assertThat(getProduct(p1.id, agent).product.number).isEqualTo("2024-1");

        migrateProductNumbers();

        assertThat(getProduct(p1.id, agent).product.number).isEqualTo("2024-1");
    }

}
