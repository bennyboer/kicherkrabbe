package de.bennyboer.kicherkrabbe.inquiries.http;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.inquiries.api.RateLimitDTO;
import de.bennyboer.kicherkrabbe.inquiries.api.RateLimitsDTO;
import de.bennyboer.kicherkrabbe.inquiries.api.requests.UpdateRateLimitsRequest;
import de.bennyboer.kicherkrabbe.permissions.*;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static de.bennyboer.kicherkrabbe.inquiries.Actions.UPDATE_RATE_LIMITS;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class UpdateRateLimitsHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyUpdateRateLimits() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a successful response
        var request = new UpdateRateLimitsRequest();
        request.rateLimits = new RateLimitsDTO();
        request.rateLimits.perMail = new RateLimitDTO();
        request.rateLimits.perMail.maxRequests = 3;
        request.rateLimits.perMail.duration = Duration.ofHours(24);
        request.rateLimits.perIp = new RateLimitDTO();
        request.rateLimits.perIp.maxRequests = 5;
        request.rateLimits.perIp.duration = Duration.ofHours(48);
        request.rateLimits.overall = new RateLimitDTO();
        request.rateLimits.overall.maxRequests = 100;
        request.rateLimits.overall.duration = Duration.ofHours(72);

        when(module.updateRateLimits(
                eq(request),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.empty());

        // when: posting the request
        var exchange = client.post()
                .uri("/api/inquiries/settings/rate-limits")
                .bodyValue(request)
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
                .uri("/api/inquiries/settings/rate-limits")
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithUnauthorizedForInvalidToken() {
        // when: posting the request
        var exchange = client.post()
                .uri("/api/inquiries/settings/rate-limits")
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
        var request = new UpdateRateLimitsRequest();
        request.rateLimits = new RateLimitsDTO();
        request.rateLimits.perMail = new RateLimitDTO();
        request.rateLimits.perMail.maxRequests = 3;
        request.rateLimits.perMail.duration = Duration.ofHours(24);
        request.rateLimits.perIp = new RateLimitDTO();
        request.rateLimits.perIp.maxRequests = 5;
        request.rateLimits.perIp.duration = Duration.ofHours(48);
        request.rateLimits.overall = new RateLimitDTO();
        request.rateLimits.overall.maxRequests = 100;
        request.rateLimits.overall.duration = Duration.ofHours(72);
        when(module.updateRateLimits(
                eq(request),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.error(new MissingPermissionError(
                Permission.builder()
                        .holder(Holder.user(HolderId.of("USER_ID")))
                        .isAllowedTo(UPDATE_RATE_LIMITS)
                        .onType(ResourceType.of("INQUIRY"))
        )));

        // when: posting the request
        var exchange = client.post()
                .uri("/api/inquiries/settings/rate-limits")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is forbidden
        exchange.expectStatus().isForbidden();
    }

}
