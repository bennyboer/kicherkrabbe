package de.bennyboer.kicherkrabbe.fabrics.http;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.fabrics.TopicId;
import de.bennyboer.kicherkrabbe.fabrics.http.api.responses.QueryTopicsResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class QueryTopicsHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyQueryTopicsAsUser() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a successful response
        when(module.getTopicsUsedInFabrics(Agent.user(AgentId.of("USER_ID")))).thenReturn(Flux.just(
                TopicId.of("TOPIC_ID_1"),
                TopicId.of("TOPIC_ID_2")
        ));

        // when: posting the request
        var exchange = client.get()
                .uri("/api/fabrics/topics")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the topics
        var response = exchange.expectBody(QueryTopicsResponse.class).returnResult().getResponseBody();
        assertThat(response.topicIds).containsExactlyInAnyOrder("TOPIC_ID_1", "TOPIC_ID_2");
    }

    @Test
    void shouldAllowUnauthorizedAccess() {
        // given: the module is configured to return a successful response
        when(module.getTopicsUsedInFabrics(Agent.anonymous())).thenReturn(Flux.just(
                TopicId.of("TOPIC_ID_1"),
                TopicId.of("TOPIC_ID_2")
        ));

        // when: posting the request without a token
        var exchange = client.get()
                .uri("/api/fabrics/topics")
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the topics
        var response = exchange.expectBody(QueryTopicsResponse.class).returnResult().getResponseBody();
        assertThat(response.topicIds).containsExactlyInAnyOrder("TOPIC_ID_1", "TOPIC_ID_2");
    }

    @Test
    void shouldNotAllowAccessWithInvalidToken() {
        // when: posting the request with an invalid token
        var exchange = client.get()
                .uri("/api/fabrics/topics")
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

}
