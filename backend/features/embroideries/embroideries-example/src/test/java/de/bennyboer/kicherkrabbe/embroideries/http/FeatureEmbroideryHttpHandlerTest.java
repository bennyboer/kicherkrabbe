package de.bennyboer.kicherkrabbe.embroideries.http;

import de.bennyboer.kicherkrabbe.embroideries.feature.AlreadyFeaturedError;
import de.bennyboer.kicherkrabbe.embroideries.http.api.responses.FeatureEmbroideryResponse;
import de.bennyboer.kicherkrabbe.embroideries.http.api.responses.UnfeatureEmbroideryResponse;
import de.bennyboer.kicherkrabbe.embroideries.unfeature.AlreadyUnfeaturedError;
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

public class FeatureEmbroideryHttpHandlerTest extends HttpHandlerTest {

    private final Agent agent = Agent.user(AgentId.of("USER_ID"));

    @Test
    void shouldSuccessfullyFeatureEmbroidery() {
        // given: the module is configured to return the new version
        when(module.featureEmbroidery("EMBROIDERY_ID", 0L, agent)).thenReturn(Mono.just(1L));

        // when: posting the feature request
        var exchange = post("/embroideries/EMBROIDERY_ID/feature?version=0", createTokenForUser("USER_ID"));

        // then: the response is successful and contains the new version
        exchange.expectStatus().isOk();
        exchange.expectBody(FeatureEmbroideryResponse.class)
                .value(response -> assertThat(response.version).isEqualTo(1L));
    }

    @Test
    void shouldSuccessfullyUnfeatureEmbroidery() {
        // given: the module is configured to return the new version
        when(module.unfeatureEmbroidery("EMBROIDERY_ID", 1L, agent)).thenReturn(Mono.just(2L));

        // when: posting the unfeature request
        var exchange = post("/embroideries/EMBROIDERY_ID/unfeature?version=1", createTokenForUser("USER_ID"));

        // then: the response is successful and contains the new version
        exchange.expectStatus().isOk();
        exchange.expectBody(UnfeatureEmbroideryResponse.class)
                .value(response -> assertThat(response.version).isEqualTo(2L));
    }

    @Test
    void shouldRespondWithPreconditionFailedWhenAlreadyFeaturedOrUnfeatured() {
        // given: the module is configured to raise already featured and unfeatured errors
        when(module.featureEmbroidery("EMBROIDERY_ID", 1L, agent)).thenReturn(Mono.error(new AlreadyFeaturedError()));
        when(module.unfeatureEmbroidery("EMBROIDERY_ID", 0L, agent)).thenReturn(Mono.error(new AlreadyUnfeaturedError()));
        var token = createTokenForUser("USER_ID");

        // when: featuring an already featured embroidery; then: the response is precondition failed
        post("/embroideries/EMBROIDERY_ID/feature?version=1", token).expectStatus().isEqualTo(412);

        // when: unfeaturing an embroidery that is not featured; then: the response is precondition failed
        post("/embroideries/EMBROIDERY_ID/unfeature?version=0", token).expectStatus().isEqualTo(412);
    }

    @Test
    void shouldRespondWith409OnAggregateVersionOutdatedError() {
        // given: the module is configured to raise a version outdated error
        when(module.featureEmbroidery("EMBROIDERY_ID", 0L, agent)).thenReturn(Mono.error(new AggregateVersionOutdatedError(
                AggregateType.of("EMBROIDERY"),
                AggregateId.of("EMBROIDERY_ID"),
                Version.zero()
        )));

        // when: posting the feature request
        var exchange = post("/embroideries/EMBROIDERY_ID/feature?version=0", createTokenForUser("USER_ID"));

        // then: the response is conflict
        exchange.expectStatus().isEqualTo(409);
    }

    @Test
    void shouldNotAllowUnauthorizedAccess() {
        // when: posting the feature request without a token; then: the response is unauthorized
        client.post().uri("/embroideries/EMBROIDERY_ID/feature?version=0").exchange().expectStatus().isUnauthorized();

        // when: posting the unfeature request without a token; then: the response is unauthorized
        client.post().uri("/embroideries/EMBROIDERY_ID/unfeature?version=0").exchange().expectStatus().isUnauthorized();
    }

    private WebTestClient.ResponseSpec post(String uri, String token) {
        return client.post()
                .uri(uri)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();
    }

}
