package de.bennyboer.kicherkrabbe.telegram.http;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.*;
import de.bennyboer.kicherkrabbe.telegram.Actions;
import de.bennyboer.kicherkrabbe.telegram.api.requests.UpdateBotApiTokenRequest;
import de.bennyboer.kicherkrabbe.telegram.api.responses.UpdateBotApiTokenResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class UpdateBotApiTokenHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyUpdateBotApiToken() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: a request to update the bot API token
        var request = new UpdateBotApiTokenRequest();
        request.version = 3L;
        request.apiToken = "NEW_API_TOKEN";

        // and: the module is configured to return a successful response
        var response = new UpdateBotApiTokenResponse();
        response.version = 4L;

        when(module.updateBotApiToken(
                eq(request),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.just(response));

        // when: posting the request
        var exchange = client.post()
                .uri(builder -> builder.path("/api/telegram/settings/bot/api-token/update").build())
                .headers(headers -> headers.setBearerAuth(token))
                .bodyValue(request)
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the expected result
        var result = exchange.expectBody(UpdateBotApiTokenResponse.class).returnResult().getResponseBody();
        assertThat(result.version).isEqualTo(response.version);
    }

    @Test
    void shouldRespondWithUnauthorizedForAnonymousAccess() {
        // when: posting the request
        var exchange = client.post()
                .uri("/api/telegram/settings/bot/api-token/update")
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithUnauthorizedForInvalidToken() {
        // when: posting the request
        var exchange = client.post()
                .uri("/api/telegram/settings/bot/api-token/update")
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
        var request = new UpdateBotApiTokenRequest();
        request.version = 3L;
        request.apiToken = "NEW_API_TOKEN";

        when(module.updateBotApiToken(
                eq(request),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.error(new MissingPermissionError(
                Permission.builder()
                        .holder(Holder.user(HolderId.of("USER_ID")))
                        .isAllowedTo(Actions.UPDATE_BOT_API_TOKEN)
                        .onType(ResourceType.of("TELEGRAM_SETTINGS"))
        )));

        // when: posting the request
        var exchange = client.post()
                .uri("/api/telegram/settings/bot/api-token/update")
                .headers(headers -> headers.setBearerAuth(token))
                .bodyValue(request)
                .exchange();

        // then: the response is forbidden
        exchange.expectStatus().isForbidden();
    }

    @Test
    void shouldRespondWithConflictForOutdatedVersion() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: a request to update the bot API token
        var request = new UpdateBotApiTokenRequest();
        request.version = 3L;
        request.apiToken = "NEW_API_TOKEN";

        // and: the module is configured to return an error
        when(module.updateBotApiToken(
                eq(request),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.error(new AggregateVersionOutdatedError(
                AggregateType.of("TELEGRAM_SETTINGS"),
                AggregateId.of("DEFAULT"),
                Version.of(3L)
        )));

        // when: posting the request
        var exchange = client.post()
                .uri("/api/telegram/settings/bot/api-token/update")
                .headers(headers -> headers.setBearerAuth(token))
                .bodyValue(request)
                .exchange();

        // then: the response is conflict (409)
        exchange.expectStatus().isEqualTo(409);
    }

}
