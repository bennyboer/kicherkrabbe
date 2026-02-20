package de.bennyboer.kicherkrabbe.offers.http;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.offers.Actions;
import de.bennyboer.kicherkrabbe.offers.api.responses.UnreserveOfferResponse;
import de.bennyboer.kicherkrabbe.permissions.*;
import de.bennyboer.kicherkrabbe.offers.unreserve.NotReservedError;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class UnreserveOfferHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyUnreserveOffer() {
        var token = createTokenForUser("USER_ID");

        when(module.unreserveOffer(
                "OFFER_ID",
                0L,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.just(1L));

        var exchange = client.post()
                .uri("/offers/OFFER_ID/unreserve?version=0")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        exchange.expectStatus().isOk();
        var response = exchange.expectBody(UnreserveOfferResponse.class)
                .returnResult()
                .getResponseBody();
        assertThat(response.version).isEqualTo(1L);
    }

    @Test
    void shouldRespondWithConflictOnOutdatedVersion() {
        var token = createTokenForUser("USER_ID");

        when(module.unreserveOffer(
                "OFFER_ID",
                0L,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.error(new AggregateVersionOutdatedError(
                AggregateType.of("OFFER"),
                AggregateId.of("OFFER_ID"),
                Version.of(0)
        )));

        var exchange = client.post()
                .uri("/offers/OFFER_ID/unreserve?version=0")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        exchange.expectStatus().isEqualTo(409);
    }

    @Test
    void shouldRespondWithPreconditionFailedOnNotReserved() {
        var token = createTokenForUser("USER_ID");

        when(module.unreserveOffer(
                "OFFER_ID",
                1L,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.error(new NotReservedError()));

        var exchange = client.post()
                .uri("/offers/OFFER_ID/unreserve?version=1")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        exchange.expectStatus().isEqualTo(412);
    }

    @Test
    void shouldNotAllowUnauthorizedAccess() {
        var exchange = client.post()
                .uri("/offers/OFFER_ID/unreserve?version=0")
                .exchange();

        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithForbiddenForMissingPermission() {
        var token = createTokenForUser("USER_ID");

        when(module.unreserveOffer(
                "OFFER_ID",
                0L,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.error(new MissingPermissionError(
                Permission.builder()
                        .holder(Holder.user(HolderId.of("USER_ID")))
                        .isAllowedTo(Actions.UNRESERVE)
                        .on(Resource.of(ResourceType.of("OFFER"), ResourceId.of("OFFER_ID")))
        )));

        var exchange = client.post()
                .uri("/offers/OFFER_ID/unreserve?version=0")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        exchange.expectStatus().isForbidden();
    }
}
