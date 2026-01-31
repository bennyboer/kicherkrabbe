package de.bennyboer.kicherkrabbe.fabrics.http;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.fabrics.*;
import de.bennyboer.kicherkrabbe.fabrics.http.api.responses.QueryFeaturedFabricsResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class QueryFeaturedFabricsHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyQueryFeaturedFabrics() {
        var token = createTokenForUser("USER_ID");

        when(module.getFeaturedFabrics(Agent.user(AgentId.of("USER_ID")))).thenReturn(Flux.just(
                PublishedFabric.of(
                        FabricId.of("FABRIC_ID"),
                        FabricName.of("Ice bear party"),
                        ImageId.of("IMAGE_ID"),
                        Set.of(ColorId.of("COLOR_ID")),
                        Set.of(TopicId.of("TOPIC_ID")),
                        Set.of(FabricTypeAvailability.of(
                                FabricTypeId.of("JERSEY_ID"),
                                true
                        ))
                )
        ));

        var exchange = client.get()
                .uri("/api/fabrics/featured")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        exchange.expectStatus().isOk();

        var response = exchange.expectBody(QueryFeaturedFabricsResponse.class).returnResult().getResponseBody();
        assertThat(response.fabrics).hasSize(1);

        var fabric = response.fabrics.get(0);
        assertThat(fabric.id).isEqualTo("FABRIC_ID");
        assertThat(fabric.name).isEqualTo("Ice bear party");
        assertThat(fabric.imageId).isEqualTo("IMAGE_ID");
        assertThat(fabric.colorIds).containsExactly("COLOR_ID");
        assertThat(fabric.topicIds).containsExactly("TOPIC_ID");
        assertThat(fabric.availability).hasSize(1);

        var availability = fabric.availability.stream().findFirst().get();
        assertThat(availability.typeId).isEqualTo("JERSEY_ID");
        assertThat(availability.inStock).isTrue();
    }

    @Test
    void shouldAllowUnauthorizedAccess() {
        when(module.getFeaturedFabrics(Agent.anonymous())).thenReturn(Flux.just(
                PublishedFabric.of(
                        FabricId.of("FABRIC_ID"),
                        FabricName.of("Ice bear party"),
                        ImageId.of("IMAGE_ID"),
                        Set.of(ColorId.of("COLOR_ID")),
                        Set.of(TopicId.of("TOPIC_ID")),
                        Set.of()
                )
        ));

        var exchange = client.get()
                .uri("/api/fabrics/featured")
                .exchange();

        exchange.expectStatus().isOk();

        var response = exchange.expectBody(QueryFeaturedFabricsResponse.class).returnResult().getResponseBody();
        assertThat(response.fabrics).hasSize(1);

        var fabric = response.fabrics.get(0);
        assertThat(fabric.id).isEqualTo("FABRIC_ID");
        assertThat(fabric.name).isEqualTo("Ice bear party");
        assertThat(fabric.imageId).isEqualTo("IMAGE_ID");
        assertThat(fabric.colorIds).containsExactly("COLOR_ID");
        assertThat(fabric.topicIds).containsExactly("TOPIC_ID");
        assertThat(fabric.availability).isEmpty();
    }

    @Test
    void shouldNotAllowAccessWithInvalidToken() {
        var exchange = client.get()
                .uri("/api/fabrics/featured")
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        exchange.expectStatus().isUnauthorized();
    }

}
