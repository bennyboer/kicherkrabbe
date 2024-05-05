package de.bennyboer.kicherkrabbe.users.http;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.users.*;
import de.bennyboer.kicherkrabbe.users.http.responses.UserDetailsResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class GetLoggedInUserDetailsHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldGetLoggedInUserDetails() {
        var userDetails = UserDetails.of(
                UserId.of("USER_ID"),
                FullName.of(
                        FirstName.of("Max"),
                        LastName.of("Mustermann")
                ),
                Mail.of("max.mustermann@kicherkrabbe.com")
        );

        when(module.getUserDetails(
                eq("USER_ID"),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.just(userDetails));

        // given: "a valid token for the logged in user"
        var token = generateTokenForUser("USER_ID");

        // when: "fetching the user details for the logged in user"
        var exchange = client.get()
                .uri("/api/users/me")
                .header("Authorization", "Bearer " + token)
                .exchange();

        // then: the response is OK
        exchange.expectStatus().isOk();

        // and: the response contains the user details
        var expectedResponse = new UserDetailsResponse();
        expectedResponse.userId = "USER_ID";
        expectedResponse.firstName = "Max";
        expectedResponse.lastName = "Mustermann";
        expectedResponse.mail = "max.mustermann@kicherkrabbe.com";

        exchange.expectBody(UserDetailsResponse.class).isEqualTo(expectedResponse);
    }

    @Test
    void shouldFailToGetLoggedInUserDetailsWhenTokenIsInvalid() {
        // when: "fetching the user details for the logged in user without token"
        var exchange = client.get()
                .uri("/api/users/me")
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();

        // and: the response is empty
        exchange.expectBody().isEmpty();
    }

}
