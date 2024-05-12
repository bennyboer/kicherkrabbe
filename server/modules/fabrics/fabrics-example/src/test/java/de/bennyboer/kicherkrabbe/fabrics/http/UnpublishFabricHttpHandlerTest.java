package de.bennyboer.kicherkrabbe.fabrics.http;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.fabrics.http.api.responses.UnpublishFabricResponse;
import de.bennyboer.kicherkrabbe.fabrics.unpublish.AlreadyUnpublishedError;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class UnpublishFabricHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyUnpublishFabric() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a successful response
        when(module.unpublishFabric(
                "FABRIC_ID",
                3L,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.just(4L));

        // when: posting the request
        var exchange = client.post()
                .uri("/api/fabrics/FABRIC_ID/unpublish?version=3")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the new version of the fabric
        exchange.expectBody(UnpublishFabricResponse.class)
                .value(response -> assertThat(response.version).isEqualTo(4L));
    }

    @Test
    void shouldRespondWith409OnAggregateVersionOutdatedError() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a conflict response
        when(module.unpublishFabric(
                "FABRIC_ID",
                3L,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.error(new AggregateVersionOutdatedError(
                AggregateType.of("TEST"),
                AggregateId.of("TEST_ID"),
                Version.zero()
        )));

        // when: posting the request
        var exchange = client.post()
                .uri("/api/fabrics/FABRIC_ID/unpublish?version=3")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is conflict
        exchange.expectStatus().isEqualTo(409);
    }

    @Test
    void shouldNotAllowUnpublishingFabricWithoutVersionQueryParam() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // when: posting the request without a version
        var exchange = client.post()
                .uri("/api/fabrics/FABRIC_ID/unpublish")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is bad request
        exchange.expectStatus().isBadRequest();
    }

    @Test
    void shouldNotAllowUnauthorizedAccess() {
        // when: posting the request without a token
        var exchange = client.post()
                .uri("/api/fabrics/FABRIC_ID/unpublish")
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldNotAllowAccessWithInvalidToken() {
        // when: posting the request with an invalid token
        var exchange = client.post()
                .uri("/api/fabrics/FABRIC_ID/unpublish")
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldResponseWith412OnAlreadyUnpublishedError() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return an error
        when(module.unpublishFabric(
                "FABRIC_ID",
                3L,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.error(new AlreadyUnpublishedError()));

        // when: posting the request
        var exchange = client.post()
                .uri("/api/fabrics/FABRIC_ID/unpublish?version=3")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is precondition failed
        exchange.expectStatus().isEqualTo(412);
    }

}
