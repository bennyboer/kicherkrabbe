package de.bennyboer.kicherkrabbe.offers.http;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.offers.Actions;
import de.bennyboer.kicherkrabbe.offers.api.requests.UpdateImagesRequest;
import de.bennyboer.kicherkrabbe.permissions.*;
import de.bennyboer.kicherkrabbe.offers.api.responses.UpdateImagesResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class UpdateImagesHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyUpdateImages() {
        var request = new UpdateImagesRequest();
        request.version = 0L;
        request.imageIds = List.of("IMAGE_1", "IMAGE_2");

        var token = createTokenForUser("USER_ID");

        when(module.updateOfferImages(
                eq("OFFER_ID"),
                any(Long.class),
                any(),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.just(1L));

        var exchange = client.post()
                .uri("/offers/OFFER_ID/images/update")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        exchange.expectStatus().isOk();
        var response = exchange.expectBody(UpdateImagesResponse.class)
                .returnResult()
                .getResponseBody();
        assertThat(response.version).isEqualTo(1L);
    }

    @Test
    void shouldRespondWithConflictOnOutdatedVersion() {
        var request = new UpdateImagesRequest();
        request.version = 0L;
        request.imageIds = List.of("IMAGE_1", "IMAGE_2");

        var token = createTokenForUser("USER_ID");

        when(module.updateOfferImages(
                eq("OFFER_ID"),
                any(Long.class),
                any(),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.error(new AggregateVersionOutdatedError(
                AggregateType.of("OFFER"),
                AggregateId.of("OFFER_ID"),
                Version.of(0)
        )));

        var exchange = client.post()
                .uri("/offers/OFFER_ID/images/update")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        exchange.expectStatus().isEqualTo(409);
    }

    @Test
    void shouldNotAllowUnauthorizedAccess() {
        var request = new UpdateImagesRequest();
        request.version = 0L;
        request.imageIds = List.of("IMAGE_1", "IMAGE_2");

        var exchange = client.post()
                .uri("/offers/OFFER_ID/images/update")
                .bodyValue(request)
                .exchange();

        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithForbiddenForMissingPermission() {
        var request = new UpdateImagesRequest();
        request.version = 0L;
        request.imageIds = List.of("IMAGE_1", "IMAGE_2");

        var token = createTokenForUser("USER_ID");

        when(module.updateOfferImages(
                eq("OFFER_ID"),
                any(Long.class),
                any(),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.error(new MissingPermissionError(
                Permission.builder()
                        .holder(Holder.user(HolderId.of("USER_ID")))
                        .isAllowedTo(Actions.UPDATE_IMAGES)
                        .on(Resource.of(ResourceType.of("OFFER"), ResourceId.of("OFFER_ID")))
        )));

        var exchange = client.post()
                .uri("/offers/OFFER_ID/images/update")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        exchange.expectStatus().isForbidden();
    }

}
