package de.bennyboer.kicherkrabbe.highlights.http;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.highlights.Actions;
import de.bennyboer.kicherkrabbe.highlights.HighlightsPage;
import de.bennyboer.kicherkrabbe.highlights.LinkType;
import de.bennyboer.kicherkrabbe.highlights.api.HighlightDTO;
import de.bennyboer.kicherkrabbe.highlights.api.LinkDTO;
import de.bennyboer.kicherkrabbe.highlights.api.LinkTypeDTO;
import de.bennyboer.kicherkrabbe.highlights.api.responses.QueryHighlightsResponse;
import de.bennyboer.kicherkrabbe.highlights.samples.SampleHighlightDetails;
import de.bennyboer.kicherkrabbe.highlights.samples.SampleLink;
import de.bennyboer.kicherkrabbe.permissions.*;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class QueryHighlightsHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyQueryHighlights() {
        var token = createTokenForUser("USER_ID");

        var highlight1 = SampleHighlightDetails.builder()
                .id("HIGHLIGHT_ID_1")
                .version(2)
                .imageId("IMAGE_ID_1")
                .link(SampleLink.builder()
                        .type(LinkType.PATTERN)
                        .id("PATTERN_ID")
                        .name("Pattern 1")
                        .build())
                .published(true)
                .sortOrder(1)
                .createdAt(Instant.parse("2024-12-01T12:00:00.000Z"))
                .build()
                .toValue();

        var highlight2 = SampleHighlightDetails.builder()
                .id("HIGHLIGHT_ID_2")
                .version(1)
                .imageId("IMAGE_ID_2")
                .published(false)
                .sortOrder(2)
                .createdAt(Instant.parse("2024-12-02T12:00:00.000Z"))
                .build()
                .toValue();

        var page = HighlightsPage.of(100, 50, 152, List.of(highlight1, highlight2));

        when(module.getHighlights(
                eq(100L),
                eq(50L),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.just(page));

        var exchange = client.get()
                .uri(builder -> builder.path("/highlights")
                        .queryParam("skip", 100)
                        .queryParam("limit", 50)
                        .build())
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        exchange.expectStatus().isOk();

        var result = exchange.expectBody(QueryHighlightsResponse.class).returnResult().getResponseBody();
        assertThat(result.skip).isEqualTo(100);
        assertThat(result.limit).isEqualTo(50);
        assertThat(result.total).isEqualTo(152);
        assertThat(result.highlights).hasSize(2);

        HighlightDTO h1 = result.highlights.get(0);
        assertThat(h1.id).isEqualTo("HIGHLIGHT_ID_1");
        assertThat(h1.version).isEqualTo(2);
        assertThat(h1.imageId).isEqualTo("IMAGE_ID_1");
        assertThat(h1.published).isTrue();
        assertThat(h1.sortOrder).isEqualTo(1);
        assertThat(h1.links).hasSize(1);

        LinkDTO l1 = h1.links.get(0);
        assertThat(l1.type).isEqualTo(LinkTypeDTO.PATTERN);
        assertThat(l1.id).isEqualTo("PATTERN_ID");
        assertThat(l1.name).isEqualTo("Pattern 1");

        HighlightDTO h2 = result.highlights.get(1);
        assertThat(h2.id).isEqualTo("HIGHLIGHT_ID_2");
        assertThat(h2.version).isEqualTo(1);
        assertThat(h2.imageId).isEqualTo("IMAGE_ID_2");
        assertThat(h2.published).isFalse();
        assertThat(h2.sortOrder).isEqualTo(2);
        assertThat(h2.links).isEmpty();
    }

    @Test
    void shouldRespondWithUnauthorizedForAnonymousAccess() {
        var exchange = client.get()
                .uri("/highlights")
                .exchange();

        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithUnauthorizedForInvalidToken() {
        var exchange = client.get()
                .uri("/highlights")
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithForbiddenForMissingPermission() {
        var token = createTokenForUser("USER_ID");

        when(module.getHighlights(
                eq(0L),
                eq((long) Integer.MAX_VALUE),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.error(new MissingPermissionError(
                Permission.builder()
                        .holder(Holder.user(HolderId.of("USER_ID")))
                        .isAllowedTo(Actions.READ)
                        .onType(ResourceType.of("HIGHLIGHT"))
        )));

        var exchange = client.get()
                .uri("/highlights")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        exchange.expectStatus().isForbidden();
    }

}
