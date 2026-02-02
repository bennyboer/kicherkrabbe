package de.bennyboer.kicherkrabbe.fabrics.http;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.fabrics.FabricId;
import de.bennyboer.kicherkrabbe.fabrics.FabricName;
import de.bennyboer.kicherkrabbe.fabrics.http.api.responses.QueryFeaturedFabricsResponse;
import de.bennyboer.kicherkrabbe.fabrics.samples.SamplePublishedFabric;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class QueryFeaturedFabricsHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyQueryFeaturedFabrics() {
        var token = createTokenForUser("USER_ID");

        when(module.getFeaturedFabrics(Agent.user(AgentId.of("USER_ID")))).thenReturn(Flux.just(
                SamplePublishedFabric.builder().build().toModel()
        ));

        var exchange = client.get()
                .uri("/fabrics/featured")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        exchange.expectStatus().isOk();

        var response = exchange.expectBody(QueryFeaturedFabricsResponse.class).returnResult().getResponseBody();
        assertThat(response.fabrics).hasSize(1);
    }

    @Test
    void shouldAllowUnauthorizedAccess() {
        when(module.getFeaturedFabrics(Agent.anonymous())).thenReturn(Flux.just(
                SamplePublishedFabric.builder().build().toModel()
        ));

        var exchange = client.get()
                .uri("/fabrics/featured")
                .exchange();

        exchange.expectStatus().isOk();

        var response = exchange.expectBody(QueryFeaturedFabricsResponse.class).returnResult().getResponseBody();
        assertThat(response.fabrics).hasSize(1);
    }

    @Test
    void shouldNotAllowAccessWithInvalidToken() {
        var exchange = client.get()
                .uri("/fabrics/featured")
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldReturnDeterministicOrderWithSameSeed() {
        var fabric1 = SamplePublishedFabric.builder().id(FabricId.of("FABRIC_1")).name(FabricName.of("Fabric 1")).build().toModel();
        var fabric2 = SamplePublishedFabric.builder().id(FabricId.of("FABRIC_2")).name(FabricName.of("Fabric 2")).build().toModel();
        var fabric3 = SamplePublishedFabric.builder().id(FabricId.of("FABRIC_3")).name(FabricName.of("Fabric 3")).build().toModel();

        when(module.getFeaturedFabrics(Agent.anonymous())).thenReturn(Flux.just(fabric1, fabric2, fabric3));

        var exchange1 = client.get()
                .uri("/fabrics/featured?seed=12345")
                .exchange();
        exchange1.expectStatus().isOk();
        var response1 = exchange1.expectBody(QueryFeaturedFabricsResponse.class).returnResult().getResponseBody();
        var ids1 = response1.fabrics.stream().map(f -> f.id).toList();

        var exchange2 = client.get()
                .uri("/fabrics/featured?seed=12345")
                .exchange();
        exchange2.expectStatus().isOk();
        var response2 = exchange2.expectBody(QueryFeaturedFabricsResponse.class).returnResult().getResponseBody();
        var ids2 = response2.fabrics.stream().map(f -> f.id).toList();

        assertThat(ids1).isEqualTo(ids2);
    }

    @Test
    void shouldReturnDifferentOrderWithDifferentSeeds() {
        var fabric1 = SamplePublishedFabric.builder().id(FabricId.of("FABRIC_1")).name(FabricName.of("Fabric 1")).build().toModel();
        var fabric2 = SamplePublishedFabric.builder().id(FabricId.of("FABRIC_2")).name(FabricName.of("Fabric 2")).build().toModel();
        var fabric3 = SamplePublishedFabric.builder().id(FabricId.of("FABRIC_3")).name(FabricName.of("Fabric 3")).build().toModel();
        var fabric4 = SamplePublishedFabric.builder().id(FabricId.of("FABRIC_4")).name(FabricName.of("Fabric 4")).build().toModel();
        var fabric5 = SamplePublishedFabric.builder().id(FabricId.of("FABRIC_5")).name(FabricName.of("Fabric 5")).build().toModel();

        when(module.getFeaturedFabrics(Agent.anonymous())).thenReturn(Flux.just(fabric1, fabric2, fabric3, fabric4, fabric5));

        var exchange1 = client.get()
                .uri("/fabrics/featured?seed=11111")
                .exchange();
        exchange1.expectStatus().isOk();
        var response1 = exchange1.expectBody(QueryFeaturedFabricsResponse.class).returnResult().getResponseBody();
        var ids1 = response1.fabrics.stream().map(f -> f.id).toList();

        var exchange2 = client.get()
                .uri("/fabrics/featured?seed=99999")
                .exchange();
        exchange2.expectStatus().isOk();
        var response2 = exchange2.expectBody(QueryFeaturedFabricsResponse.class).returnResult().getResponseBody();
        var ids2 = response2.fabrics.stream().map(f -> f.id).toList();

        assertThat(ids1).isNotEqualTo(ids2);
    }

    @Test
    void shouldWorkWithoutSeed() {
        var fabric1 = SamplePublishedFabric.builder().id(FabricId.of("FABRIC_1")).name(FabricName.of("Fabric 1")).build().toModel();
        var fabric2 = SamplePublishedFabric.builder().id(FabricId.of("FABRIC_2")).name(FabricName.of("Fabric 2")).build().toModel();
        var fabric3 = SamplePublishedFabric.builder().id(FabricId.of("FABRIC_3")).name(FabricName.of("Fabric 3")).build().toModel();

        when(module.getFeaturedFabrics(Agent.anonymous())).thenReturn(Flux.just(fabric1, fabric2, fabric3));

        var exchange = client.get()
                .uri("/fabrics/featured")
                .exchange();
        exchange.expectStatus().isOk();
        var response = exchange.expectBody(QueryFeaturedFabricsResponse.class).returnResult().getResponseBody();

        assertThat(response.fabrics).hasSize(3);
        assertThat(response.fabrics.stream().map(f -> f.id).toList()).isEqualTo(List.of("FABRIC_1", "FABRIC_2", "FABRIC_3"));
    }

}
