package de.bennyboer.kicherkrabbe.fabrics.http;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.fabrics.*;
import de.bennyboer.kicherkrabbe.fabrics.http.api.FabricTypeAvailabilityDTO;
import de.bennyboer.kicherkrabbe.fabrics.http.api.requests.CreateFabricRequest;
import de.bennyboer.kicherkrabbe.fabrics.http.api.responses.CreateFabricResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class CreateFabricHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyCreateFabric() {
        // given: a request to create a fabric
        var request = new CreateFabricRequest();
        var jerseyAvailability = new FabricTypeAvailabilityDTO();
        jerseyAvailability.typeId = "JERSEY_ID";
        jerseyAvailability.inStock = true;

        var cottonAvailability = new FabricTypeAvailabilityDTO();
        cottonAvailability.typeId = "COTTON_ID";
        cottonAvailability.inStock = false;

        request.name = "Ice bear party";
        request.imageId = "ICE_BEAR_IMAGE_ID";
        request.colorIds = Set.of("BLUE_ID", "WHITE_ID");
        request.topicIds = Set.of("WINTER_ID", "ANIMALS_ID");
        request.availability = Set.of(jerseyAvailability, cottonAvailability);

        // and: having a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a successful response
        when(module.createFabric(
                request.name,
                request.imageId,
                request.colorIds,
                request.topicIds,
                request.availability,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.just("FABRIC_ID"));

        // when: posting the request
        var exchange = client.post()
                .uri("/fabrics/create")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the ID of the new fabric
        var response = exchange.expectBody(CreateFabricResponse.class)
                .returnResult()
                .getResponseBody();
        assertThat(response.id).isEqualTo("FABRIC_ID");
    }

    @Test
    void shouldRespondWithBadRequestOnInvalidRequest() {
        // given: a request to create a fabric with an invalid request
        var jerseyAvailability = new FabricTypeAvailabilityDTO();
        jerseyAvailability.typeId = "JERSEY_ID";
        jerseyAvailability.inStock = true;

        var cottonAvailability = new FabricTypeAvailabilityDTO();
        cottonAvailability.typeId = "COTTON_ID";
        cottonAvailability.inStock = false;

        var request = new CreateFabricRequest();
        request.name = "Ice bear party";
        request.imageId = "";
        request.colorIds = Set.of("BLUE_ID", "WHITE_ID");
        request.topicIds = Set.of("WINTER_ID", "ANIMALS_ID");
        request.availability = Set.of(jerseyAvailability, cottonAvailability);

        // and: having a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return an illegal argument exception
        when(module.createFabric(
                request.name,
                request.imageId,
                request.colorIds,
                request.topicIds,
                request.availability,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.error(new IllegalArgumentException("Invalid request")));

        // when: posting the request
        var exchange = client.post()
                .uri("/fabrics/create")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is bad request
        exchange.expectStatus().isBadRequest();
    }

    @Test
    void shouldNotAllowUnauthorizedAccess() {
        // given: a request to create a fabric
        var request = new CreateFabricRequest();

        // when: posting the request without a token
        var exchange = client.post()
                .uri("/fabrics/create")
                .bodyValue(request)
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldNotAllowAccessWithInvalidToken() {
        // given: a request to create a fabric
        var request = new CreateFabricRequest();

        // when: posting the request with an invalid token
        var exchange = client.post()
                .uri("/fabrics/create")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithPreconditionFailedOnMissingTopicsError() {
        // given: a request to create a fabric with a missing topic
        var request = new CreateFabricRequest();
        var jerseyAvailability = new FabricTypeAvailabilityDTO();
        jerseyAvailability.typeId = "JERSEY_ID";
        jerseyAvailability.inStock = true;

        var cottonAvailability = new FabricTypeAvailabilityDTO();
        cottonAvailability.typeId = "COTTON_ID";
        cottonAvailability.inStock = false;

        request.name = "Ice bear party";
        request.imageId = "ICE_BEAR_IMAGE_ID";
        request.colorIds = Set.of("BLUE_ID", "WHITE_ID");
        request.topicIds = Set.of("WINTER_ID", "ANIMALS_ID", "MISSING_TOPIC_ID");
        request.availability = Set.of(jerseyAvailability, cottonAvailability);

        // and: having a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a topics missing error
        when(module.createFabric(
                request.name,
                request.imageId,
                request.colorIds,
                request.topicIds,
                request.availability,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.error(new TopicsMissingError(Set.of(TopicId.of("MISSING_TOPIC_ID")))));

        // when: posting the request
        var exchange = client.post()
                .uri("/fabrics/create")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is precondition failed
        exchange.expectStatus().isEqualTo(412);
    }

    @Test
    void shouldRespondWithPreconditionFailedOnMissingColorsError() {
        // given: a request to create a fabric with a missing color
        var request = new CreateFabricRequest();
        var jerseyAvailability = new FabricTypeAvailabilityDTO();
        jerseyAvailability.typeId = "JERSEY_ID";
        jerseyAvailability.inStock = true;

        var cottonAvailability = new FabricTypeAvailabilityDTO();
        cottonAvailability.typeId = "COTTON_ID";
        cottonAvailability.inStock = false;

        request.name = "Ice bear party";
        request.imageId = "ICE_BEAR_IMAGE_ID";
        request.colorIds = Set.of("BLUE_ID", "WHITE_ID", "MISSING_COLOR_ID");
        request.topicIds = Set.of("WINTER_ID", "ANIMALS_ID");
        request.availability = Set.of(jerseyAvailability, cottonAvailability);

        // and: having a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a colors missing error
        when(module.createFabric(
                request.name,
                request.imageId,
                request.colorIds,
                request.topicIds,
                request.availability,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.error(new ColorsMissingError(Set.of(ColorId.of("MISSING_COLOR_ID")))));

        // when: posting the request
        var exchange = client.post()
                .uri("/fabrics/create")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is precondition failed
        exchange.expectStatus().isEqualTo(412);
    }

    @Test
    void shouldRespondWithPreconditionFailedOnMissingFabricTypesError() {
        // given: a request to create a fabric with a missing fabric type
        var request = new CreateFabricRequest();
        var jerseyAvailability = new FabricTypeAvailabilityDTO();
        jerseyAvailability.typeId = "JERSEY_ID";
        jerseyAvailability.inStock = true;

        var cottonAvailability = new FabricTypeAvailabilityDTO();
        cottonAvailability.typeId = "COTTON_ID";
        cottonAvailability.inStock = false;

        var missingAvailability = new FabricTypeAvailabilityDTO();
        missingAvailability.typeId = "MISSING_FABRIC_TYPE_ID";
        missingAvailability.inStock = false;

        request.name = "Ice bear party";
        request.imageId = "ICE_BEAR_IMAGE_ID";
        request.colorIds = Set.of("BLUE_ID", "WHITE_ID");
        request.topicIds = Set.of("WINTER_ID", "ANIMALS_ID");
        request.availability = Set.of(jerseyAvailability, cottonAvailability, missingAvailability);

        // and: having a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a fabric types missing error
        when(module.createFabric(
                request.name,
                request.imageId,
                request.colorIds,
                request.topicIds,
                request.availability,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.error(new FabricTypesMissingError(Set.of(FabricTypeId.of("MISSING_FABRIC_TYPE_ID")))));

        // when: posting the request
        var exchange = client.post()
                .uri("/fabrics/create")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is precondition failed
        exchange.expectStatus().isEqualTo(412);
    }

}
