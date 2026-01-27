package de.bennyboer.kicherkrabbe.inquiries.http;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.inquiries.api.RateLimitDTO;
import de.bennyboer.kicherkrabbe.inquiries.api.RateLimitsDTO;
import de.bennyboer.kicherkrabbe.inquiries.api.responses.QuerySettingsResponse;
import de.bennyboer.kicherkrabbe.permissions.*;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static de.bennyboer.kicherkrabbe.inquiries.Actions.QUERY_SETTINGS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class QuerySettingsHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyQuerySettings() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a successful response
        var response = new QuerySettingsResponse();
        response.enabled = true;
        response.rateLimits = new RateLimitsDTO();
        response.rateLimits.perMail = new RateLimitDTO();
        response.rateLimits.perMail.maxRequests = 10;
        response.rateLimits.perMail.duration = Duration.ofHours(48);
        response.rateLimits.perIp = new RateLimitDTO();
        response.rateLimits.perIp.maxRequests = 100;
        response.rateLimits.perIp.duration = Duration.ofHours(24);
        response.rateLimits.overall = new RateLimitDTO();
        response.rateLimits.overall.maxRequests = 1000;
        response.rateLimits.overall.duration = Duration.ofHours(72);
        when(module.getSettings(eq(Agent.user(AgentId.of("USER_ID"))))).thenReturn(Mono.just(response));

        // when: posting the request
        var exchange = client.get()
                .uri("/api/inquiries/settings")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the expected settings
        var result = exchange.expectBody(QuerySettingsResponse.class).returnResult().getResponseBody();
        assertThat(result.enabled).isTrue();
        assertThat(result.rateLimits.perMail.maxRequests).isEqualTo(10);
        assertThat(result.rateLimits.perMail.duration).isEqualTo(Duration.ofHours(48));
        assertThat(result.rateLimits.perIp.maxRequests).isEqualTo(100);
        assertThat(result.rateLimits.perIp.duration).isEqualTo(Duration.ofHours(24));
        assertThat(result.rateLimits.overall.maxRequests).isEqualTo(1000);
        assertThat(result.rateLimits.overall.duration).isEqualTo(Duration.ofHours(72));
    }

    @Test
    void shouldRespondWithUnauthorizedForAnonymousAccess() {
        // when: posting the request
        var exchange = client.get()
                .uri("/api/inquiries/settings")
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithUnauthorizedForInvalidToken() {
        // when: posting the request
        var exchange = client.get()
                .uri("/api/inquiries/settings")
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
        when(module.getSettings(eq(Agent.user(AgentId.of("USER_ID"))))).thenReturn(Mono.error(new MissingPermissionError(
                Permission.builder()
                        .holder(Holder.user(HolderId.of("USER_ID")))
                        .isAllowedTo(QUERY_SETTINGS)
                        .onType(ResourceType.of("INQUIRY"))
        )));

        // when: posting the request
        var exchange = client.get()
                .uri("/api/inquiries/settings")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is forbidden
        exchange.expectStatus().isForbidden();
    }

}
