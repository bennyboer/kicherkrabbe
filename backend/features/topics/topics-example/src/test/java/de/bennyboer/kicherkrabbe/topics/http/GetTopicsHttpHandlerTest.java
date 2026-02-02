package de.bennyboer.kicherkrabbe.topics.http;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.topics.TopicDetails;
import de.bennyboer.kicherkrabbe.topics.TopicId;
import de.bennyboer.kicherkrabbe.topics.TopicName;
import de.bennyboer.kicherkrabbe.topics.TopicsPage;
import de.bennyboer.kicherkrabbe.topics.http.api.responses.QueryTopicsResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class GetTopicsHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyGetTopics() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a successful response
        var resultingPage = TopicsPage.of(
                2L,
                8L,
                4L,
                List.of(
                        TopicDetails.of(
                                TopicId.of("TOPIC_ID_1"),
                                Version.zero(),
                                TopicName.of("Winter"),
                                Instant.parse("2024-03-18T11:25:00Z")
                        ),
                        TopicDetails.of(
                                TopicId.of("TOPIC_ID_2"),
                                Version.zero(),
                                TopicName.of("Fall"),
                                Instant.parse("2024-03-12T12:30:00Z")
                        )
                )
        );
        when(module.getTopics(
                "term",
                2L,
                8L,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.just(resultingPage));

        // when: posting the request
        var exchange = client.get()
                .uri("/topics/?searchTerm=term&skip=2&limit=8")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the topics
        var response = exchange.expectBody(QueryTopicsResponse.class)
                .returnResult()
                .getResponseBody();
        assertThat(response.skip).isEqualTo(2L);
        assertThat(response.limit).isEqualTo(8L);
        assertThat(response.total).isEqualTo(4L);
        assertThat(response.topics).hasSize(2);
        var actualTopics = response.topics;
        assertThat(actualTopics.get(0).id).isEqualTo("TOPIC_ID_1");
        assertThat(actualTopics.get(0).version).isEqualTo(0);
        assertThat(actualTopics.get(0).name).isEqualTo("Winter");
        assertThat(actualTopics.get(0).createdAt).isEqualTo("2024-03-18T11:25:00Z");
        assertThat(actualTopics.get(1).id).isEqualTo("TOPIC_ID_2");
        assertThat(actualTopics.get(1).version).isEqualTo(0);
        assertThat(actualTopics.get(1).name).isEqualTo("Fall");
        assertThat(actualTopics.get(1).createdAt).isEqualTo("2024-03-12T12:30:00Z");
    }

    @Test
    void shouldNotAllowUnauthorizedAccess() {
        // when: posting the request without a token
        var exchange = client.get()
                .uri("/topics/?searchTerm=term&skip=2&limit=8")
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldNotAllowAccessWithInvalidToken() {
        // when: posting the request with an invalid token
        var exchange = client.get()
                .uri("/topics/?searchTerm=term&skip=2&limit=8")
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

}
