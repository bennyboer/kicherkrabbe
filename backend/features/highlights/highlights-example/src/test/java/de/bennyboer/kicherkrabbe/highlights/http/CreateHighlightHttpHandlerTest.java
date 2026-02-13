package de.bennyboer.kicherkrabbe.highlights.http;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.highlights.Actions;
import de.bennyboer.kicherkrabbe.highlights.api.requests.CreateHighlightRequest;
import de.bennyboer.kicherkrabbe.highlights.api.responses.CreateHighlightResponse;
import de.bennyboer.kicherkrabbe.permissions.*;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class CreateHighlightHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyCreateHighlight() {
        var token = createTokenForUser("USER_ID");

        var request = new CreateHighlightRequest();
        request.imageId = "IMAGE_ID";
        request.sortOrder = 1;

        when(module.createHighlight(
                eq("IMAGE_ID"),
                eq(1L),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.just("HIGHLIGHT_ID"));

        var exchange = client.post()
                .uri("/highlights/create")
                .headers(headers -> headers.setBearerAuth(token))
                .bodyValue(request)
                .exchange();

        exchange.expectStatus().isOk();

        var result = exchange.expectBody(CreateHighlightResponse.class).returnResult().getResponseBody();
        assertThat(result.id).isEqualTo("HIGHLIGHT_ID");
    }

    @Test
    void shouldRespondWithUnauthorizedForAnonymousAccess() {
        var exchange = client.post()
                .uri("/highlights/create")
                .exchange();

        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithUnauthorizedForInvalidToken() {
        var exchange = client.post()
                .uri("/highlights/create")
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithForbiddenForMissingPermission() {
        var token = createTokenForUser("USER_ID");

        var request = new CreateHighlightRequest();
        request.imageId = "IMAGE_ID";
        request.sortOrder = 1;

        when(module.createHighlight(
                eq("IMAGE_ID"),
                eq(1L),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.error(new MissingPermissionError(
                Permission.builder()
                        .holder(Holder.user(HolderId.of("USER_ID")))
                        .isAllowedTo(Actions.CREATE)
                        .onType(ResourceType.of("HIGHLIGHT"))
        )));

        var exchange = client.post()
                .uri("/highlights/create")
                .headers(headers -> headers.setBearerAuth(token))
                .bodyValue(request)
                .exchange();

        exchange.expectStatus().isForbidden();
    }

}
