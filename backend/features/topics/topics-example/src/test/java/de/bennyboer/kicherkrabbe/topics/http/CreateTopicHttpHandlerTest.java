package de.bennyboer.kicherkrabbe.topics.http;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.topics.http.api.requests.CreateTopicRequest;
import de.bennyboer.kicherkrabbe.topics.http.api.responses.CreateTopicResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class CreateTopicHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyCreateTopic() {
        // given: a request to create a topic
        var request = new CreateTopicRequest();
        request.name = "Winter";

        // and: having a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a successful response
        when(module.createTopic(
                request.name,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.just("TOPIC_ID"));

        // when: posting the request
        var exchange = client.post()
                .uri("/api/topics/create")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the ID of the new topic
        var response = exchange.expectBody(CreateTopicResponse.class)
                .returnResult()
                .getResponseBody();
        assertThat(response.id).isEqualTo("TOPIC_ID");
    }

    @Test
    void shouldNotAllowUnauthorizedAccess() {
        // given: a request to create a topic
        var request = new CreateTopicRequest();
        request.name = "Winter";

        // when: posting the request without a token
        var exchange = client.post()
                .uri("/api/topics/create")
                .bodyValue(request)
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldNotAllowAccessWithInvalidToken() {
        // given: a request to create a topic
        var request = new CreateTopicRequest();
        request.name = "Winter";

        // when: posting the request with an invalid token
        var exchange = client.post()
                .uri("/api/topics/create")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithBadRequestOnIllegalRequest() {
        // given: a request to create a topic with an illegal name
        var request = new CreateTopicRequest();
        request.name = " ";

        // and: having a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return an illegal argument exception
        when(module.createTopic(
                request.name,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.error(new IllegalArgumentException("Illegal name")));

        // when: posting the request
        var exchange = client.post()
                .uri("/api/topics/create")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is bad request
        exchange.expectStatus().isBadRequest();
    }

}
