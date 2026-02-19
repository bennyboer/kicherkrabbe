package de.bennyboer.kicherkrabbe.fabrics.http;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.fabrics.FabricTypeId;
import de.bennyboer.kicherkrabbe.fabrics.FabricTypesMissingError;
import de.bennyboer.kicherkrabbe.fabrics.http.api.FabricTypeAvailabilityDTO;
import de.bennyboer.kicherkrabbe.fabrics.http.api.requests.UpdateFabricAvailabilityRequest;
import de.bennyboer.kicherkrabbe.fabrics.http.api.responses.UpdateFabricAvailabilityResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class UpdateFabricAvailabilityHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyUpdateFabricAvailability() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: a request to update the availability of a fabric
        var jerseyAvailability = new FabricTypeAvailabilityDTO();
        jerseyAvailability.typeId = "JERSEY_TYPE_ID";
        jerseyAvailability.inStock = true;
        var cottonAvailability = new FabricTypeAvailabilityDTO();
        cottonAvailability.typeId = "COTTON_TYPE_ID";
        cottonAvailability.inStock = false;

        var request = new UpdateFabricAvailabilityRequest();
        request.version = 3L;
        request.availability = Set.of(jerseyAvailability, cottonAvailability);

        // and: the module is configured to return a successful response
        when(module.updateFabricAvailability(
                "FABRIC_ID",
                3L,
                Set.of(jerseyAvailability, cottonAvailability),
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.just(4L));

        // when: posting the request
        var exchange = client.post()
                .uri("/fabrics/FABRIC_ID/update/availability")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the new version of the fabric
        exchange.expectBody(UpdateFabricAvailabilityResponse.class)
                .value(response -> assertThat(response.version).isEqualTo(4L));
    }

    @Test
    void shouldRespondWith409OnAggregateVersionOutdatedError() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: a request to update the availability of a fabric
        var request = new UpdateFabricAvailabilityRequest();
        request.version = 3L;
        request.availability = Set.of();

        // and: the module is configured to return a conflict response
        when(module.updateFabricAvailability(
                "FABRIC_ID",
                3L,
                Set.of(),
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.error(new AggregateVersionOutdatedError(
                AggregateType.of("TEST"),
                AggregateId.of("TEST_ID"),
                Version.zero()
        )));

        // when: posting the request
        var exchange = client.post()
                .uri("/fabrics/FABRIC_ID/update/availability")
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
                .uri("/fabrics/FABRIC_ID/update/availability")
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldNotAllowAccessWithInvalidToken() {
        // when: posting the request with an invalid token
        var exchange = client.post()
                .uri("/fabrics/FABRIC_ID/update/availability")
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithBadRequestOnInvalidRequest() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: a request to update the availability of a fabric
        var invalidAvailability = new FabricTypeAvailabilityDTO();
        invalidAvailability.typeId = "";
        invalidAvailability.inStock = true;

        var request = new UpdateFabricAvailabilityRequest();
        request.version = 3L;
        request.availability = Set.of(invalidAvailability);

        // and: the module is configured to return an illegal argument exception
        when(module.updateFabricAvailability(
                "FABRIC_ID",
                3L,
                Set.of(invalidAvailability),
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.error(new IllegalArgumentException("Invalid request")));

        // when: posting the request
        var exchange = client.post()
                .uri("/fabrics/FABRIC_ID/update/availability")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is bad request
        exchange.expectStatus().isBadRequest();
    }

    @Test
    void shouldRespondWithPreconditionFailedOnMissingFabricTypesError() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: a request to update the availability of a fabric
        var missingAvailability = new FabricTypeAvailabilityDTO();
        missingAvailability.typeId = "MISSING_FABRIY_TYPE_ID";
        missingAvailability.inStock = true;

        var request = new UpdateFabricAvailabilityRequest();
        request.version = 3L;
        request.availability = Set.of(missingAvailability);

        // and: the module is configured to return a missing fabric types error
        when(module.updateFabricAvailability(
                "FABRIC_ID",
                3L,
                Set.of(missingAvailability),
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.error(new FabricTypesMissingError(Set.of(FabricTypeId.of("MISSING_FABRIC_TYPE_ID")))));

        // when: posting the request
        var exchange = client.post()
                .uri("/fabrics/FABRIC_ID/update/availability")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is precondition failed
        exchange.expectStatus().isEqualTo(412);
    }

}
