package de.bennyboer.kicherkrabbe.embroideries.http;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.when;

public class DeleteEmbroideryHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyDeleteEmbroidery() {
        // given: having a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to delete the embroidery
        when(module.deleteEmbroidery("EMBROIDERY_ID", 3L, Agent.user(AgentId.of("USER_ID"))))
                .thenReturn(Mono.empty());

        // when: sending the delete request
        var exchange = client.delete()
                .uri("/embroideries/EMBROIDERY_ID?version=3")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();
    }

    @Test
    void shouldRespondWith409OnAggregateVersionOutdatedError() {
        // given: having a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to raise a version outdated error
        when(module.deleteEmbroidery("EMBROIDERY_ID", 0L, Agent.user(AgentId.of("USER_ID"))))
                .thenReturn(Mono.error(new AggregateVersionOutdatedError(
                        AggregateType.of("EMBROIDERY"),
                        AggregateId.of("EMBROIDERY_ID"),
                        Version.zero()
                )));

        // when: sending the delete request
        var exchange = client.delete()
                .uri("/embroideries/EMBROIDERY_ID?version=0")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is conflict
        exchange.expectStatus().isEqualTo(409);
    }

    @Test
    void shouldNotAllowUnauthorizedAccess() {
        // when: sending the delete request without a token
        var exchange = client.delete()
                .uri("/embroideries/EMBROIDERY_ID?version=0")
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

}
