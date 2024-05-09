package de.bennyboer.kicherkrabbe.fabrictypes.http;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.fabrictypes.FabricTypeDetails;
import de.bennyboer.kicherkrabbe.fabrictypes.FabricTypeId;
import de.bennyboer.kicherkrabbe.fabrictypes.FabricTypeName;
import de.bennyboer.kicherkrabbe.fabrictypes.FabricTypesPage;
import de.bennyboer.kicherkrabbe.fabrictypes.http.responses.QueryFabricTypesResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class GetFabricTypesHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyGetFabricTypes() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a successful response
        var resultingPage = FabricTypesPage.of(
                2L,
                8L,
                4L,
                List.of(
                        FabricTypeDetails.of(
                                FabricTypeId.of("FABRIC_TYPE_ID_1"),
                                Version.zero(),
                                FabricTypeName.of("Jersey"),
                                Instant.parse("2024-03-18T11:25:00Z")
                        ),
                        FabricTypeDetails.of(
                                FabricTypeId.of("FABRIC_TYPE_ID_2"),
                                Version.zero(),
                                FabricTypeName.of("French-Terry"),
                                Instant.parse("2024-03-12T12:30:00Z")
                        )
                )
        );
        when(module.getFabricTypes(
                "term",
                2L,
                8L,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.just(resultingPage));

        // when: posting the request
        var exchange = client.get()
                .uri("/api/fabric-types/?searchTerm=term&skip=2&limit=8")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the fabric types
        var response = exchange.expectBody(QueryFabricTypesResponse.class)
                .returnResult()
                .getResponseBody();
        assertThat(response.skip).isEqualTo(2L);
        assertThat(response.limit).isEqualTo(8L);
        assertThat(response.total).isEqualTo(4L);
        assertThat(response.fabricTypes).hasSize(2);
        var actualFabricTypes = response.fabricTypes;
        assertThat(actualFabricTypes.get(0).id).isEqualTo("FABRIC_TYPE_ID_1");
        assertThat(actualFabricTypes.get(0).version).isEqualTo(0);
        assertThat(actualFabricTypes.get(0).name).isEqualTo("Jersey");
        assertThat(actualFabricTypes.get(0).createdAt).isEqualTo("2024-03-18T11:25:00Z");
        assertThat(actualFabricTypes.get(1).id).isEqualTo("FABRIC_TYPE_ID_2");
        assertThat(actualFabricTypes.get(1).version).isEqualTo(0);
        assertThat(actualFabricTypes.get(1).name).isEqualTo("French-Terry");
        assertThat(actualFabricTypes.get(1).createdAt).isEqualTo("2024-03-12T12:30:00Z");
    }

    @Test
    void shouldNotAllowUnauthorizedAccess() {
        // when: posting the request without a token
        var exchange = client.get()
                .uri("/api/fabric-types/?searchTerm=term&skip=2&limit=8")
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldNotAllowAccessWithInvalidToken() {
        // when: posting the request with an invalid token
        var exchange = client.get()
                .uri("/api/fabric-types/?searchTerm=term&skip=2&limit=8")
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

}
