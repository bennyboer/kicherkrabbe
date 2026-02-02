package de.bennyboer.kicherkrabbe.fabrics.http;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.fabrics.ColorId;
import de.bennyboer.kicherkrabbe.fabrics.http.api.responses.QueryColorsResponse;
import de.bennyboer.kicherkrabbe.fabrics.persistence.colors.Color;
import de.bennyboer.kicherkrabbe.fabrics.persistence.colors.ColorName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class QueryColorsUsedInFabricsHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyQueryColorsAsUser() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a successful response
        when(module.getColorsUsedInFabrics(Agent.user(AgentId.of("USER_ID")))).thenReturn(Flux.just(
                Color.of(ColorId.of("COLOR_ID_1"), ColorName.of("COLOR_NAME_1"), 255, 0, 0),
                Color.of(ColorId.of("COLOR_ID_2"), ColorName.of("COLOR_NAME_2"), 0, 255, 0)
        ));

        // when: posting the request
        var exchange = client.get()
                .uri("/fabrics/colors/used")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the colors
        var response = exchange.expectBody(QueryColorsResponse.class).returnResult().getResponseBody();
        assertThat(response.colors).hasSize(2);

        var color1 = response.colors.get(0);
        assertThat(color1.id).isEqualTo("COLOR_ID_1");
        assertThat(color1.name).isEqualTo("COLOR_NAME_1");
        assertThat(color1.red).isEqualTo(255);
        assertThat(color1.green).isEqualTo(0);
        assertThat(color1.blue).isEqualTo(0);

        var color2 = response.colors.get(1);
        assertThat(color2.id).isEqualTo("COLOR_ID_2");
        assertThat(color2.name).isEqualTo("COLOR_NAME_2");
        assertThat(color2.red).isEqualTo(0);
        assertThat(color2.green).isEqualTo(255);
        assertThat(color2.blue).isEqualTo(0);
    }

    @Test
    void shouldAllowUnauthorizedAccess() {
        // given: the module is configured to return a successful response
        when(module.getColorsUsedInFabrics(Agent.anonymous())).thenReturn(Flux.just(
                Color.of(ColorId.of("COLOR_ID_1"), ColorName.of("COLOR_NAME_1"), 255, 0, 0),
                Color.of(ColorId.of("COLOR_ID_2"), ColorName.of("COLOR_NAME_2"), 0, 255, 0)
        ));

        // when: posting the request without a token
        var exchange = client.get()
                .uri("/fabrics/colors/used")
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the colors
        var response = exchange.expectBody(QueryColorsResponse.class).returnResult().getResponseBody();
        assertThat(response.colors).hasSize(2);

        var color1 = response.colors.get(0);
        assertThat(color1.id).isEqualTo("COLOR_ID_1");
        assertThat(color1.name).isEqualTo("COLOR_NAME_1");
        assertThat(color1.red).isEqualTo(255);
        assertThat(color1.green).isEqualTo(0);
        assertThat(color1.blue).isEqualTo(0);

        var color2 = response.colors.get(1);
        assertThat(color2.id).isEqualTo("COLOR_ID_2");
        assertThat(color2.name).isEqualTo("COLOR_NAME_2");
        assertThat(color2.red).isEqualTo(0);
        assertThat(color2.green).isEqualTo(255);
        assertThat(color2.blue).isEqualTo(0);
    }

    @Test
    void shouldNotAllowAccessWithInvalidToken() {
        // when: posting the request with an invalid token
        var exchange = client.get()
                .uri("/fabrics/colors/used")
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

}
