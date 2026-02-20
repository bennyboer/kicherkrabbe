package de.bennyboer.kicherkrabbe.offers.http;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.offers.Actions;
import de.bennyboer.kicherkrabbe.offers.api.responses.ReserveOfferResponse;
import de.bennyboer.kicherkrabbe.permissions.*;
import de.bennyboer.kicherkrabbe.offers.reserve.AlreadyReservedError;
import de.bennyboer.kicherkrabbe.offers.reserve.NotPublishedError;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class ReserveOfferHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyReserveOffer() {
        var token = createTokenForUser("USER_ID");

        when(module.reserveOffer(
                "OFFER_ID",
                0L,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.just(1L));

        var exchange = client.post()
                .uri("/offers/OFFER_ID/reserve?version=0")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        exchange.expectStatus().isOk();
        var response = exchange.expectBody(ReserveOfferResponse.class)
                .returnResult()
                .getResponseBody();
        assertThat(response.version).isEqualTo(1L);
    }

    @Test
    void shouldRespondWithConflictOnOutdatedVersion() {
        var token = createTokenForUser("USER_ID");

        when(module.reserveOffer(
                "OFFER_ID",
                0L,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.error(new AggregateVersionOutdatedError(
                AggregateType.of("OFFER"),
                AggregateId.of("OFFER_ID"),
                Version.of(0)
        )));

        var exchange = client.post()
                .uri("/offers/OFFER_ID/reserve?version=0")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        exchange.expectStatus().isEqualTo(409);
    }

    @Test
    void shouldRespondWithPreconditionFailedOnNotPublished() {
        var token = createTokenForUser("USER_ID");

        when(module.reserveOffer(
                "OFFER_ID",
                1L,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.error(new NotPublishedError()));

        var exchange = client.post()
                .uri("/offers/OFFER_ID/reserve?version=1")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        exchange.expectStatus().isEqualTo(412);
    }

    @Test
    void shouldRespondWithPreconditionFailedOnAlreadyReserved() {
        var token = createTokenForUser("USER_ID");

        when(module.reserveOffer(
                "OFFER_ID",
                1L,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.error(new AlreadyReservedError()));

        var exchange = client.post()
                .uri("/offers/OFFER_ID/reserve?version=1")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        exchange.expectStatus().isEqualTo(412);
    }

    @Test
    void shouldNotAllowUnauthorizedAccess() {
        var exchange = client.post()
                .uri("/offers/OFFER_ID/reserve?version=0")
                .exchange();

        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithForbiddenForMissingPermission() {
        var token = createTokenForUser("USER_ID");

        when(module.reserveOffer(
                "OFFER_ID",
                0L,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.error(new MissingPermissionError(
                Permission.builder()
                        .holder(Holder.user(HolderId.of("USER_ID")))
                        .isAllowedTo(Actions.RESERVE)
                        .on(Resource.of(ResourceType.of("OFFER"), ResourceId.of("OFFER_ID")))
        )));

        var exchange = client.post()
                .uri("/offers/OFFER_ID/reserve?version=0")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        exchange.expectStatus().isForbidden();
    }
}
