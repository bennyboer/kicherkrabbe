package de.bennyboer.kicherkrabbe.fabrics.http;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.fabrics.FabricTypeId;
import de.bennyboer.kicherkrabbe.fabrics.http.api.responses.QueryFabricTypesResponse;
import de.bennyboer.kicherkrabbe.fabrics.persistence.fabrictypes.FabricType;
import de.bennyboer.kicherkrabbe.fabrics.persistence.fabrictypes.FabricTypeName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class QueryFabricTypesUsedInFabricsHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyQueryFabricTypesAsUser() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a successful response
        when(module.getFabricTypesUsedInFabrics(Agent.user(AgentId.of("USER_ID")))).thenReturn(Flux.just(
                FabricType.of(FabricTypeId.of("FABRIC_TYPE_ID_1"), FabricTypeName.of("FABRIC_TYPE_NAME_1")),
                FabricType.of(FabricTypeId.of("FABRIC_TYPE_ID_2"), FabricTypeName.of("FABRIC_TYPE_NAME_2"))
        ));

        // when: posting the request
        var exchange = client.get()
                .uri("/fabrics/fabric-types/used")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the fabric-types
        var response = exchange.expectBody(QueryFabricTypesResponse.class).returnResult().getResponseBody();
        assertThat(response.fabricTypes).hasSize(2);

        var topic1 = response.fabricTypes.get(0);
        assertThat(topic1.id).isEqualTo("FABRIC_TYPE_ID_1");
        assertThat(topic1.name).isEqualTo("FABRIC_TYPE_NAME_1");

        var topic2 = response.fabricTypes.get(1);
        assertThat(topic2.id).isEqualTo("FABRIC_TYPE_ID_2");
        assertThat(topic2.name).isEqualTo("FABRIC_TYPE_NAME_2");
    }

    @Test
    void shouldAllowUnauthorizedAccess() {
        // given: the module is configured to return a successful response
        when(module.getFabricTypesUsedInFabrics(Agent.anonymous())).thenReturn(Flux.just(
                FabricType.of(FabricTypeId.of("FABRIC_TYPE_ID_1"), FabricTypeName.of("FABRIC_TYPE_NAME_1")),
                FabricType.of(FabricTypeId.of("FABRIC_TYPE_ID_2"), FabricTypeName.of("FABRIC_TYPE_NAME_2"))
        ));

        // when: posting the request without a token
        var exchange = client.get()
                .uri("/fabrics/fabric-types/used")
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the topics
        var response = exchange.expectBody(QueryFabricTypesResponse.class).returnResult().getResponseBody();
        assertThat(response.fabricTypes).hasSize(2);

        var topic1 = response.fabricTypes.get(0);
        assertThat(topic1.id).isEqualTo("FABRIC_TYPE_ID_1");
        assertThat(topic1.name).isEqualTo("FABRIC_TYPE_NAME_1");

        var topic2 = response.fabricTypes.get(1);
        assertThat(topic2.id).isEqualTo("FABRIC_TYPE_ID_2");
        assertThat(topic2.name).isEqualTo("FABRIC_TYPE_NAME_2");
    }

    @Test
    void shouldNotAllowAccessWithInvalidToken() {
        // when: posting the request with an invalid token
        var exchange = client.get()
                .uri("/fabrics/fabric-types/used")
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

}
