package de.bennyboer.kicherkrabbe.patterns.http;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.patterns.http.api.requests.UpdatePatternImagesRequest;
import de.bennyboer.kicherkrabbe.patterns.http.api.responses.UpdatePatternImagesResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class UpdatePatternImagesHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyUpdatePatternImages() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: a request to update the images of a pattern
        var request = new UpdatePatternImagesRequest();
        request.version = 3L;
        request.images = List.of("IMAGE_ID_1", "IMAGE_ID_2");

        // and: the module is configured to return a successful response
        when(module.updatePatternImages(
                "PATTERN_ID",
                3L,
                List.of("IMAGE_ID_1", "IMAGE_ID_2"),
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.just(4L));

        // when: posting the request
        var exchange = client.post()
                .uri("/api/patterns/PATTERN_ID/update/images")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the new version of the pattern
        exchange.expectBody(UpdatePatternImagesResponse.class)
                .value(response -> assertThat(response.version).isEqualTo(4L));
    }

    @Test
    void shouldRespondWith409OnAggregateVersionOutdatedError() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: a request to update the images of a pattern
        var request = new UpdatePatternImagesRequest();
        request.version = 3L;
        request.images = List.of("IMAGE_ID_1", "IMAGE_ID_2");

        // and: the module is configured to return a conflict response
        when(module.updatePatternImages(
                "PATTERN_ID",
                3L,
                List.of("IMAGE_ID_1", "IMAGE_ID_2"),
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.error(new AggregateVersionOutdatedError(
                AggregateType.of("TEST"),
                AggregateId.of("TEST_ID"),
                Version.zero()
        )));

        // when: posting the request
        var exchange = client.post()
                .uri("/api/patterns/PATTERN_ID/update/images")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is conflict
        exchange.expectStatus().isEqualTo(409);
    }

    @Test
    void shouldNotAllowUnauthorizedAccess() {
        // when: posting the request without a token
        var exchange = client.post()
                .uri("/api/patterns/PATTERN_ID/update/images")
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldNotAllowAccessWithInvalidToken() {
        // when: posting the request with an invalid token
        var exchange = client.post()
                .uri("/api/patterns/PATTERN_ID/update/images")
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithBadRequestOnInvalidRequest() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: a request to update the images of a pattern
        var request = new UpdatePatternImagesRequest();
        request.version = 3L;
        request.images = List.of("", "IMAGE_ID_2");

        // and: the module is configured to return an illegal argument exception
        when(module.updatePatternImages(
                "PATTERN_ID",
                3L,
                List.of("", "IMAGE_ID_2"),
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.error(new IllegalArgumentException("Invalid request")));

        // when: posting the request
        var exchange = client.post()
                .uri("/api/patterns/PATTERN_ID/update/images")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is bad request
        exchange.expectStatus().isBadRequest();
    }

}
