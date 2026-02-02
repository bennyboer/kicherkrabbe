package de.bennyboer.kicherkrabbe.fabrics.http;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.fabrics.http.api.requests.RenameFabricRequest;
import de.bennyboer.kicherkrabbe.fabrics.http.api.responses.RenameFabricResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class RenameFabricHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyRenameFabric() {
        // given: a request to rename a fabric
        var request = new RenameFabricRequest();
        request.version = 0L;
        request.name = "New name";

        // and: having a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a successful response
        when(module.renameFabric(
                "FABRIC_ID",
                request.version,
                request.name,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.just(1L));

        // when: posting the request
        var exchange = client.post()
                .uri("/fabrics/FABRIC_ID/rename")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the new version of the fabric
        exchange.expectBody(RenameFabricResponse.class)
                .value(response -> assertThat(response.version).isEqualTo(1L));
    }

    @Test
    void shouldNotAllowUnauthorizedAccess() {
        // given: a request to rename a fabric
        var request = new RenameFabricRequest();
        request.version = 0L;
        request.name = "New name";

        // when: posting the request without a token
        var exchange = client.post()
                .uri("/fabrics/FABRIC_ID/rename")
                .bodyValue(request)
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldNotAllowAccessWithInvalidToken() {
        // given: a request to rename a fabric
        var request = new RenameFabricRequest();
        request.version = 0L;
        request.name = "New name";

        // when: posting the request with an invalid token
        var exchange = client.post()
                .uri("/fabrics/FABRIC_ID/rename")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWith409OnAggregateVersionOutdatedError() {
        // given: a request to rename a fabric
        var request = new RenameFabricRequest();
        request.version = 0L;
        request.name = "New name";

        // and: having a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a conflict response
        when(module.renameFabric(
                "FABRIC_ID",
                request.version,
                request.name,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.error(new AggregateVersionOutdatedError(
                AggregateType.of("TEST"),
                AggregateId.of("TEST_ID"),
                Version.zero()
        )));

        // when: posting the request
        var exchange = client.post()
                .uri("/fabrics/FABRIC_ID/rename")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is conflict
        exchange.expectStatus().isEqualTo(409);
    }

    @Test
    void shouldRespondWithBadRequestOnInvalidName() {
        // given: a request to rename a fabric
        var request = new RenameFabricRequest();
        request.version = 0L;
        request.name = "";

        // and: having a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return an illegal argument exception
        when(module.renameFabric(
                "FABRIC_ID",
                request.version,
                request.name,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.error(new IllegalArgumentException("Name must not be empty")));

        // when: posting the request
        var exchange = client.post()
                .uri("/fabrics/FABRIC_ID/rename")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is bad request
        exchange.expectStatus().isBadRequest();
    }

}
