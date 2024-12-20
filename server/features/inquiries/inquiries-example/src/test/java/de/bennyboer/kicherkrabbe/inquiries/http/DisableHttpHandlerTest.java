package de.bennyboer.kicherkrabbe.inquiries.http;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.*;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static de.bennyboer.kicherkrabbe.inquiries.Actions.ENABLE_OR_DISABLE_INQUIRIES;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class DisableHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyDisableInquiries() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a successful response
        when(module.setSendingInquiriesEnabled(
                eq(false),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.empty());

        // when: posting the request
        var exchange = client.post()
                .uri("/api/inquiries/settings/disable")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response is empty
        exchange.expectBody().isEmpty();
    }

    @Test
    void shouldRespondWithUnauthorizedForAnonymousAccess() {
        // when: posting the request
        var exchange = client.post()
                .uri("/api/inquiries/settings/disable")
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithUnauthorizedForInvalidToken() {
        // when: posting the request
        var exchange = client.post()
                .uri("/api/inquiries/settings/disable")
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
        when(module.setSendingInquiriesEnabled(eq(false), eq(Agent.user(AgentId.of("USER_ID"))))).thenReturn(Mono.error(
                new MissingPermissionError(
                        Permission.builder()
                                .holder(Holder.user(HolderId.of("USER_ID")))
                                .isAllowedTo(ENABLE_OR_DISABLE_INQUIRIES)
                                .onType(ResourceType.of("INQUIRY"))
                )));

        // when: posting the request
        var exchange = client.post()
                .uri("/api/inquiries/settings/disable")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is forbidden
        exchange.expectStatus().isForbidden();
    }

}
