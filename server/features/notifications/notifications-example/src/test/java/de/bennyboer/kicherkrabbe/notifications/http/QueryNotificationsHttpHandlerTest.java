package de.bennyboer.kicherkrabbe.notifications.http;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.notifications.Actions;
import de.bennyboer.kicherkrabbe.notifications.DateRangeFilter;
import de.bennyboer.kicherkrabbe.notifications.api.*;
import de.bennyboer.kicherkrabbe.notifications.api.responses.QueryNotificationsResponse;
import de.bennyboer.kicherkrabbe.permissions.*;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class QueryNotificationsHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyQueryNotifications() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a successful response
        var response = new QueryNotificationsResponse();
        response.total = 102;
        response.notifications = new ArrayList<>();

        var notification1 = new NotificationDTO();
        notification1.id = "NOTIFICATION_ID_1";
        notification1.version = 1L;
        notification1.origin = new OriginDTO();
        notification1.origin.type = OriginTypeDTO.MAIL;
        notification1.origin.id = "MAIL_ID_1";
        notification1.target = new TargetDTO();
        notification1.target.type = TargetTypeDTO.SYSTEM;
        notification1.target.id = "SYSTEM";
        notification1.title = "Some title";
        notification1.message = "Some message";
        notification1.sentAt = Instant.parse("2024-12-03T12:15:00.000Z");
        response.notifications.add(notification1);

        var notification2 = new NotificationDTO();
        notification2.id = "NOTIFICATION_ID_2";
        notification2.version = 0L;
        notification2.origin = new OriginDTO();
        notification2.origin.type = OriginTypeDTO.MAIL;
        notification2.origin.id = "MAIL_ID_2";
        notification2.target = new TargetDTO();
        notification2.target.type = TargetTypeDTO.SYSTEM;
        notification2.target.id = "SYSTEM";
        notification2.title = "Mail received";
        notification2.message = "You have received a new mail.";
        notification2.sentAt = Instant.parse("2024-12-03T12:30:00.000Z");
        response.notifications.add(notification2);

        when(module.getNotifications(
                eq(DateRangeFilter.of(
                        Instant.parse("2024-12-03T00:00:00.000Z"),
                        Instant.parse("2024-12-04T00:00:00.000Z")
                )),
                eq(100L),
                eq(300L),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.just(response));

        // when: posting the request
        var exchange = client.get()
                .uri(builder -> builder.path("/api/notifications")
                        .queryParam("from", "2024-12-03T00:00:00.000Z")
                        .queryParam("to", "2024-12-04T00:00:00.000Z")
                        .queryParam("skip", "100")
                        .queryParam("limit", "300")
                        .build())
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the expected notifications
        var result = exchange.expectBody(QueryNotificationsResponse.class).returnResult().getResponseBody();
        assertThat(result.total).isEqualTo(102);
        assertThat(result.notifications).hasSize(2);
        assertThat(result.notifications.get(0)).isEqualTo(notification1);
        assertThat(result.notifications.get(1)).isEqualTo(notification2);
    }

    @Test
    void shouldQueryNotificationsSuccessfullyWithoutQueryParams() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a successful response
        var response = new QueryNotificationsResponse();
        response.total = 2;
        response.notifications = new ArrayList<>();

        var notification1 = new NotificationDTO();
        notification1.id = "NOTIFICATION_ID_1";
        notification1.version = 1L;
        notification1.origin = new OriginDTO();
        notification1.origin.type = OriginTypeDTO.MAIL;
        notification1.origin.id = "MAIL_ID_1";
        notification1.target = new TargetDTO();
        notification1.target.type = TargetTypeDTO.SYSTEM;
        notification1.target.id = "SYSTEM";
        notification1.title = "Some title";
        notification1.message = "Some message";
        notification1.sentAt = Instant.parse("2024-12-03T12:15:00.000Z");
        response.notifications.add(notification1);

        var notification2 = new NotificationDTO();
        notification2.id = "NOTIFICATION_ID_2";
        notification2.version = 0L;
        notification2.origin = new OriginDTO();
        notification2.origin.type = OriginTypeDTO.MAIL;
        notification2.origin.id = "MAIL_ID_2";
        notification2.target = new TargetDTO();
        notification2.target.type = TargetTypeDTO.SYSTEM;
        notification2.target.id = "SYSTEM";
        notification2.title = "Mail received";
        notification2.message = "You have received a new mail.";
        notification2.sentAt = Instant.parse("2024-12-03T12:30:00.000Z");
        response.notifications.add(notification2);

        when(module.getNotifications(
                eq(DateRangeFilter.empty()),
                eq(0L),
                eq(100L),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.just(response));

        // when: posting the request
        var exchange = client.get()
                .uri(builder -> builder.path("/api/notifications").build())
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the expected notifications
        var result = exchange.expectBody(QueryNotificationsResponse.class).returnResult().getResponseBody();
        assertThat(result.total).isEqualTo(2);
        assertThat(result.notifications).hasSize(2);
        assertThat(result.notifications.get(0)).isEqualTo(notification1);
        assertThat(result.notifications.get(1)).isEqualTo(notification2);
    }

    @Test
    void shouldCorrectNegativeSkipAndLimitQueryParams() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a successful response
        var response = new QueryNotificationsResponse();
        response.total = 0;
        response.notifications = new ArrayList<>();

        when(module.getNotifications(
                eq(DateRangeFilter.empty()),
                eq(0L),
                eq(100L),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.just(response));

        // when: posting the request
        var exchange = client.get()
                .uri(builder -> builder.path("/api/notifications")
                        .queryParam("skip", "-100")
                        .queryParam("limit", "-100")
                        .build())
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the expected notifications
        var result = exchange.expectBody(QueryNotificationsResponse.class).returnResult().getResponseBody();
        assertThat(result.total).isEqualTo(0);
        assertThat(result.notifications).isEmpty();
    }

    @Test
    void shouldRespondWithUnauthorizedForAnonymousAccess() {
        // when: posting the request
        var exchange = client.get()
                .uri("/api/notifications")
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithUnauthorizedForInvalidToken() {
        // when: posting the request
        var exchange = client.get()
                .uri("/api/notifications")
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
        when(module.getNotifications(
                eq(DateRangeFilter.empty()),
                eq(0L),
                eq(100L),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.error(new MissingPermissionError(
                Permission.builder()
                        .holder(Holder.user(HolderId.of("USER_ID")))
                        .isAllowedTo(Actions.READ)
                        .onType(ResourceType.of("NOTIFICATION"))
        )));

        // when: posting the request
        var exchange = client.get()
                .uri("/api/notifications")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is forbidden
        exchange.expectStatus().isForbidden();
    }

}
