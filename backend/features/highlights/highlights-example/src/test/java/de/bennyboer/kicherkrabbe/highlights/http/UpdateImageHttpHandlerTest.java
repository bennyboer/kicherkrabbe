package de.bennyboer.kicherkrabbe.highlights.http;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.highlights.Actions;
import de.bennyboer.kicherkrabbe.highlights.api.requests.UpdateHighlightImageRequest;
import de.bennyboer.kicherkrabbe.highlights.api.responses.UpdateHighlightImageResponse;
import de.bennyboer.kicherkrabbe.permissions.*;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class UpdateImageHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyUpdateImage() {
        var token = createTokenForUser("USER_ID");

        var request = new UpdateHighlightImageRequest();
        request.version = 2L;
        request.imageId = "NEW_IMAGE_ID";

        when(module.updateImage(
                eq("HIGHLIGHT_ID"),
                eq(2L),
                eq("NEW_IMAGE_ID"),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.just(3L));

        var exchange = client.post()
                .uri("/highlights/{highlightId}/update/image", "HIGHLIGHT_ID")
                .headers(headers -> headers.setBearerAuth(token))
                .bodyValue(request)
                .exchange();

        exchange.expectStatus().isOk();

        var result = exchange.expectBody(UpdateHighlightImageResponse.class).returnResult().getResponseBody();
        assertThat(result.version).isEqualTo(3L);
    }

    @Test
    void shouldRespondWithUnauthorizedForAnonymousAccess() {
        var exchange = client.post()
                .uri("/highlights/HIGHLIGHT_ID/update/image")
                .exchange();

        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithUnauthorizedForInvalidToken() {
        var exchange = client.post()
                .uri("/highlights/HIGHLIGHT_ID/update/image")
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithForbiddenForMissingPermission() {
        var token = createTokenForUser("USER_ID");

        var request = new UpdateHighlightImageRequest();
        request.version = 2L;
        request.imageId = "NEW_IMAGE_ID";

        when(module.updateImage(
                eq("HIGHLIGHT_ID"),
                eq(2L),
                eq("NEW_IMAGE_ID"),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.error(new MissingPermissionError(
                Permission.builder()
                        .holder(Holder.user(HolderId.of("USER_ID")))
                        .isAllowedTo(Actions.UPDATE_IMAGE)
                        .on(Resource.of(ResourceType.of("HIGHLIGHT"), ResourceId.of("HIGHLIGHT_ID")))
        )));

        var exchange = client.post()
                .uri("/highlights/HIGHLIGHT_ID/update/image")
                .headers(headers -> headers.setBearerAuth(token))
                .bodyValue(request)
                .exchange();

        exchange.expectStatus().isForbidden();
    }

    @Test
    void shouldRespondWith409WhenVersionMismatch() {
        var token = createTokenForUser("USER_ID");

        var request = new UpdateHighlightImageRequest();
        request.version = 2L;
        request.imageId = "NEW_IMAGE_ID";

        when(module.updateImage(
                eq("HIGHLIGHT_ID"),
                eq(2L),
                eq("NEW_IMAGE_ID"),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.error(new AggregateVersionOutdatedError(
                AggregateType.of("HIGHLIGHT"),
                AggregateId.of("HIGHLIGHT_ID"),
                Version.of(3L)
        )));

        var exchange = client.post()
                .uri("/highlights/HIGHLIGHT_ID/update/image")
                .headers(headers -> headers.setBearerAuth(token))
                .bodyValue(request)
                .exchange();

        exchange.expectStatus().isEqualTo(409);
    }

}
