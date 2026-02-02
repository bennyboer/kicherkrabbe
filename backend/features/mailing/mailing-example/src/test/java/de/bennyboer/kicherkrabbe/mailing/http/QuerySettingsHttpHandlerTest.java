package de.bennyboer.kicherkrabbe.mailing.http;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.mailing.Actions;
import de.bennyboer.kicherkrabbe.mailing.api.MailgunSettingsDTO;
import de.bennyboer.kicherkrabbe.mailing.api.SettingsDTO;
import de.bennyboer.kicherkrabbe.mailing.api.responses.QuerySettingsResponse;
import de.bennyboer.kicherkrabbe.permissions.*;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
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
        response.settings.mailgun = new MailgunSettingsDTO();
        response.settings.mailgun.maskedApiToken = "****ABCD";

        when(module.getSettings(
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.just(response));

        // when: posting the request
        var exchange = client.get()
                .uri(builder -> builder.path("/mailing/settings").build())
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
                .uri("/mailing/settings")
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithUnauthorizedForInvalidToken() {
        // when: posting the request
        var exchange = client.get()
                .uri("/mailing/settings")
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
                        .onType(ResourceType.of("MAILING_SETTINGS"))
        )));

        // when: posting the request
        var exchange = client.get()
                .uri("/mailing/settings")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is forbidden
        exchange.expectStatus().isForbidden();
    }

}
