package de.bennyboer.kicherkrabbe.fabrics.http;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.fabrics.FabricAlias;
import de.bennyboer.kicherkrabbe.fabrics.FabricId;
import de.bennyboer.kicherkrabbe.fabrics.FabricName;
import de.bennyboer.kicherkrabbe.fabrics.ImageId;
import de.bennyboer.kicherkrabbe.fabrics.PublishedFabric;
import de.bennyboer.kicherkrabbe.fabrics.http.api.responses.QueryPublishedFabricResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class QueryPublishedFabricHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyQueryPublishedFabric() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a successful response
        when(module.getPublishedFabric(
                "FABRIC_ID",
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.just(PublishedFabric.of(
                FabricId.of("FABRIC_ID"),
                FabricName.of("Fabric name"),
                FabricAlias.of("fabric-name"),
                ImageId.of("IMAGE_ID"),
                List.of(),
                Set.of(),
                Set.of(),
                Set.of()
        )));

        // when: posting the request
        var exchange = client.get()
                .uri("/fabrics/FABRIC_ID/published")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the published fabric
        var response = exchange.expectBody(QueryPublishedFabricResponse.class).returnResult().getResponseBody();
        assertThat(response.fabric.id).isEqualTo("FABRIC_ID");
        assertThat(response.fabric.alias).isEqualTo("fabric-name");
        assertThat(response.fabric.name).isEqualTo("Fabric name");
        assertThat(response.fabric.imageId).isEqualTo("IMAGE_ID");
        assertThat(response.fabric.colorIds).isEmpty();
        assertThat(response.fabric.topicIds).isEmpty();
        assertThat(response.fabric.availability).isEmpty();
    }

    @Test
    void shouldAllowUnauthorizedAccess() {
        // given: the module is configured to return a successful response
        when(module.getPublishedFabric(
                "FABRIC_ID",
                Agent.anonymous()
        )).thenReturn(Mono.just(PublishedFabric.of(
                FabricId.of("FABRIC_ID"),
                FabricName.of("Fabric name"),
                FabricAlias.of("fabric-name"),
                ImageId.of("IMAGE_ID"),
                List.of(),
                Set.of(),
                Set.of(),
                Set.of()
        )));

        // when: posting the request without a token
        var exchange = client.get()
                .uri("/fabrics/FABRIC_ID/published")
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the published fabric
        var response = exchange.expectBody(QueryPublishedFabricResponse.class).returnResult().getResponseBody();
        assertThat(response.fabric.id).isEqualTo("FABRIC_ID");
        assertThat(response.fabric.alias).isEqualTo("fabric-name");
        assertThat(response.fabric.name).isEqualTo("Fabric name");
        assertThat(response.fabric.imageId).isEqualTo("IMAGE_ID");
        assertThat(response.fabric.colorIds).isEmpty();
        assertThat(response.fabric.topicIds).isEmpty();
        assertThat(response.fabric.availability).isEmpty();
    }

    @Test
    void shouldNotAllowAccessWithInvalidToken() {
        // when: posting the request with an invalid token
        var exchange = client.get()
                .uri("/fabrics/FABRIC_ID/published")
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldReturnWith404WhenPublishedFabricNotFound() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return an empty response
        when(module.getPublishedFabric(
                "FABRIC_ID",
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.empty());

        // when: posting the request
        var exchange = client.get()
                .uri("/fabrics/FABRIC_ID/published")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is not found
        exchange.expectStatus().isNotFound();
    }

}
