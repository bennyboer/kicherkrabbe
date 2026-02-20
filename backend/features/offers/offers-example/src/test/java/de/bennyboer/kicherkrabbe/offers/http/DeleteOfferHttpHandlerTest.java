package de.bennyboer.kicherkrabbe.offers.http;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.offers.Actions;
import de.bennyboer.kicherkrabbe.offers.delete.CannotDeleteNonDraftError;
import de.bennyboer.kicherkrabbe.permissions.*;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.when;

public class DeleteOfferHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyDeleteOffer() {
        var token = createTokenForUser("USER_ID");

        when(module.deleteOffer(
                "OFFER_ID",
                0L,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.empty());

        var exchange = client.delete()
                .uri("/offers/OFFER_ID/?version=0")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        exchange.expectStatus().isOk();
    }

    @Test
    void shouldRespondWithConflictOnOutdatedVersion() {
        var token = createTokenForUser("USER_ID");

        when(module.deleteOffer(
                "OFFER_ID",
                0L,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.error(new AggregateVersionOutdatedError(
                AggregateType.of("OFFER"),
                AggregateId.of("OFFER_ID"),
                Version.of(0)
        )));

        var exchange = client.delete()
                .uri("/offers/OFFER_ID/?version=0")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        exchange.expectStatus().isEqualTo(409);
    }

    @Test
    void shouldRespondWithConflictOnCannotDeleteNonDraft() {
        var token = createTokenForUser("USER_ID");

        when(module.deleteOffer(
                "OFFER_ID",
                1L,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.error(new CannotDeleteNonDraftError()));

        var exchange = client.delete()
                .uri("/offers/OFFER_ID/?version=1")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        exchange.expectStatus().isEqualTo(409);
    }

    @Test
    void shouldNotAllowUnauthorizedAccess() {
        var exchange = client.delete()
                .uri("/offers/OFFER_ID/?version=0")
                .exchange();

        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithForbiddenForMissingPermission() {
        var token = createTokenForUser("USER_ID");

        when(module.deleteOffer(
                "OFFER_ID",
                0L,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.error(new MissingPermissionError(
                Permission.builder()
                        .holder(Holder.user(HolderId.of("USER_ID")))
                        .isAllowedTo(Actions.DELETE)
                        .on(Resource.of(ResourceType.of("OFFER"), ResourceId.of("OFFER_ID")))
        )));

        var exchange = client.delete()
                .uri("/offers/OFFER_ID/?version=0")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        exchange.expectStatus().isForbidden();
    }
}
