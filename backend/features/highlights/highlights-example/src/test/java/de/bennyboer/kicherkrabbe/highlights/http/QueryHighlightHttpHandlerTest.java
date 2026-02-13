package de.bennyboer.kicherkrabbe.highlights.http;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.highlights.Actions;
import de.bennyboer.kicherkrabbe.highlights.api.HighlightDTO;
import de.bennyboer.kicherkrabbe.highlights.samples.SampleHighlightDetails;
import de.bennyboer.kicherkrabbe.permissions.*;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class QueryHighlightHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyQueryHighlight() {
        var token = createTokenForUser("USER_ID");

        var highlight = SampleHighlightDetails.builder()
                .id("HIGHLIGHT_ID")
                .version(2)
                .imageId("IMAGE_ID")
                .published(true)
                .sortOrder(1)
                .createdAt(Instant.parse("2024-12-01T12:00:00.000Z"))
                .build()
                .toValue();

        when(module.getHighlight(
                eq("HIGHLIGHT_ID"),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.just(highlight));

        var exchange = client.get()
                .uri("/highlights/{highlightId}", "HIGHLIGHT_ID")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        exchange.expectStatus().isOk();

        var result = exchange.expectBody(HighlightDTO.class).returnResult().getResponseBody();
        assertThat(result.id).isEqualTo("HIGHLIGHT_ID");
        assertThat(result.version).isEqualTo(2);
        assertThat(result.imageId).isEqualTo("IMAGE_ID");
        assertThat(result.published).isTrue();
        assertThat(result.sortOrder).isEqualTo(1);
    }

    @Test
    void shouldRespondWith404WhenHighlightNotFound() {
        var token = createTokenForUser("USER_ID");

        when(module.getHighlight(
                eq("HIGHLIGHT_ID"),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.empty());

        var exchange = client.get()
                .uri("/highlights/{highlightId}", "HIGHLIGHT_ID")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        exchange.expectStatus().isNotFound();
    }

    @Test
    void shouldRespondWithUnauthorizedForAnonymousAccess() {
        var exchange = client.get()
                .uri("/highlights/HIGHLIGHT_ID")
                .exchange();

        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithUnauthorizedForInvalidToken() {
        var exchange = client.get()
                .uri("/highlights/HIGHLIGHT_ID")
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithForbiddenForMissingPermission() {
        var token = createTokenForUser("USER_ID");

        when(module.getHighlight(
                eq("HIGHLIGHT_ID"),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.error(new MissingPermissionError(
                Permission.builder()
                        .holder(Holder.user(HolderId.of("USER_ID")))
                        .isAllowedTo(Actions.READ)
                        .on(Resource.of(ResourceType.of("HIGHLIGHT"), ResourceId.of("HIGHLIGHT_ID")))
        )));

        var exchange = client.get()
                .uri("/highlights/HIGHLIGHT_ID")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        exchange.expectStatus().isForbidden();
    }

}
