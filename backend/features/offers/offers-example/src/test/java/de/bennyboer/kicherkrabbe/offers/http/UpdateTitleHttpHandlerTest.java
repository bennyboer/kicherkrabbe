package de.bennyboer.kicherkrabbe.offers.http;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.offers.Actions;
import de.bennyboer.kicherkrabbe.offers.AliasAlreadyInUseError;
import de.bennyboer.kicherkrabbe.offers.OfferAlias;
import de.bennyboer.kicherkrabbe.offers.OfferId;
import de.bennyboer.kicherkrabbe.permissions.*;
import de.bennyboer.kicherkrabbe.offers.api.requests.UpdateTitleRequest;
import de.bennyboer.kicherkrabbe.offers.api.responses.UpdateTitleResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class UpdateTitleHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyUpdateTitle() {
        var request = new UpdateTitleRequest();
        request.version = 0L;
        request.title = "New Title";

        var token = createTokenForUser("USER_ID");

        when(module.updateOfferTitle(
                eq("OFFER_ID"),
                any(Long.class),
                eq("New Title"),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.just(1L));

        var exchange = client.post()
                .uri("/offers/OFFER_ID/title/update")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        exchange.expectStatus().isOk();
        var response = exchange.expectBody(UpdateTitleResponse.class)
                .returnResult()
                .getResponseBody();
        assertThat(response.version).isEqualTo(1L);
    }

    @Test
    void shouldRespondWithConflictOnOutdatedVersion() {
        var request = new UpdateTitleRequest();
        request.version = 0L;
        request.title = "New Title";

        var token = createTokenForUser("USER_ID");

        when(module.updateOfferTitle(
                eq("OFFER_ID"),
                any(Long.class),
                eq("New Title"),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.error(new AggregateVersionOutdatedError(
                AggregateType.of("OFFER"),
                AggregateId.of("OFFER_ID"),
                Version.of(0)
        )));

        var exchange = client.post()
                .uri("/offers/OFFER_ID/title/update")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        exchange.expectStatus().isEqualTo(409);
    }

    @Test
    void shouldNotAllowUnauthorizedAccess() {
        var request = new UpdateTitleRequest();
        request.version = 0L;
        request.title = "New Title";

        var exchange = client.post()
                .uri("/offers/OFFER_ID/title/update")
                .bodyValue(request)
                .exchange();

        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithPreconditionFailedOnAliasConflict() {
        var request = new UpdateTitleRequest();
        request.version = 0L;
        request.title = "Conflicting Title";

        var token = createTokenForUser("USER_ID");

        when(module.updateOfferTitle(
                eq("OFFER_ID"),
                any(Long.class),
                eq("Conflicting Title"),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.error(new AliasAlreadyInUseError(
                OfferId.of("CONFLICTING_OFFER_ID"),
                OfferAlias.of("conflicting-title")
        )));

        var exchange = client.post()
                .uri("/offers/OFFER_ID/title/update")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        exchange.expectStatus().isEqualTo(412);
        var body = exchange.expectBody(Map.class)
                .returnResult()
                .getResponseBody();
        assertThat(body.get("reason")).isEqualTo("ALIAS_ALREADY_IN_USE");
        assertThat(body.get("offerId")).isEqualTo("CONFLICTING_OFFER_ID");
        assertThat(body.get("alias")).isEqualTo("conflicting-title");
    }

    @Test
    void shouldRespondWithForbiddenForMissingPermission() {
        var request = new UpdateTitleRequest();
        request.version = 0L;
        request.title = "New Title";

        var token = createTokenForUser("USER_ID");

        when(module.updateOfferTitle(
                eq("OFFER_ID"),
                any(Long.class),
                eq("New Title"),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.error(new MissingPermissionError(
                Permission.builder()
                        .holder(Holder.user(HolderId.of("USER_ID")))
                        .isAllowedTo(Actions.UPDATE_TITLE)
                        .on(Resource.of(ResourceType.of("OFFER"), ResourceId.of("OFFER_ID")))
        )));

        var exchange = client.post()
                .uri("/offers/OFFER_ID/title/update")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        exchange.expectStatus().isForbidden();
    }

}
