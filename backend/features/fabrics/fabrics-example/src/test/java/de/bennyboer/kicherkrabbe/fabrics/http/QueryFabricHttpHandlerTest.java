package de.bennyboer.kicherkrabbe.fabrics.http;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateNotFoundError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.fabrics.FabricDetails;
import de.bennyboer.kicherkrabbe.fabrics.FabricId;
import de.bennyboer.kicherkrabbe.fabrics.FabricName;
import de.bennyboer.kicherkrabbe.fabrics.ImageId;
import de.bennyboer.kicherkrabbe.fabrics.http.api.responses.QueryFabricResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class QueryFabricHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyQueryFabric() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a successful response
        when(module.getFabric(
                "FABRIC_ID",
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.just(FabricDetails.of(
                FabricId.of("FABRIC_ID"),
                Version.zero(),
                FabricName.of("Fabric name"),
                ImageId.of("IMAGE_ID"),
                Set.of(),
                Set.of(),
                Set.of(),
                false,
                false,
                Instant.now()
        )));

        // when: posting the request
        var exchange = client.get()
                .uri("/api/fabrics/FABRIC_ID/")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the fabric
        var response = exchange.expectBody(QueryFabricResponse.class).returnResult().getResponseBody();
        assertThat(response.fabric.id).isEqualTo("FABRIC_ID");
        assertThat(response.fabric.version).isEqualTo(0L);
        assertThat(response.fabric.name).isEqualTo("Fabric name");
        assertThat(response.fabric.imageId).isEqualTo("IMAGE_ID");
        assertThat(response.fabric.colorIds).isEmpty();
        assertThat(response.fabric.topicIds).isEmpty();
        assertThat(response.fabric.availability).isEmpty();
        assertThat(response.fabric.published).isFalse();
        assertThat(response.fabric.featured).isFalse();
        assertThat(response.fabric.createdAt).isNotNull();
    }

    @Test
    void shouldNotAllowUnauthorizedAccess() {
        // when: posting the request without a token
        var exchange = client.get()
                .uri("/api/fabrics/FABRIC_ID/")
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldNotAllowAccessWithInvalidToken() {
        // when: posting the request with an invalid token
        var exchange = client.get()
                .uri("/api/fabrics/FABRIC_ID/")
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldReturnWith404WhenFabricNotFound() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return n not found error
        when(module.getFabric(
                "FABRIC_ID",
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.error(new AggregateNotFoundError(AggregateType.of("FABRIC"), AggregateId.of("FABRIC_ID"))));

        // when: posting the request
        var exchange = client.get()
                .uri("/api/fabrics/FABRIC_ID/")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is not found
        exchange.expectStatus().isNotFound();
    }

}
