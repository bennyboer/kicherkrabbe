package de.bennyboer.kicherkrabbe.mailbox.http;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.mailbox.Actions;
import de.bennyboer.kicherkrabbe.mailbox.api.MailDTO;
import de.bennyboer.kicherkrabbe.mailbox.api.OriginDTO;
import de.bennyboer.kicherkrabbe.mailbox.api.OriginTypeDTO;
import de.bennyboer.kicherkrabbe.mailbox.api.SenderDTO;
import de.bennyboer.kicherkrabbe.mailbox.api.responses.QueryMailsResponse;
import de.bennyboer.kicherkrabbe.permissions.*;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static de.bennyboer.kicherkrabbe.mailbox.api.StatusDTO.READ;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class QueryMailsHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyQueryMails() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a successful response
        var response = new QueryMailsResponse();
        response.total = 102;
        response.mails = new ArrayList<>();

        var mail1 = new MailDTO();
        mail1.id = "MAIL_ID_1";
        mail1.origin = new OriginDTO();
        mail1.origin.type = OriginTypeDTO.INQUIRY;
        mail1.origin.id = "INQUIRY_ID_1";
        mail1.sender = new SenderDTO();
        mail1.sender.name = "John Doe";
        mail1.sender.mail = "john.doe@kicherkrabbe.com";
        mail1.sender.phone = "+49123456789";
        mail1.subject = "Some subject";
        mail1.content = "Some content";
        mail1.receivedAt = Instant.parse("2024-12-03T12:15:00.000Z");
        mail1.readAt = Instant.parse("2024-12-03T12:30:00.000Z");
        response.mails.add(mail1);

        var mail2 = new MailDTO();
        mail2.id = "MAIL_ID_2";
        mail2.origin = new OriginDTO();
        mail2.origin.type = OriginTypeDTO.INQUIRY;
        mail2.origin.id = "INQUIRY_ID_2";
        mail2.sender = new SenderDTO();
        mail2.sender.name = "Jane Doe";
        mail2.sender.mail = "jane.doe@kicherkrabbe.com";
        mail2.sender.phone = "+49876543210";
        mail2.subject = "Another subject";
        mail2.content = "Another content";
        mail2.receivedAt = Instant.parse("2024-12-03T12:45:00.000Z");
        mail2.readAt = Instant.parse("2024-12-03T13:00:00.000Z");
        response.mails.add(mail2);

        when(module.getMails(
                eq("Search term"),
                eq(READ),
                eq(100L),
                eq(300L),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.just(response));

        // when: posting the request
        var exchange = client.get()
                .uri(builder -> builder.path("/api/mailbox/mails")
                        .queryParam("searchTerm", "Search term")
                        .queryParam("status", "READ")
                        .queryParam("skip", "100")
                        .queryParam("limit", "300")
                        .build())
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the expected mails
        var result = exchange.expectBody(QueryMailsResponse.class).returnResult().getResponseBody();
        assertThat(result.total).isEqualTo(102);
        assertThat(result.mails).hasSize(2);
        assertThat(result.mails.get(0)).isEqualTo(mail1);
        assertThat(result.mails.get(1)).isEqualTo(mail2);
    }

    @Test
    void shouldQueryMailsSuccessfullyWithoutQueryParams() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a successful response
        var response = new QueryMailsResponse();
        response.total = 2;
        response.mails = new ArrayList<>();

        var mail1 = new MailDTO();
        mail1.id = "MAIL_ID_1";
        mail1.origin = new OriginDTO();
        mail1.origin.type = OriginTypeDTO.INQUIRY;
        mail1.origin.id = "INQUIRY_ID_1";
        mail1.sender = new SenderDTO();
        mail1.sender.name = "John Doe";
        mail1.sender.mail = "john.doe@kicherkrabbe.com";
        mail1.sender.phone = "+49123456789";
        mail1.subject = "Some subject";
        mail1.content = "Some content";
        mail1.receivedAt = Instant.parse("2024-12-03T12:15:00.000Z");
        mail1.readAt = Instant.parse("2024-12-03T12:30:00.000Z");
        response.mails.add(mail1);

        var mail2 = new MailDTO();
        mail2.id = "MAIL_ID_2";
        mail2.origin = new OriginDTO();
        mail2.origin.type = OriginTypeDTO.INQUIRY;
        mail2.origin.id = "INQUIRY_ID_2";
        mail2.sender = new SenderDTO();
        mail2.sender.name = "Jane Doe";
        mail2.sender.mail = "jane.doe@kicherkrabbe.com";
        mail2.sender.phone = "+49876543210";
        mail2.subject = "Another subject";
        mail2.content = "Another content";
        mail2.receivedAt = Instant.parse("2024-12-03T12:45:00.000Z");
        mail2.readAt = Instant.parse("2024-12-03T13:00:00.000Z");
        response.mails.add(mail2);

        when(module.getMails(
                eq(""),
                eq(null),
                eq(0L),
                eq(100L),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.just(response));

        // when: posting the request
        var exchange = client.get()
                .uri(builder -> builder.path("/api/mailbox/mails").build())
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the expected mails
        var result = exchange.expectBody(QueryMailsResponse.class).returnResult().getResponseBody();
        assertThat(result.total).isEqualTo(2);
        assertThat(result.mails).hasSize(2);
        assertThat(result.mails.get(0)).isEqualTo(mail1);
        assertThat(result.mails.get(1)).isEqualTo(mail2);
    }

    @Test
    void shouldCorrectNegativeSkipAndLimitQueryParams() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: a valid response
        var response = new QueryMailsResponse();
        response.total = 0;
        response.mails = List.of();

        // and: the module is configured to return an error
        when(module.getMails(
                eq(""),
                eq(null),
                eq(0L),
                eq(100L),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.just(response));

        // when: posting the request
        var exchange = client.get()
                .uri(builder -> builder.path("/api/mailbox/mails")
                        .queryParam("skip", -100)
                        .queryParam("limit", -100)
                        .build())
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is ok
        exchange.expectStatus().isOk();

        // and: the response contains the expected mails
        var result = exchange.expectBody(QueryMailsResponse.class).returnResult().getResponseBody();
        assertThat(result.total).isEqualTo(0);
        assertThat(result.mails).isEmpty();
    }

    @Test
    void shouldRespondWithUnauthorizedForAnonymousAccess() {
        // when: posting the request
        var exchange = client.get()
                .uri("/api/mailbox/mails")
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithUnauthorizedForInvalidToken() {
        // when: posting the request
        var exchange = client.get()
                .uri("/api/mailbox/mails")
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
                eq(""),
                eq(null),
                eq(0L),
                eq(100L),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.error(new MissingPermissionError(
                Permission.builder()
                        .holder(Holder.user(HolderId.of("USER_ID")))
                        .isAllowedTo(Actions.READ)
                        .onType(ResourceType.of("MAIL"))
        )));

        // when: posting the request
        var exchange = client.get()
                .uri("/api/mailbox/mails")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is forbidden
        exchange.expectStatus().isForbidden();
    }

}
