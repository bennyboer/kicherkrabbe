package de.bennyboer.kicherkrabbe.mailing.http;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.mailing.Actions;
import de.bennyboer.kicherkrabbe.mailing.api.requests.UpdateRateLimitRequest;
import de.bennyboer.kicherkrabbe.mailing.api.responses.UpdateRateLimitResponse;
import de.bennyboer.kicherkrabbe.permissions.*;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class UpdateRateLimitHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyUpdateRateLimit() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: a request to update the rate limit
        var request = new UpdateRateLimitRequest();
        request.version = 3L;
        request.durationInMs = 1000L;
        request.limit = 10;

        // and: the module is configured to return a successful response
        var response = new UpdateRateLimitResponse();
        response.version = 4L;

        when(module.updateRateLimit(
                eq(request),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.just(response));

        // when: posting the request
        var exchange = client.post()
                .uri(builder -> builder.path("/api/mailing/settings/rate-limit/update").build())
                .headers(headers -> headers.setBearerAuth(token))
                .bodyValue(request)
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the expected result
        var result = exchange.expectBody(UpdateRateLimitResponse.class).returnResult().getResponseBody();
        assertThat(result.version).isEqualTo(response.version);
    }

    @Test
    void shouldRespondWithUnauthorizedForAnonymousAccess() {
        // when: posting the request
        var exchange = client.post()
                .uri("/api/mailing/settings/rate-limit/update")
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithUnauthorizedForInvalidToken() {
        // when: posting the request
        var exchange = client.post()
                .uri("/api/mailing/settings/rate-limit/update")
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
        var request = new UpdateRateLimitRequest();
        request.version = 3L;
        request.durationInMs = 1000L;
        request.limit = 10;

        when(module.updateRateLimit(
                eq(request),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.error(new MissingPermissionError(
                Permission.builder()
                        .holder(Holder.user(HolderId.of("USER_ID")))
                        .isAllowedTo(Actions.UPDATE_RATE_LIMIT)
                        .onType(ResourceType.of("MAILING_SETTINGS"))
        )));

        // when: posting the request
        var exchange = client.post()
                .uri("/api/mailing/settings/rate-limit/update")
                .headers(headers -> headers.setBearerAuth(token))
                .bodyValue(request)
                .exchange();

        // then: the response is forbidden
        exchange.expectStatus().isForbidden();
    }

    @Test
    void shouldRespondWithConflictForOutdatedVersion() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: a request to update the rate limit
        var request = new UpdateRateLimitRequest();
        request.version = 3L;
        request.durationInMs = 1000L;
        request.limit = 10;

        // and: the module is configured to return an error
        when(module.updateRateLimit(
                eq(request),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.error(new AggregateVersionOutdatedError(
                AggregateType.of("MAILING_SETTINGS"),
                AggregateId.of("DEFAULT"),
                Version.of(3L)
        )));

        // when: posting the request
        var exchange = client.post()
                .uri("/api/mailing/settings/rate-limit/update")
                .headers(headers -> headers.setBearerAuth(token))
                .bodyValue(request)
                .exchange();

        // then: the response is conflict (409)
        exchange.expectStatus().isEqualTo(409);
    }

}
