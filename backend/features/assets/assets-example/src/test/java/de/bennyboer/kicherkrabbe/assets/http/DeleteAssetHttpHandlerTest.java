package de.bennyboer.kicherkrabbe.assets.http;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class DeleteAssetHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyDeleteAsset() {
        // given: having a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a successful response
        when(module.deleteAsset(
                eq("ASSET_ID"),
                eq(42L),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.empty());

        // when: posting the request
        var exchange = client.delete()
                .uri("/assets/ASSET_ID/?version=42")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();
    }

    @Test
    void shouldNotAllowUnauthorizedAccess() {
        // when: posting the request
        var exchange = client.delete()
                .uri("/assets/ASSET_ID/")
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldNotAllowAccessWithInvalidToken() {
        // when: posting the request
        var exchange = client.delete()
                .uri("/assets/ASSET_ID/")
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

}
