package de.bennyboer.kicherkrabbe.patterns.http;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.when;

public class DeletePatternHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyDeletePattern() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a successful response
        when(module.deletePattern(
                "PATTERN_ID",
                3L,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.empty());

        // when: posting the request
        var exchange = client.delete()
                .uri("/patterns/PATTERN_ID?version=3")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();
    }

    @Test
    void shouldRequireVersionQueryParam() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // when: posting the request without version query param
        var exchange = client.delete()
                .uri("/patterns/PATTERN_ID")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is bad request
        exchange.expectStatus().isBadRequest();
    }

    @Test
    void shouldNotAllowUnauthorizedAccess() {
        // when: posting the request without a token
        var exchange = client.delete()
                .uri("/patterns/PATTERN_ID")
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldNotAllowAccessWithInvalidToken() {
        // when: posting the request with an invalid token
        var exchange = client.delete()
                .uri("/patterns/PATTERN_ID")
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldReturnConflictWhenVersionOutdated() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a conflict response
        when(module.deletePattern(
                "PATTERN_ID",
                3L,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.error(new AggregateVersionOutdatedError(
                AggregateType.of("TEST"),
                AggregateId.of("TEST_ID"),
                Version.zero()
        )));

        // when: posting the request
        var exchange = client.delete()
                .uri("/patterns/PATTERN_ID?version=3")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is conflict
        exchange.expectStatus().isEqualTo(409);
    }

}
