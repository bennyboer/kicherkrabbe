package de.bennyboer.kicherkrabbe.mailing.http;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.mailing.Actions;
import de.bennyboer.kicherkrabbe.mailing.api.MailDTO;
import de.bennyboer.kicherkrabbe.mailing.api.ReceiverDTO;
import de.bennyboer.kicherkrabbe.mailing.api.SenderDTO;
import de.bennyboer.kicherkrabbe.mailing.api.responses.QueryMailsResponse;
import de.bennyboer.kicherkrabbe.permissions.*;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class QueryMailsHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyQueryMailsWithoutQueryParams() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a successful response
        var response = new QueryMailsResponse();
        response.total = 2L;
        response.mails = new ArrayList<>();

        var receiver1 = new ReceiverDTO();
        receiver1.mail = "john.doe@kicherkrabbe.com";
        var receiver2 = new ReceiverDTO();
        receiver2.mail = "jane.doe@kicherkrabbe.com";

        var mail1 = new MailDTO();
        mail1.id = "MAIL_ID_1";
        mail1.version = 1L;
        mail1.sender = new SenderDTO();
        mail1.sender.mail = "no-reply@kicherkrabbe.com";
        mail1.receivers = Set.of(receiver1);
        mail1.subject = "Subject 1";
        mail1.text = "Body 1";
        mail1.sentAt = Instant.parse("2024-12-03T10:15:30.00Z");

        var mail2 = new MailDTO();
        mail2.id = "MAIL_ID_2";
        mail2.version = 0L;
        mail2.sender = new SenderDTO();
        mail2.sender.mail = "no-reply@kicherkrabbe.com";
        mail2.receivers = Set.of(receiver2);
        mail2.subject = "Subject 2";
        mail2.text = "Body 2";
        mail2.sentAt = Instant.parse("2024-12-03T12:30:00.00Z");

        response.mails.add(mail1);
        response.mails.add(mail2);

        when(module.getMails(
                eq(0L),
                eq(100L),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.just(response));

        // when: posting the request
        var exchange = client.get()
                .uri(builder -> builder.path("/mailing/mails").build())
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the expected mails
        var result = exchange.expectBody(QueryMailsResponse.class).returnResult().getResponseBody();
        assertThat(result.total).isEqualTo(2L);
        assertThat(result.mails).isEqualTo(response.mails);
    }

    @Test
    void shouldSuccessfullyQueryMailsWithQueryParams() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a successful response
        var response = new QueryMailsResponse();
        response.total = 32L;
        response.mails = new ArrayList<>();

        var receiver1 = new ReceiverDTO();
        receiver1.mail = "john.doe@kicherkrabbe.com";
        var receiver2 = new ReceiverDTO();
        receiver2.mail = "jane.doe@kicherkrabbe.com";

        var mail1 = new MailDTO();
        mail1.id = "MAIL_ID_1";
        mail1.version = 1L;
        mail1.sender = new SenderDTO();
        mail1.sender.mail = "no-reply@kicherkrabbe.com";
        mail1.receivers = Set.of(receiver1);
        mail1.subject = "Subject 1";
        mail1.text = "Body 1";
        mail1.sentAt = Instant.parse("2024-12-03T10:15:30.00Z");

        var mail2 = new MailDTO();
        mail2.id = "MAIL_ID_2";
        mail2.version = 0L;
        mail2.sender = new SenderDTO();
        mail2.sender.mail = "no-reply@kicherkrabbe.com";
        mail2.receivers = Set.of(receiver2);
        mail2.subject = "Subject 2";
        mail2.text = "Body 2";
        mail2.sentAt = Instant.parse("2024-12-03T12:30:00.00Z");

        response.mails.add(mail1);
        response.mails.add(mail2);

        when(module.getMails(
                eq(30L),
                eq(50L),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.just(response));

        // when: posting the request
        var exchange = client.get()
                .uri(builder -> builder.path("/mailing/mails")
                        .queryParam("skip", "30")
                        .queryParam("limit", "50")
                        .build())
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the expected mails
        var result = exchange.expectBody(QueryMailsResponse.class).returnResult().getResponseBody();
        assertThat(result.total).isEqualTo(32L);
        assertThat(result.mails).isEqualTo(response.mails);
    }

    @Test
    void shouldRespondWithUnauthorizedForAnonymousAccess() {
        // when: posting the request
        var exchange = client.get()
                .uri("/mailing/mails")
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithUnauthorizedForInvalidToken() {
        // when: posting the request
        var exchange = client.get()
                .uri("/mailing/mails")
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
        when(module.getMails(
                eq(0L),
                eq(100L),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.error(new MissingPermissionError(
                Permission.builder()
                        .holder(Holder.user(HolderId.of("USER_ID")))
                        .isAllowedTo(Actions.READ)
                        .onType(ResourceType.of("MAILING_MAIL"))
        )));

        // when: posting the request
        var exchange = client.get()
                .uri("/mailing/mails")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is forbidden
        exchange.expectStatus().isForbidden();
    }

}
