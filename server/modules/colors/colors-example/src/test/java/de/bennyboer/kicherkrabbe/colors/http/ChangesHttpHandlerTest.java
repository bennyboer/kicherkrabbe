package de.bennyboer.kicherkrabbe.colors.http;

import de.bennyboer.kicherkrabbe.changes.ResourceChange;
import de.bennyboer.kicherkrabbe.changes.ResourceId;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.Set;

import static de.bennyboer.kicherkrabbe.changes.ResourceChangeType.PERMISSIONS_ADDED;
import static de.bennyboer.kicherkrabbe.changes.ResourceChangeType.PERMISSIONS_REMOVED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class ChangesHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyGetAccessibleColorChanges() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a successful response
        when(module.getColorChanges(
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Flux.just(
                ResourceChange.of(PERMISSIONS_ADDED, Set.of(ResourceId.of("COLOR_ID")), Map.of()),
                ResourceChange.of(PERMISSIONS_REMOVED, Set.of(ResourceId.of("COLOR_ID")), Map.of())
        ));

        // when: posting the request
        var exchange = client.get()
                .uri("/api/colors/changes")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the events
        var events = exchange.expectBodyList(String.class)
                .returnResult()
                .getResponseBody();
        assertThat(events).containsExactly(
                "{\"type\":\"PERMISSIONS_ADDED\",\"affected\":[\"COLOR_ID\"],\"payload\":{}}",
                "{\"type\":\"PERMISSIONS_REMOVED\",\"affected\":[\"COLOR_ID\"],\"payload\":{}}"
        );
    }

    @Test
    void shouldNotAllowUnauthorizedAccess() {
        // when: posting the request without a token
        var exchange = client.get()
                .uri("/api/colors/changes")
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldNotAllowAccessWithInvalidToken() {
        // when: posting the request with an invalid token
        var exchange = client.get()
                .uri("/api/colors/changes")
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

}
