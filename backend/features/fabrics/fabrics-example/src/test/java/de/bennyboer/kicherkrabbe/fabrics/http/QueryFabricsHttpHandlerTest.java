package de.bennyboer.kicherkrabbe.fabrics.http;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.fabrics.*;
import de.bennyboer.kicherkrabbe.fabrics.http.api.requests.QueryFabricsRequest;
import de.bennyboer.kicherkrabbe.fabrics.http.api.responses.QueryFabricsResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class QueryFabricsHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyQueryFabrics() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: a request to query fabrics
        var request = new QueryFabricsRequest();
        request.searchTerm = "test";
        request.skip = 3;
        request.limit = 35;

        // and: the module is configured to return a successful response
        when(module.getFabrics(
                "test",
                3,
                35,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.just(FabricsPage.of(
                3,
                35,
                4,
                List.of(FabricDetails.of(
                        FabricId.of("FABRIC_ID"),
                        Version.of(0),
                        FabricName.of("Fabric name"),
                        ImageId.of("IMAGE_ID"),
                        Set.of(),
                        Set.of(),
                        Set.of(),
                        false,
                        false,
                        Instant.now()
                ))
        )));

        // when: posting the request
        var exchange = client.post()
                .uri("/fabrics/")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the fabrics page
        var response = exchange.expectBody(QueryFabricsResponse.class).returnResult().getResponseBody();
        assertThat(response).isNotNull();
        assertThat(response.skip).isEqualTo(3);
        assertThat(response.limit).isEqualTo(35);
        assertThat(response.total).isEqualTo(4);
        assertThat(response.fabrics).hasSize(1);

        var responseFabric = response.fabrics.get(0);
        assertThat(responseFabric.id).isEqualTo("FABRIC_ID");
        assertThat(responseFabric.name).isEqualTo("Fabric name");
        assertThat(responseFabric.imageId).isEqualTo("IMAGE_ID");
        assertThat(responseFabric.colorIds).isEmpty();
        assertThat(responseFabric.topicIds).isEmpty();
        assertThat(responseFabric.availability).isEmpty();
        assertThat(responseFabric.published).isFalse();
        assertThat(responseFabric.featured).isFalse();
        assertThat(responseFabric.createdAt).isNotNull();
    }

    @Test
    void shouldNotAllowUnauthorizedAccess() {
        // given: a request to query fabrics
        var request = new QueryFabricsRequest();
        request.searchTerm = "test";
        request.skip = 3;
        request.limit = 35;

        // when: posting the request without a token
        var exchange = client.post()
                .uri("/fabrics/")
                .bodyValue(request)
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldNotAllowAccessWithInvalidToken() {
        // given: a request to query fabrics
        var request = new QueryFabricsRequest();
        request.searchTerm = "test";
        request.skip = 3;
        request.limit = 35;

        // when: posting the request with an invalid token
        var exchange = client.post()
                .uri("/fabrics/FABRIC_ID/")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

}
