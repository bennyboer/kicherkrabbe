package de.bennyboer.kicherkrabbe.highlights.http;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.highlights.Actions;
import de.bennyboer.kicherkrabbe.highlights.api.requests.UpdateHighlightSortOrderRequest;
import de.bennyboer.kicherkrabbe.highlights.api.responses.UpdateHighlightSortOrderResponse;
import de.bennyboer.kicherkrabbe.permissions.*;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class UpdateSortOrderHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyUpdateSortOrder() {
        var token = createTokenForUser("USER_ID");

        var request = new UpdateHighlightSortOrderRequest();
        request.version = 2L;
        request.sortOrder = 5;

        when(module.updateSortOrder(
                eq("HIGHLIGHT_ID"),
                eq(2L),
                eq(5L),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.just(3L));

        var exchange = client.post()
                .uri("/highlights/{highlightId}/update/sort-order", "HIGHLIGHT_ID")
                .headers(headers -> headers.setBearerAuth(token))
                .bodyValue(request)
                .exchange();

        exchange.expectStatus().isOk();

        var result = exchange.expectBody(UpdateHighlightSortOrderResponse.class).returnResult().getResponseBody();
        assertThat(result.version).isEqualTo(3L);
    }

    @Test
    void shouldRespondWithUnauthorizedForAnonymousAccess() {
        var exchange = client.post()
                .uri("/highlights/HIGHLIGHT_ID/update/sort-order")
                .exchange();

        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithUnauthorizedForInvalidToken() {
        var exchange = client.post()
                .uri("/highlights/HIGHLIGHT_ID/update/sort-order")
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithForbiddenForMissingPermission() {
        var token = createTokenForUser("USER_ID");

        var request = new UpdateHighlightSortOrderRequest();
        request.version = 2L;
        request.sortOrder = 5;

        when(module.updateSortOrder(
                eq("HIGHLIGHT_ID"),
                eq(2L),
                eq(5L),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.error(new MissingPermissionError(
                Permission.builder()
                        .holder(Holder.user(HolderId.of("USER_ID")))
                        .isAllowedTo(Actions.UPDATE_SORT_ORDER)
                        .on(Resource.of(ResourceType.of("HIGHLIGHT"), ResourceId.of("HIGHLIGHT_ID")))
        )));

        var exchange = client.post()
                .uri("/highlights/HIGHLIGHT_ID/update/sort-order")
                .headers(headers -> headers.setBearerAuth(token))
                .bodyValue(request)
                .exchange();

        exchange.expectStatus().isForbidden();
    }

    @Test
    void shouldRespondWith409WhenVersionMismatch() {
        var token = createTokenForUser("USER_ID");

        var request = new UpdateHighlightSortOrderRequest();
        request.version = 2L;
        request.sortOrder = 5;

        when(module.updateSortOrder(
                eq("HIGHLIGHT_ID"),
                eq(2L),
                eq(5L),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.error(new AggregateVersionOutdatedError(
                AggregateType.of("HIGHLIGHT"),
                AggregateId.of("HIGHLIGHT_ID"),
                Version.of(3L)
        )));

        var exchange = client.post()
                .uri("/highlights/HIGHLIGHT_ID/update/sort-order")
                .headers(headers -> headers.setBearerAuth(token))
                .bodyValue(request)
                .exchange();

        exchange.expectStatus().isEqualTo(409);
    }

}
