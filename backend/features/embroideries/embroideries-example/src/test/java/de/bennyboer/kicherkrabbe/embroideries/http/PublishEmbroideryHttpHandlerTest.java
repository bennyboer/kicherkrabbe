package de.bennyboer.kicherkrabbe.embroideries.http;

import de.bennyboer.kicherkrabbe.embroideries.http.api.responses.PublishEmbroideryResponse;
import de.bennyboer.kicherkrabbe.embroideries.http.api.responses.UnpublishEmbroideryResponse;
import de.bennyboer.kicherkrabbe.embroideries.publish.AlreadyPublishedError;
import de.bennyboer.kicherkrabbe.embroideries.unpublish.AlreadyUnpublishedError;
import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class PublishEmbroideryHttpHandlerTest extends HttpHandlerTest {

    private final Agent agent = Agent.user(AgentId.of("USER_ID"));

    @Test
    void shouldSuccessfullyPublishEmbroidery() {
        // given: the module is configured to return the new version
        when(module.publishEmbroidery("EMBROIDERY_ID", 0L, agent)).thenReturn(Mono.just(1L));

        // when: posting the publish request
        var exchange = post("/embroideries/EMBROIDERY_ID/publish?version=0", createTokenForUser("USER_ID"));

        // then: the response is successful and contains the new version
        exchange.expectStatus().isOk();
        exchange.expectBody(PublishEmbroideryResponse.class)
                .value(response -> assertThat(response.version).isEqualTo(1L));
    }

    @Test
    void shouldSuccessfullyUnpublishEmbroidery() {
        // given: the module is configured to return the new version
        when(module.unpublishEmbroidery("EMBROIDERY_ID", 1L, agent)).thenReturn(Mono.just(2L));

        // when: posting the unpublish request
        var exchange = post("/embroideries/EMBROIDERY_ID/unpublish?version=1", createTokenForUser("USER_ID"));

        // then: the response is successful and contains the new version
        exchange.expectStatus().isOk();
        exchange.expectBody(UnpublishEmbroideryResponse.class)
                .value(response -> assertThat(response.version).isEqualTo(2L));
    }

    @Test
    void shouldRespondWithPreconditionFailedWhenAlreadyPublishedOrUnpublished() {
        // given: the module is configured to raise already published and unpublished errors
        when(module.publishEmbroidery("EMBROIDERY_ID", 1L, agent)).thenReturn(Mono.error(new AlreadyPublishedError()));
        when(module.unpublishEmbroidery("EMBROIDERY_ID", 0L, agent)).thenReturn(Mono.error(new AlreadyUnpublishedError()));
        var token = createTokenForUser("USER_ID");

        // when: publishing an already published embroidery; then: the response is precondition failed
        post("/embroideries/EMBROIDERY_ID/publish?version=1", token).expectStatus().isEqualTo(412);

        // when: unpublishing an unpublished embroidery; then: the response is precondition failed
        post("/embroideries/EMBROIDERY_ID/unpublish?version=0", token).expectStatus().isEqualTo(412);
    }

    @Test
    void shouldRespondWith409OnAggregateVersionOutdatedError() {
        // given: the module is configured to raise a version outdated error
        when(module.publishEmbroidery("EMBROIDERY_ID", 0L, agent)).thenReturn(Mono.error(new AggregateVersionOutdatedError(
                AggregateType.of("EMBROIDERY"),
                AggregateId.of("EMBROIDERY_ID"),
                Version.zero()
        )));

        // when: posting the publish request
        var exchange = post("/embroideries/EMBROIDERY_ID/publish?version=0", createTokenForUser("USER_ID"));

        // then: the response is conflict
        exchange.expectStatus().isEqualTo(409);
    }

    @Test
    void shouldRespondWithBadRequestWhenVersionIsMissing() {
        // when: posting the publish request without a version
        var exchange = post("/embroideries/EMBROIDERY_ID/publish", createTokenForUser("USER_ID"));

        // then: the response is bad request
        exchange.expectStatus().isBadRequest();
    }

    @Test
    void shouldNotAllowUnauthorizedAccess() {
        // when: posting the publish request without a token; then: the response is unauthorized
        client.post().uri("/embroideries/EMBROIDERY_ID/publish?version=0").exchange().expectStatus().isUnauthorized();

        // when: posting the unpublish request without a token; then: the response is unauthorized
        client.post().uri("/embroideries/EMBROIDERY_ID/unpublish?version=0").exchange().expectStatus().isUnauthorized();
    }

    private WebTestClient.ResponseSpec post(String uri, String token) {
        return client.post()
                .uri(uri)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();
    }

}
