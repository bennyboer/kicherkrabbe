package de.bennyboer.kicherkrabbe.telegram.http;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.*;
import de.bennyboer.kicherkrabbe.telegram.Actions;
import de.bennyboer.kicherkrabbe.telegram.api.BotSettingsDTO;
import de.bennyboer.kicherkrabbe.telegram.api.SettingsDTO;
import de.bennyboer.kicherkrabbe.telegram.api.responses.QuerySettingsResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class QuerySettingsHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyQuerySettings() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a successful response
        var response = new QuerySettingsResponse();
        response.settings = new SettingsDTO();
        response.settings.version = 3L;
        response.settings.botSettings = new BotSettingsDTO();
        response.settings.botSettings.maskedApiToken = "********";

        when(module.getSettings(
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.just(response));

        // when: posting the request
        var exchange = client.get()
                .uri(builder -> builder.path("/telegram/settings").build())
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the expected settings
        var result = exchange.expectBody(QuerySettingsResponse.class).returnResult().getResponseBody();
        assertThat(result.settings).isEqualTo(response.settings);
    }

    @Test
    void shouldRespondWithUnauthorizedForAnonymousAccess() {
        // when: posting the request
        var exchange = client.get()
                .uri("/telegram/settings")
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithUnauthorizedForInvalidToken() {
        // when: posting the request
        var exchange = client.get()
                .uri("/telegram/settings")
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithForbiddenForMissingPermission() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return an error
        when(module.getSettings(
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.error(new MissingPermissionError(
                Permission.builder()
                        .holder(Holder.user(HolderId.of("USER_ID")))
                        .isAllowedTo(Actions.READ)
                        .onType(ResourceType.of("TELEGRAM_SETTINGS"))
        )));

        // when: posting the request
        var exchange = client.get()
                .uri("/telegram/settings")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is forbidden
        exchange.expectStatus().isForbidden();
    }

}
