package de.bennyboer.kicherkrabbe.notifications.http;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.notifications.Actions;
import de.bennyboer.kicherkrabbe.notifications.api.requests.EnableSystemNotificationsRequest;
import de.bennyboer.kicherkrabbe.notifications.api.responses.EnableSystemNotificationsResponse;
import de.bennyboer.kicherkrabbe.permissions.*;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class EnableSystemNotificationsHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyEnableSystemNotifications() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: a request to enable system notifications
        var request = new EnableSystemNotificationsRequest();
        request.version = 3L;

        // and: the module is configured to return a successful response
        var response = new EnableSystemNotificationsResponse();
        response.version = 4L;

        when(module.enableSystemNotifications(
                eq(3L),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.just(response));

        // when: posting the request
        var exchange = client.post()
                .uri(builder -> builder.path("/notifications/settings/system/enable").build())
                .headers(headers -> headers.setBearerAuth(token))
                .bodyValue(request)
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the expected result
        var result = exchange.expectBody(EnableSystemNotificationsResponse.class).returnResult().getResponseBody();
        assertThat(result.version).isEqualTo(4L);
    }

    @Test
    void shouldRespondWithUnauthorizedForAnonymousAccess() {
        // when: posting the request
        var exchange = client.post()
                .uri("/notifications/settings/system/enable")
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithUnauthorizedForInvalidToken() {
        // when: posting the request
        var exchange = client.post()
                .uri("/notifications/settings/system/enable")
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithForbiddenForMissingPermission() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: a request to enable system notifications
        var request = new EnableSystemNotificationsRequest();
        request.version = 3L;

        // and: the module is configured to return an error
        when(module.enableSystemNotifications(
                eq(3L),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.error(new MissingPermissionError(
                Permission.builder()
                        .holder(Holder.user(HolderId.of("USER_ID")))
                        .isAllowedTo(Actions.ENABLE_SYSTEM_NOTIFICATIONS)
                        .onType(ResourceType.of("NOTIFICATION_SETTINGS"))
        )));

        // when: posting the request
        var exchange = client.post()
                .uri("/notifications/settings/system/enable")
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

        // and: a request to enable system notifications
        var request = new EnableSystemNotificationsRequest();
        request.version = 3L;

        // and: the module is configured to return an error
        when(module.enableSystemNotifications(
                eq(3L),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.error(new AggregateVersionOutdatedError(
                AggregateType.of("NOTIFICATION_SETTINGS"),
                AggregateId.of("DEFAULT"),
                Version.of(3L)
        )));

        // when: posting the request
        var exchange = client.post()
                .uri("/notifications/settings/system/enable")
                .headers(headers -> headers.setBearerAuth(token))
                .bodyValue(request)
                .exchange();

        // then: the response is conflict (409)
        exchange.expectStatus().isEqualTo(409);
    }

}
