package de.bennyboer.kicherkrabbe.mailbox.http;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.mailbox.api.responses.DeleteMailResponse;
import de.bennyboer.kicherkrabbe.permissions.*;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static de.bennyboer.kicherkrabbe.mailbox.Actions.DELETE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class DeleteMailHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyDeleteMail() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a successful response
        var response = new DeleteMailResponse();
        response.version = 3L;

        when(module.deleteMail(
                eq("SOME_MAIL_ID"),
                eq(2L),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.just(response));

        // when: posting the request
        var exchange = client.delete()
                .uri(builder -> builder.path("/mailbox/mails/{mailId}")
                        .queryParam("version", 2L)
                        .build("SOME_MAIL_ID"))
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the new version of the mail
        var result = exchange.expectBody(DeleteMailResponse.class).returnResult().getResponseBody();
        assertThat(result.version).isEqualTo(3L);
    }

    @Test
    void shouldRespondWith404WhenMailDoesNotExist() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return an empty response
        var response = new DeleteMailResponse();
        response.version = 3L;

        when(module.deleteMail(
                eq("SOME_MAIL_ID"),
                eq(2L),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.empty());

        // when: posting the request
        var exchange = client.delete()
                .uri(builder -> builder.path("/mailbox/mails/{mailId}")
                        .queryParam("version", 2L)
                        .build("SOME_MAIL_ID"))
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is not found
        exchange.expectStatus().isNotFound();

        // and: the response contains no body
        exchange.expectBody().isEmpty();
    }

    @Test
    void shouldRespondWithUnauthorizedForAnonymousAccess() {
        // when: posting the request
        var exchange = client.delete()
                .uri("/mailbox/mails/SOME_MAIL_ID?version=2")
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithUnauthorizedForInvalidToken() {
        // when: posting the request
        var exchange = client.delete()
                .uri("/mailbox/mails/SOME_MAIL_ID?version=2")
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
        when(module.deleteMail(
                eq("SOME_MAIL_ID"),
                eq(2L),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.error(new MissingPermissionError(
                Permission.builder()
                        .holder(Holder.user(HolderId.of("USER_ID")))
                        .isAllowedTo(DELETE)
                        .on(Resource.of(ResourceType.of("MAIL"), ResourceId.of("SOME_MAIL_ID")))
        )));

        // when: posting the request
        var exchange = client.delete()
                .uri("/mailbox/mails/SOME_MAIL_ID?version=2")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is forbidden
        exchange.expectStatus().isForbidden();
    }

    @Test
    void shouldRespondWith409WhenVersionMismatch() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return an error
        when(module.deleteMail(
                eq("SOME_MAIL_ID"),
                eq(2L),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.error(new AggregateVersionOutdatedError(
                AggregateType.of("MAIL"),
                AggregateId.of("SOME_MAIL_ID"),
                Version.of(3L)
        )));

        // when: posting the request
        var exchange = client.delete()
                .uri("/mailbox/mails/SOME_MAIL_ID?version=2")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is 409 Conflict
        exchange.expectStatus().isEqualTo(409);
    }

    @Test
    void shouldRespondWithBadRequestIfVersionQueryParamIsMissing() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // when: posting the request
        var exchange = client.delete()
                .uri("/mailbox/mails/SOME_MAIL_ID")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is bad request
        exchange.expectStatus().isBadRequest();
    }

}
