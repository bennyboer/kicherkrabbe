package de.bennyboer.kicherkrabbe.mailbox.http;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.mailbox.Actions;
import de.bennyboer.kicherkrabbe.mailbox.api.MailDTO;
import de.bennyboer.kicherkrabbe.mailbox.api.OriginDTO;
import de.bennyboer.kicherkrabbe.mailbox.api.OriginTypeDTO;
import de.bennyboer.kicherkrabbe.mailbox.api.SenderDTO;
import de.bennyboer.kicherkrabbe.mailbox.api.responses.QueryMailResponse;
import de.bennyboer.kicherkrabbe.permissions.*;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class QueryMailHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyQueryMail() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a successful response
        var response = new QueryMailResponse();
        response.mail = new MailDTO();
        response.mail.id = "SOME_MAIL_ID";
        response.mail.origin = new OriginDTO();
        response.mail.origin.type = OriginTypeDTO.INQUIRY;
        response.mail.origin.id = "INQUIRY_ID_1";
        response.mail.sender = new SenderDTO();
        response.mail.sender.name = "John Doe";
        response.mail.sender.mail = "john.doe@kicherkrabbe.com";
        response.mail.sender.phone = "+49123456789";
        response.mail.subject = "Some subject";
        response.mail.content = "Some content";
        response.mail.receivedAt = Instant.parse("2024-12-03T12:15:00.000Z");
        response.mail.readAt = Instant.parse("2024-12-03T12:30:00.000Z");

        when(module.getMail(
                eq("SOME_MAIL_ID"),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.just(response));

        // when: posting the request
        var exchange = client.get()
                .uri(builder -> builder.path("/mailbox/mails/{mailId}").build("SOME_MAIL_ID"))
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the expected mail
        var result = exchange.expectBody(QueryMailResponse.class).returnResult().getResponseBody();
        assertThat(result.mail).isEqualTo(response.mail);
    }

    @Test
    void shouldRespondWith404WhenMailDoesNotExist() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return an empty response
        when(module.getMail(
                eq("SOME_MAIL_ID"),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.empty());

        // when: posting the request
        var exchange = client.get()
                .uri(builder -> builder.path("/mailbox/mails/{mailId}").build("SOME_MAIL_ID"))
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
        var exchange = client.get()
                .uri("/mailbox/mails/SOME_MAIL_ID")
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithUnauthorizedForInvalidToken() {
        // when: posting the request
        var exchange = client.get()
                .uri("/mailbox/mails/SOME_MAIL_ID")
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
                        .on(Resource.of(ResourceType.of("MAIL"), ResourceId.of("SOME_MAIL_ID")))
        )));

        // when: posting the request
        var exchange = client.get()
                .uri("/mailbox/mails/SOME_MAIL_ID")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is forbidden
        exchange.expectStatus().isForbidden();
    }

}
