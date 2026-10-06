package de.bennyboer.kicherkrabbe.embroideries.http;

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
    void shouldSuccessfullyGetEmbroideryChanges() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return some changes
        when(module.getEmbroideryChanges(Agent.user(AgentId.of("USER_ID")))).thenReturn(Flux.just(
                ResourceChange.of(PERMISSIONS_ADDED, Set.of(ResourceId.of("EMBROIDERY_ID")), Map.of()),
                ResourceChange.of(PERMISSIONS_REMOVED, Set.of(ResourceId.of("EMBROIDERY_ID")), Map.of())
        ));

        // when: requesting the changes
        var exchange = client.get()
                .uri("/embroideries/changes")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the changes
        var events = exchange.expectBodyList(String.class)
                .returnResult()
                .getResponseBody();
        assertThat(events).containsExactly(
                "{\"affected\":[\"EMBROIDERY_ID\"],\"payload\":{},\"type\":\"PERMISSIONS_ADDED\"}",
                "{\"affected\":[\"EMBROIDERY_ID\"],\"payload\":{},\"type\":\"PERMISSIONS_REMOVED\"}"
        );
    }

    @Test
    void shouldNotAllowUnauthorizedAccess() {
        // when: requesting the changes without a token
        var exchange = client.get()
                .uri("/embroideries/changes")
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

}
