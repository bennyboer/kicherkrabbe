package de.bennyboer.kicherkrabbe.mailing.http;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.mailing.Actions;
import de.bennyboer.kicherkrabbe.mailing.api.MailDTO;
import de.bennyboer.kicherkrabbe.mailing.api.ReceiverDTO;
import de.bennyboer.kicherkrabbe.mailing.api.SenderDTO;
import de.bennyboer.kicherkrabbe.mailing.api.responses.QueryMailResponse;
import de.bennyboer.kicherkrabbe.permissions.*;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class QueryMailHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyQueryMail() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a successful response
        var receiver = new ReceiverDTO();
        receiver.mail = "jane.doe@kicherkrabbe.com";

        var response = new QueryMailResponse();
        response.mail = new MailDTO();
        response.mail.id = "SOME_MAIL_ID";
        response.mail.version = 1L;
        response.mail.sender = new SenderDTO();
        response.mail.sender.mail = "john.doe@kicherkrabbe.com";
        response.mail.receivers = Set.of(receiver);
        response.mail.subject = "Subject";
        response.mail.text = "Body";
        response.mail.sentAt = Instant.parse("2024-12-03T10:15:30.00Z");

        when(module.getMail(
                eq("SOME_MAIL_ID"),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.just(response));

        // when: posting the request
        var exchange = client.get()
                .uri(builder -> builder.path("/api/mailing/mails/{mailId}").build("SOME_MAIL_ID"))
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the expected mails
        var result = exchange.expectBody(QueryMailResponse.class).returnResult().getResponseBody();
        assertThat(result).isEqualTo(response);
    }

    @Test
    void shouldRespondWithUnauthorizedForAnonymousAccess() {
        // when: posting the request
        var exchange = client.get()
                .uri(builder -> builder.path("/api/mailing/mails/{mailId}").build("SOME_MAIL_ID"))
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithUnauthorizedForInvalidToken() {
        // when: posting the request
        var exchange = client.get()
                .uri(builder -> builder.path("/api/mailing/mails/{mailId}").build("SOME_MAIL_ID"))
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
        when(module.getMail(
                eq("SOME_MAIL_ID"),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.error(new MissingPermissionError(
                Permission.builder()
                        .holder(Holder.user(HolderId.of("USER_ID")))
                        .isAllowedTo(Actions.READ)
                        .on(Resource.of(ResourceType.of("MAILING_MAIL"), ResourceId.of("SOME_MAIL_ID")))
        )));

        // when: posting the request
        var exchange = client.get()
                .uri(builder -> builder.path("/api/mailing/mails/{mailId}").build("SOME_MAIL_ID"))
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is forbidden
        exchange.expectStatus().isForbidden();
    }

}
