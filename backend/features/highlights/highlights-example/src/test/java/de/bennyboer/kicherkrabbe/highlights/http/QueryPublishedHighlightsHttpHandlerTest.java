package de.bennyboer.kicherkrabbe.highlights.http;

import de.bennyboer.kicherkrabbe.highlights.LinkType;
import de.bennyboer.kicherkrabbe.highlights.api.LinkDTO;
import de.bennyboer.kicherkrabbe.highlights.api.LinkTypeDTO;
import de.bennyboer.kicherkrabbe.highlights.api.PublishedHighlightDTO;
import de.bennyboer.kicherkrabbe.highlights.api.responses.QueryPublishedHighlightsResponse;
import de.bennyboer.kicherkrabbe.highlights.samples.SampleHighlightDetails;
import de.bennyboer.kicherkrabbe.highlights.samples.SampleLink;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class QueryPublishedHighlightsHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyQueryPublishedHighlights() {
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
                .published(true)
                .sortOrder(2)
                .createdAt(Instant.parse("2024-12-02T12:00:00.000Z"))
                .build()
                .toValue();

        when(module.getPublishedHighlights()).thenReturn(Flux.just(highlight1, highlight2));

        var exchange = client.get()
                .uri("/highlights/published")
                .exchange();

        exchange.expectStatus().isOk();

        var result = exchange.expectBody(QueryPublishedHighlightsResponse.class).returnResult().getResponseBody();
        assertThat(result.highlights).hasSize(2);

        PublishedHighlightDTO h1 = result.highlights.get(0);
        assertThat(h1.id).isEqualTo("HIGHLIGHT_ID_1");
        assertThat(h1.imageId).isEqualTo("IMAGE_ID_1");
        assertThat(h1.links).hasSize(1);

        LinkDTO l1 = h1.links.get(0);
        assertThat(l1.type).isEqualTo(LinkTypeDTO.PATTERN);
        assertThat(l1.id).isEqualTo("PATTERN_ID");
        assertThat(l1.name).isEqualTo("Pattern 1");

        PublishedHighlightDTO h2 = result.highlights.get(1);
        assertThat(h2.id).isEqualTo("HIGHLIGHT_ID_2");
        assertThat(h2.imageId).isEqualTo("IMAGE_ID_2");
        assertThat(h2.links).isEmpty();
    }

    @Test
    void shouldAllowAnonymousAccessToPublishedHighlights() {
        when(module.getPublishedHighlights()).thenReturn(Flux.empty());

        var exchange = client.get()
                .uri("/highlights/published")
                .exchange();

        exchange.expectStatus().isOk();

        var result = exchange.expectBody(QueryPublishedHighlightsResponse.class).returnResult().getResponseBody();
        assertThat(result.highlights).isEmpty();
    }

}
