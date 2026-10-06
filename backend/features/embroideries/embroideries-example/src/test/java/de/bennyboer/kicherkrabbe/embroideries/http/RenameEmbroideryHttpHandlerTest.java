package de.bennyboer.kicherkrabbe.embroideries.http;

import de.bennyboer.kicherkrabbe.embroideries.AliasAlreadyInUseError;
import de.bennyboer.kicherkrabbe.embroideries.EmbroideryAlias;
import de.bennyboer.kicherkrabbe.embroideries.EmbroideryId;
import de.bennyboer.kicherkrabbe.embroideries.http.api.requests.RenameEmbroideryRequest;
import de.bennyboer.kicherkrabbe.embroideries.http.api.responses.RenameEmbroideryResponse;
import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class RenameEmbroideryHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyRenameEmbroidery() {
        // given: having a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return the new version
        when(module.renameEmbroidery("EMBROIDERY_ID", 0L, "Happy Crab", Agent.user(AgentId.of("USER_ID"))))
                .thenReturn(Mono.just(1L));

        // when: posting the request
        var exchange = postRequest("Happy Crab", token);

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the new version
        exchange.expectBody(RenameEmbroideryResponse.class)
                .value(response -> assertThat(response.version).isEqualTo(1L));
    }

    @Test
    void shouldRespondWith409OnAggregateVersionOutdatedError() {
        // given: having a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to raise a version outdated error
        when(module.renameEmbroidery("EMBROIDERY_ID", 0L, "Happy Crab", Agent.user(AgentId.of("USER_ID"))))
                .thenReturn(Mono.error(new AggregateVersionOutdatedError(
                        AggregateType.of("EMBROIDERY"),
                        AggregateId.of("EMBROIDERY_ID"),
                        Version.zero()
                )));

        // when: posting the request
        var exchange = postRequest("Happy Crab", token);

        // then: the response is conflict
        exchange.expectStatus().isEqualTo(409);
    }

    @Test
    void shouldRespondWithBadRequestOnInvalidName() {
        // given: having a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to raise an illegal argument exception
        when(module.renameEmbroidery("EMBROIDERY_ID", 0L, "", Agent.user(AgentId.of("USER_ID"))))
                .thenReturn(Mono.error(new IllegalArgumentException("Name must not be blank")));

        // when: posting the request
        var exchange = postRequest("", token);

        // then: the response is bad request
        exchange.expectStatus().isBadRequest();
    }

    @Test
    void shouldRespondWithPreconditionFailedAndReasonOnAliasAlreadyInUse() {
        // given: having a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to raise an alias already in use error
        when(module.renameEmbroidery("EMBROIDERY_ID", 0L, "Happy Crab", Agent.user(AgentId.of("USER_ID"))))
                .thenReturn(Mono.error(new AliasAlreadyInUseError(
                        EmbroideryId.of("OTHER_EMBROIDERY_ID"),
                        EmbroideryAlias.of("happy-crab")
                )));

        // when: posting the request
        var exchange = postRequest("Happy Crab", token);

        // then: the response is precondition failed with the reason
        exchange.expectStatus().isEqualTo(412);
        exchange.expectBody(Map.class).isEqualTo(Map.of(
                "reason", "ALIAS_ALREADY_IN_USE",
                "embroideryId", "OTHER_EMBROIDERY_ID",
                "alias", "happy-crab"
        ));
    }

    @Test
    void shouldNotAllowUnauthorizedAccess() {
        // given: a request to rename an embroidery
        var request = new RenameEmbroideryRequest();
        request.version = 0L;
        request.name = "Happy Crab";

        // when: posting the request without a token
        var exchange = client.post()
                .uri("/embroideries/EMBROIDERY_ID/rename")
                .bodyValue(request)
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    private WebTestClient.ResponseSpec postRequest(String name, String token) {
        var request = new RenameEmbroideryRequest();
        request.version = 0L;
        request.name = name;

        return client.post()
                .uri("/embroideries/EMBROIDERY_ID/rename")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();
    }

}
