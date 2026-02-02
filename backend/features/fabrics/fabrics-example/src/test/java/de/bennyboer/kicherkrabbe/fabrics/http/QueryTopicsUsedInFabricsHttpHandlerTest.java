package de.bennyboer.kicherkrabbe.fabrics.http;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.fabrics.TopicId;
import de.bennyboer.kicherkrabbe.fabrics.http.api.responses.QueryTopicsResponse;
import de.bennyboer.kicherkrabbe.fabrics.persistence.topics.Topic;
import de.bennyboer.kicherkrabbe.fabrics.persistence.topics.TopicName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class QueryTopicsUsedInFabricsHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyQueryTopicsAsUser() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a successful response
        when(module.getTopicsUsedInFabrics(Agent.user(AgentId.of("USER_ID")))).thenReturn(Flux.just(
                Topic.of(TopicId.of("TOPIC_ID_1"), TopicName.of("TOPIC_NAME_1")),
                Topic.of(TopicId.of("TOPIC_ID_2"), TopicName.of("TOPIC_NAME_2"))
        ));

        // when: posting the request
        var exchange = client.get()
                .uri("/fabrics/topics/used")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the topics
        var response = exchange.expectBody(QueryTopicsResponse.class).returnResult().getResponseBody();
        assertThat(response.topics).hasSize(2);

        var topic1 = response.topics.get(0);
        assertThat(topic1.id).isEqualTo("TOPIC_ID_1");
        assertThat(topic1.name).isEqualTo("TOPIC_NAME_1");

        var topic2 = response.topics.get(1);
        assertThat(topic2.id).isEqualTo("TOPIC_ID_2");
        assertThat(topic2.name).isEqualTo("TOPIC_NAME_2");
    }

    @Test
    void shouldAllowUnauthorizedAccess() {
        // given: the module is configured to return a successful response
        when(module.getTopicsUsedInFabrics(Agent.anonymous())).thenReturn(Flux.just(
                Topic.of(TopicId.of("TOPIC_ID_1"), TopicName.of("TOPIC_NAME_1")),
                Topic.of(TopicId.of("TOPIC_ID_2"), TopicName.of("TOPIC_NAME_2"))
        ));

        // when: posting the request without a token
        var exchange = client.get()
                .uri("/fabrics/topics/used")
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the topics
        var response = exchange.expectBody(QueryTopicsResponse.class).returnResult().getResponseBody();
        assertThat(response.topics).hasSize(2);

        var topic1 = response.topics.get(0);
        assertThat(topic1.id).isEqualTo("TOPIC_ID_1");
        assertThat(topic1.name).isEqualTo("TOPIC_NAME_1");

        var topic2 = response.topics.get(1);
        assertThat(topic2.id).isEqualTo("TOPIC_ID_2");
        assertThat(topic2.name).isEqualTo("TOPIC_NAME_2");
    }

    @Test
    void shouldNotAllowAccessWithInvalidToken() {
        // when: posting the request with an invalid token
        var exchange = client.get()
                .uri("/fabrics/topics/used")
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

}
