package de.bennyboer.kicherkrabbe.colors.http;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class AccessibleChangesHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyGetAccessibleColorChanges() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a successful response
        when(module.trackAccessibleColorsChanges(
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Flux.just("ADDED", "REMOVED"));

        // when: posting the request
        var exchange = client.get()
                .uri("/api/colors/accessible-changes")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the events
        var events = exchange.expectBodyList(String.class)
                .returnResult()
                .getResponseBody();
        assertThat(events).containsExactly("ADDED", "REMOVED");
    }

    @Test
    void shouldNotAllowUnauthorizedAccess() {
        // when: posting the request without a token
        var exchange = client.get()
                .uri("/api/colors/accessible-changes")
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldNotAllowAccessWithInvalidToken() {
        // when: posting the request with an invalid token
        var exchange = client.get()
                .uri("/api/colors/accessible-changes")
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

}
