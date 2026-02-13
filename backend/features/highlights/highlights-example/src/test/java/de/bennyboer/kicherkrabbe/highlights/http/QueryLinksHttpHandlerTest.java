package de.bennyboer.kicherkrabbe.highlights.http;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.highlights.Actions;
import de.bennyboer.kicherkrabbe.highlights.api.LinkDTO;
import de.bennyboer.kicherkrabbe.highlights.api.LinkTypeDTO;
import de.bennyboer.kicherkrabbe.highlights.api.responses.QueryLinksResponse;
import de.bennyboer.kicherkrabbe.permissions.*;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class QueryLinksHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyQueryLinks() {
        var token = createTokenForUser("USER_ID");

        var link1 = new LinkDTO();
        link1.type = LinkTypeDTO.PATTERN;
        link1.id = "PATTERN_ID";
        link1.name = "Pattern 1";

        var link2 = new LinkDTO();
        link2.type = LinkTypeDTO.FABRIC;
        link2.id = "FABRIC_ID";
        link2.name = "Fabric 1";

        var response = new QueryLinksResponse();
        response.total = 50;
        response.links = List.of(link1, link2);

        when(module.getLinks(
                eq("Search"),
                eq(10L),
                eq(20L),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.just(response));

        var exchange = client.get()
                .uri(builder -> builder.path("/highlights/links")
                        .queryParam("searchTerm", "Search")
                        .queryParam("skip", 10)
                        .queryParam("limit", 20)
                        .build())
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        exchange.expectStatus().isOk();

        var result = exchange.expectBody(QueryLinksResponse.class).returnResult().getResponseBody();
        assertThat(result.total).isEqualTo(50);
        assertThat(result.links).hasSize(2);
        assertThat(result.links.get(0).type).isEqualTo(LinkTypeDTO.PATTERN);
        assertThat(result.links.get(0).id).isEqualTo("PATTERN_ID");
        assertThat(result.links.get(0).name).isEqualTo("Pattern 1");
        assertThat(result.links.get(1).type).isEqualTo(LinkTypeDTO.FABRIC);
        assertThat(result.links.get(1).id).isEqualTo("FABRIC_ID");
        assertThat(result.links.get(1).name).isEqualTo("Fabric 1");
    }

    @Test
    void shouldRespondWithUnauthorizedForAnonymousAccess() {
        var exchange = client.get()
                .uri("/highlights/links")
                .exchange();

        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithUnauthorizedForInvalidToken() {
        var exchange = client.get()
                .uri("/highlights/links")
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithForbiddenForMissingPermission() {
        var token = createTokenForUser("USER_ID");

        when(module.getLinks(
                eq(""),
                eq(0L),
                eq((long) Integer.MAX_VALUE),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.error(new MissingPermissionError(
                Permission.builder()
                        .holder(Holder.user(HolderId.of("USER_ID")))
                        .isAllowedTo(Actions.READ)
                        .onType(ResourceType.of("HIGHLIGHT_LINK"))
        )));

        var exchange = client.get()
                .uri("/highlights/links")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        exchange.expectStatus().isForbidden();
    }

}
