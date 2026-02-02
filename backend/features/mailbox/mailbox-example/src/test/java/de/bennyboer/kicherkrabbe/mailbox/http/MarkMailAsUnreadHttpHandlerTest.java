package de.bennyboer.kicherkrabbe.mailbox.http;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.mailbox.api.requests.MarkMailAsUnreadRequest;
import de.bennyboer.kicherkrabbe.mailbox.api.responses.MarkMailAsReadResponse;
import de.bennyboer.kicherkrabbe.mailbox.api.responses.MarkMailAsUnreadResponse;
import de.bennyboer.kicherkrabbe.permissions.*;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static de.bennyboer.kicherkrabbe.mailbox.Actions.MARK_AS_UNREAD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class MarkMailAsUnreadHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyMarkMailAsUnread() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: a request to mark the mail as unread
        var request = new MarkMailAsUnreadRequest();
        request.version = 2L;

        // and: the module is configured to return a successful response
        var response = new MarkMailAsUnreadResponse();
        response.version = 3L;

        when(module.markMailAsUnread(
                eq("SOME_MAIL_ID"),
                eq(request),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.just(response));

        // when: posting the request
        var exchange = client.post()
                .uri(builder -> builder.path("/mailbox/mails/{mailId}/unread")
                        .build("SOME_MAIL_ID"))
                .headers(headers -> headers.setBearerAuth(token))
                .bodyValue(request)
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the new version of the mail
        var result = exchange.expectBody(MarkMailAsReadResponse.class).returnResult().getResponseBody();
        assertThat(result.version).isEqualTo(3L);
    }

    @Test
    void shouldRespondWith404WhenMailDoesNotExist() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: a request to mark the mail as unread
        var request = new MarkMailAsUnreadRequest();
        request.version = 2L;

        // and: the module is configured to return an empty response
        var response = new MarkMailAsUnreadResponse();
        response.version = 3L;

        when(module.markMailAsUnread(
                eq("SOME_MAIL_ID"),
                eq(request),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.empty());

        // when: posting the request
        var exchange = client.post()
                .uri(builder -> builder.path("/mailbox/mails/{mailId}/unread")
                        .build("SOME_MAIL_ID"))
                .headers(headers -> headers.setBearerAuth(token))
                .bodyValue(request)
                .exchange();

        // then: the response is not found
        exchange.expectStatus().isNotFound();

        // and: the response contains no body
        exchange.expectBody().isEmpty();
    }

    @Test
    void shouldRespondWithUnauthorizedForAnonymousAccess() {
        // when: posting the request
        var exchange = client.post()
                .uri("/mailbox/mails/SOME_MAIL_ID/unread")
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithUnauthorizedForInvalidToken() {
        // when: posting the request
        var exchange = client.post()
                .uri("/mailbox/mails/SOME_MAIL_ID/unread")
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithForbiddenForMissingPermission() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: a request to mark the mail as unread
        var request = new MarkMailAsUnreadRequest();
        request.version = 2L;

        // and: the module is configured to return an error
        when(module.markMailAsUnread(
                eq("SOME_MAIL_ID"),
                eq(request),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.error(new MissingPermissionError(
                Permission.builder()
                        .holder(Holder.user(HolderId.of("USER_ID")))
                        .isAllowedTo(MARK_AS_UNREAD)
                        .on(Resource.of(ResourceType.of("MAIL"), ResourceId.of("SOME_MAIL_ID")))
        )));

        // when: posting the request
        var exchange = client.post()
                .uri("/mailbox/mails/SOME_MAIL_ID/unread")
                .headers(headers -> headers.setBearerAuth(token))
                .bodyValue(request)
                .exchange();

        // then: the response is forbidden
        exchange.expectStatus().isForbidden();
    }

    @Test
    void shouldRespondWith409WhenVersionMismatch() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: a request to mark the mail as unread
        var request = new MarkMailAsUnreadRequest();
        request.version = 2L;

        // and: the module is configured to return an error
        when(module.markMailAsUnread(
                eq("SOME_MAIL_ID"),
                eq(request),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.error(new AggregateVersionOutdatedError(
                AggregateType.of("MAIL"),
                AggregateId.of("SOME_MAIL_ID"),
                Version.of(3L)
        )));

        // when: posting the request
        var exchange = client.post()
                .uri("/mailbox/mails/SOME_MAIL_ID/unread")
                .headers(headers -> headers.setBearerAuth(token))
                .bodyValue(request)
                .exchange();

        // then: the response is 409 Conflict
        exchange.expectStatus().isEqualTo(409);
    }

    @Test
    void shouldRespondWithBadRequestIfVersionIsNegativeInRequest() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: a request to mark the mail as unread
        var request = new MarkMailAsUnreadRequest();
        request.version = -1L;

        // when: posting the request
        var exchange = client.post()
                .uri("/mailbox/mails/SOME_MAIL_ID/unread")
                .headers(headers -> headers.setBearerAuth(token))
                .bodyValue(request)
                .exchange();

        // then: the response is bad request
        exchange.expectStatus().isBadRequest();
    }

}
