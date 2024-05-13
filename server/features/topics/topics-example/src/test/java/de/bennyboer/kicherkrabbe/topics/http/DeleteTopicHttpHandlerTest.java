package de.bennyboer.kicherkrabbe.topics.http;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.topics.http.api.responses.DeleteTopicResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class DeleteTopicHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyDeleteTopic() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a successful response
        when(module.deleteTopic(
                "TOPIC_ID",
                3L,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.just(4L));

        // when: posting the request
        var exchange = client.delete()
                .uri("/api/topics/TOPIC_ID/?version=3")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the version of the deleted topic
        var response = exchange.expectBody(DeleteTopicResponse.class)
                .returnResult()
                .getResponseBody();
        assertThat(response.version).isEqualTo(4L);
    }

    @Test
    void shouldNotAllowUnauthorizedAccess() {
        // when: posting the request without a token
        var exchange = client.delete()
                .uri("/api/topics/TOPIC_ID/?version=3")
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldNotAllowAccessWithInvalidToken() {
        // when: posting the request with an invalid token
        var exchange = client.delete()
                .uri("/api/topics/TOPIC_ID/?version=3")
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWith409ConflictIfVersionIsOutdated() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a conflict response
        when(module.deleteTopic(
                "TOPIC_ID",
                3L,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.error(new AggregateVersionOutdatedError(
                AggregateType.of("TOPIC"),
                AggregateId.of("TOPIC_ID"),
                Version.of(3L)
        )));

        // when: posting the request
        var exchange = client.delete()
                .uri("/api/topics/TOPIC_ID/?version=3")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is a conflict
        exchange.expectStatus().isEqualTo(409);
    }

}
