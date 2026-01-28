package de.bennyboer.kicherkrabbe.fabrics.http;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.fabrics.http.api.requests.UpdateFabricImageRequest;
import de.bennyboer.kicherkrabbe.fabrics.http.api.responses.UpdateFabricImageResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class UpdateFabricImageHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyUpdateFabricImage() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: a request to update the image of a fabric
        var request = new UpdateFabricImageRequest();
        request.version = 3L;
        request.imageId = "NEW_IMAGE_ID";

        // and: the module is configured to return a successful response
        when(module.updateFabricImage(
                "FABRIC_ID",
                3L,
                "NEW_IMAGE_ID",
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.just(4L));

        // when: posting the request
        var exchange = client.post()
                .uri("/api/fabrics/FABRIC_ID/update/image")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the new version of the fabric
        exchange.expectBody(UpdateFabricImageResponse.class)
                .value(response -> assertThat(response.version).isEqualTo(4L));
    }

    @Test
    void shouldRespondWith409OnAggregateVersionOutdatedError() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: a request to update the image of a fabric
        var request = new UpdateFabricImageRequest();
        request.version = 3L;
        request.imageId = "NEW_IMAGE_ID";

        // and: the module is configured to return a conflict response
        when(module.updateFabricImage(
                "FABRIC_ID",
                3L,
                "NEW_IMAGE_ID",
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.error(new AggregateVersionOutdatedError(
                AggregateType.of("TEST"),
                AggregateId.of("TEST_ID"),
                Version.zero()
        )));

        // when: posting the request
        var exchange = client.post()
                .uri("/api/fabrics/FABRIC_ID/update/image")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is conflict
        exchange.expectStatus().isEqualTo(409);
    }

    @Test
    void shouldNotAllowUnauthorizedAccess() {
        // when: posting the request without a token
        var exchange = client.post()
                .uri("/api/fabrics/FABRIC_ID/update/image")
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldNotAllowAccessWithInvalidToken() {
        // when: posting the request with an invalid token
        var exchange = client.post()
                .uri("/api/fabrics/FABRIC_ID/update/image")
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithBadRequestOnInvalidRequest() {
        // given: a request to update the image of a fabric
        var request = new UpdateFabricImageRequest();
        request.version = 0L;
        request.imageId = "NEW_IMAGE_ID";

        // and: having a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return an illegal argument exception
        when(module.updateFabricImage(
                "FABRIC_ID",
                0L,
                "NEW_IMAGE_ID",
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.error(new IllegalArgumentException("Invalid request")));

        // when: posting the request without a token
        var exchange = client.post()
                .uri("/api/fabrics/FABRIC_ID/update/image")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is bad request
        exchange.expectStatus().isBadRequest();
    }

}
