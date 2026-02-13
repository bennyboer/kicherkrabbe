package de.bennyboer.kicherkrabbe.highlights.http;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.highlights.Actions;
import de.bennyboer.kicherkrabbe.highlights.api.requests.UnpublishHighlightRequest;
import de.bennyboer.kicherkrabbe.highlights.api.responses.UnpublishHighlightResponse;
import de.bennyboer.kicherkrabbe.highlights.unpublish.AlreadyUnpublishedError;
import de.bennyboer.kicherkrabbe.permissions.*;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class UnpublishHighlightHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyUnpublishHighlight() {
        var token = createTokenForUser("USER_ID");

        var request = new UnpublishHighlightRequest();
        request.version = 2L;

        when(module.unpublishHighlight(
                eq("HIGHLIGHT_ID"),
                eq(2L),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.just(3L));

        var exchange = client.post()
                .uri("/highlights/{highlightId}/unpublish", "HIGHLIGHT_ID")
                .headers(headers -> headers.setBearerAuth(token))
                .bodyValue(request)
                .exchange();

        exchange.expectStatus().isOk();

        var result = exchange.expectBody(UnpublishHighlightResponse.class).returnResult().getResponseBody();
        assertThat(result.version).isEqualTo(3L);
    }

    @Test
    void shouldRespondWithUnauthorizedForAnonymousAccess() {
        var exchange = client.post()
                .uri("/highlights/HIGHLIGHT_ID/unpublish")
                .exchange();

        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithUnauthorizedForInvalidToken() {
        var exchange = client.post()
                .uri("/highlights/HIGHLIGHT_ID/unpublish")
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithForbiddenForMissingPermission() {
        var token = createTokenForUser("USER_ID");

        var request = new UnpublishHighlightRequest();
        request.version = 2L;

        when(module.unpublishHighlight(
                eq("HIGHLIGHT_ID"),
                eq(2L),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.error(new MissingPermissionError(
                Permission.builder()
                        .holder(Holder.user(HolderId.of("USER_ID")))
                        .isAllowedTo(Actions.UNPUBLISH)
                        .on(Resource.of(ResourceType.of("HIGHLIGHT"), ResourceId.of("HIGHLIGHT_ID")))
        )));

        var exchange = client.post()
                .uri("/highlights/HIGHLIGHT_ID/unpublish")
                .headers(headers -> headers.setBearerAuth(token))
                .bodyValue(request)
                .exchange();

        exchange.expectStatus().isForbidden();
    }

    @Test
    void shouldRespondWith409WhenVersionMismatch() {
        var token = createTokenForUser("USER_ID");

        var request = new UnpublishHighlightRequest();
        request.version = 2L;

        when(module.unpublishHighlight(
                eq("HIGHLIGHT_ID"),
                eq(2L),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.error(new AggregateVersionOutdatedError(
                AggregateType.of("HIGHLIGHT"),
                AggregateId.of("HIGHLIGHT_ID"),
                Version.of(3L)
        )));

        var exchange = client.post()
                .uri("/highlights/HIGHLIGHT_ID/unpublish")
                .headers(headers -> headers.setBearerAuth(token))
                .bodyValue(request)
                .exchange();

        exchange.expectStatus().isEqualTo(409);
    }

    @Test
    void shouldRespondWith409WhenAlreadyUnpublished() {
        var token = createTokenForUser("USER_ID");

        var request = new UnpublishHighlightRequest();
        request.version = 2L;

        when(module.unpublishHighlight(
                eq("HIGHLIGHT_ID"),
                eq(2L),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.error(new AlreadyUnpublishedError()));

        var exchange = client.post()
                .uri("/highlights/HIGHLIGHT_ID/unpublish")
                .headers(headers -> headers.setBearerAuth(token))
                .bodyValue(request)
                .exchange();

        exchange.expectStatus().isEqualTo(409);
    }

}
