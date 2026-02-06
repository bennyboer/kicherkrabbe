package de.bennyboer.kicherkrabbe.fabrics.http;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.fabrics.*;
import de.bennyboer.kicherkrabbe.fabrics.http.api.FabricsAvailabilityFilterDTO;
import de.bennyboer.kicherkrabbe.fabrics.http.api.FabricsSortDTO;
import de.bennyboer.kicherkrabbe.fabrics.http.api.requests.QueryPublishedFabricsRequest;
import de.bennyboer.kicherkrabbe.fabrics.http.api.responses.QueryPublishedFabricsResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

import static de.bennyboer.kicherkrabbe.fabrics.http.api.FabricsSortDirectionDTO.ASCENDING;
import static de.bennyboer.kicherkrabbe.fabrics.http.api.FabricsSortPropertyDTO.ALPHABETICAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class QueryPublishedFabricsHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyQueryPublishedFabrics() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: a request to query published fabrics
        var request = new QueryPublishedFabricsRequest();
        request.searchTerm = "test";
        request.skip = 3;
        request.limit = 5;
        request.sort = new FabricsSortDTO();
        request.sort.property = ALPHABETICAL;
        request.sort.direction = ASCENDING;
        request.colorIds = Set.of("COLOR_ID");
        request.topicIds = Set.of("TOPIC_ID");
        request.availability = new FabricsAvailabilityFilterDTO();
        request.availability.active = false;
        request.availability.inStock = true;

        // and: the module is configured to return a successful response
        when(module.getPublishedFabrics(
                request.searchTerm,
                request.colorIds,
                request.topicIds,
                request.availability,
                request.sort,
                request.skip,
                request.limit,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.just(PublishedFabricsPage.of(
                3,
                5,
                4,
                List.of(PublishedFabric.of(
                        FabricId.of("FABRIC_ID"),
                        FabricName.of("Fabric name"),
                        FabricAlias.of("fabric-name"),
                        ImageId.of("IMAGE_ID"),
                        Set.of(ColorId.of("COLOR_ID")),
                        Set.of(TopicId.of("TOPIC_ID")),
                        Set.of()
                ))
        )));

        // when: posting the request
        var exchange = client.post()
                .uri("/fabrics/published")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the published fabrics
        var response = exchange.expectBody(QueryPublishedFabricsResponse.class).returnResult().getResponseBody();
        assertThat(response.skip).isEqualTo(3);
        assertThat(response.limit).isEqualTo(5);
        assertThat(response.total).isEqualTo(4);
        assertThat(response.fabrics).hasSize(1);
        var fabric = response.fabrics.get(0);
        assertThat(fabric.id).isEqualTo("FABRIC_ID");
        assertThat(fabric.alias).isEqualTo("fabric-name");
        assertThat(fabric.name).isEqualTo("Fabric name");
        assertThat(fabric.imageId).isEqualTo("IMAGE_ID");
        assertThat(fabric.colorIds).containsExactly("COLOR_ID");
        assertThat(fabric.topicIds).containsExactly("TOPIC_ID");
        assertThat(fabric.availability).isEmpty();
    }

    @Test
    void shouldAllowUnauthorizedAccess() {
        // given: a request to query published fabrics
        var request = new QueryPublishedFabricsRequest();
        request.searchTerm = "test";
        request.skip = 3;
        request.limit = 5;
        request.sort = new FabricsSortDTO();
        request.sort.property = ALPHABETICAL;
        request.sort.direction = ASCENDING;
        request.colorIds = Set.of("COLOR_ID");
        request.topicIds = Set.of("TOPIC_ID");
        request.availability = new FabricsAvailabilityFilterDTO();
        request.availability.active = false;
        request.availability.inStock = true;

        // and: the module is configured to return a successful response
        when(module.getPublishedFabrics(
                request.searchTerm,
                request.colorIds,
                request.topicIds,
                request.availability,
                request.sort,
                request.skip,
                request.limit,
                Agent.anonymous()
        )).thenReturn(Mono.just(PublishedFabricsPage.of(
                3,
                5,
                4,
                List.of(PublishedFabric.of(
                        FabricId.of("FABRIC_ID"),
                        FabricName.of("Fabric name"),
                        FabricAlias.of("fabric-name"),
                        ImageId.of("IMAGE_ID"),
                        Set.of(ColorId.of("COLOR_ID")),
                        Set.of(TopicId.of("TOPIC_ID")),
                        Set.of()
                ))
        )));

        // when: posting the request without a token
        var exchange = client.post()
                .uri("/fabrics/published")
                .bodyValue(request)
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the published fabrics
        var response = exchange.expectBody(QueryPublishedFabricsResponse.class).returnResult().getResponseBody();
        assertThat(response.skip).isEqualTo(3);
        assertThat(response.limit).isEqualTo(5);
        assertThat(response.total).isEqualTo(4);
        assertThat(response.fabrics).hasSize(1);
        var fabric = response.fabrics.get(0);
        assertThat(fabric.id).isEqualTo("FABRIC_ID");
        assertThat(fabric.alias).isEqualTo("fabric-name");
        assertThat(fabric.name).isEqualTo("Fabric name");
        assertThat(fabric.imageId).isEqualTo("IMAGE_ID");
        assertThat(fabric.colorIds).containsExactly("COLOR_ID");
        assertThat(fabric.topicIds).containsExactly("TOPIC_ID");
        assertThat(fabric.availability).isEmpty();
    }

    @Test
    void shouldNotAllowAccessWithInvalidToken() {
        // given: a request to query published fabrics
        var request = new QueryPublishedFabricsRequest();
        request.searchTerm = "test";
        request.skip = 3;
        request.limit = 5;
        request.sort = new FabricsSortDTO();
        request.sort.property = ALPHABETICAL;
        request.sort.direction = ASCENDING;
        request.colorIds = Set.of("COLOR_ID");
        request.topicIds = Set.of("TOPIC_ID");
        request.availability = new FabricsAvailabilityFilterDTO();
        request.availability.active = false;
        request.availability.inStock = true;

        // when: posting the request with an invalid token
        var exchange = client.post()
                .uri("/fabrics/published")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

}
