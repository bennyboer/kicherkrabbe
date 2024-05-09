package de.bennyboer.kicherkrabbe.colors.http;

import de.bennyboer.kicherkrabbe.colors.ColorDetails;
import de.bennyboer.kicherkrabbe.colors.ColorId;
import de.bennyboer.kicherkrabbe.colors.ColorName;
import de.bennyboer.kicherkrabbe.colors.ColorsPage;
import de.bennyboer.kicherkrabbe.colors.http.responses.QueryColorsResponse;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class GetColorsHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyGetColors() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a successful response
        var resultingPage = ColorsPage.of(
                2L,
                8L,
                4L,
                List.of(
                        ColorDetails.of(
                                ColorId.of("COLOR_ID_1"),
                                Version.zero(),
                                ColorName.of("Red"),
                                255,
                                0,
                                0,
                                Instant.parse("2024-03-18T11:25:00Z")
                        ),
                        ColorDetails.of(
                                ColorId.of("COLOR_ID_2"),
                                Version.zero(),
                                ColorName.of("Blue"),
                                0,
                                0,
                                255,
                                Instant.parse("2024-03-12T12:30:00Z")
                        )
                )
        );
        when(module.getColors(
                "term",
                2L,
                8L,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.just(resultingPage));

        // when: posting the request
        var exchange = client.get()
                .uri("/api/colors/?searchTerm=term&skip=2&limit=8")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the colors
        var response = exchange.expectBody(QueryColorsResponse.class)
                .returnResult()
                .getResponseBody();
        assertThat(response.skip).isEqualTo(2L);
        assertThat(response.limit).isEqualTo(8L);
        assertThat(response.total).isEqualTo(4L);
        assertThat(response.colors).hasSize(2);
        var actualColors = response.colors;
        assertThat(actualColors.get(0).id).isEqualTo("COLOR_ID_1");
        assertThat(actualColors.get(0).version).isEqualTo(0);
        assertThat(actualColors.get(0).name).isEqualTo("Red");
        assertThat(actualColors.get(0).red).isEqualTo(255);
        assertThat(actualColors.get(0).green).isEqualTo(0);
        assertThat(actualColors.get(0).blue).isEqualTo(0);
        assertThat(actualColors.get(0).createdAt).isEqualTo("2024-03-18T11:25:00Z");
        assertThat(actualColors.get(1).id).isEqualTo("COLOR_ID_2");
        assertThat(actualColors.get(1).version).isEqualTo(0);
        assertThat(actualColors.get(1).name).isEqualTo("Blue");
        assertThat(actualColors.get(1).red).isEqualTo(0);
        assertThat(actualColors.get(1).green).isEqualTo(0);
        assertThat(actualColors.get(1).blue).isEqualTo(255);
        assertThat(actualColors.get(1).createdAt).isEqualTo("2024-03-12T12:30:00Z");
    }

    @Test
    void shouldNotAllowUnauthorizedAccess() {
        // when: posting the request without a token
        var exchange = client.get()
                .uri("/api/colors/?searchTerm=term&skip=2&limit=8")
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldNotAllowAccessWithInvalidToken() {
        // when: posting the request with an invalid token
        var exchange = client.get()
                .uri("/api/colors/?searchTerm=term&skip=2&limit=8")
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

}
