package de.bennyboer.kicherkrabbe.credentials.http;

import de.bennyboer.kicherkrabbe.credentials.CredentialsModule;
import de.bennyboer.kicherkrabbe.credentials.http.api.requests.UseCredentialsRequest;
import de.bennyboer.kicherkrabbe.credentials.http.api.responses.UseCredentialsResponse;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class UseCredentialsHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyUseCredentials() {
        // given: a request to use credentials
        var request = new UseCredentialsRequest();
        request.name = "Username";
        request.password = "Password";

        // and: the auth module is configured to return a successful response
        when(module.useCredentials(
                request.name,
                request.password,
                Agent.anonymous()
        )).thenReturn(Mono.just(CredentialsModule.UseCredentialsResult.of("Token")));

        // when: using the credentials
        var exchange = client.post()
                .uri("/api/credentials/use")
                .bodyValue(request)
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the token
        var response = exchange.expectBody(UseCredentialsResponse.class)
                .returnResult()
                .getResponseBody();
        assertThat(response.token).isEqualTo("Token");
    }

    @Test
    void shouldFailToUseCredentialsOnEmptyResult() {
        // given: a request to use credentials
        var request = new UseCredentialsRequest();
        request.name = "Username";
        request.password = "Password";

        // and: the auth module is configured to return an empty result
        when(module.useCredentials(
                request.name,
                request.password,
                Agent.anonymous()
        )).thenReturn(Mono.empty());

        // when: using the credentials
        var exchange = client.post()
                .uri("/api/credentials/use")
                .bodyValue(request)
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();

        // and: the response is empty
        exchange.expectBody().isEmpty();
    }

    @Test
    void shouldFailToUseCredentialsOnErrorResult() {
        // given: a request to use credentials
        var request = new UseCredentialsRequest();
        request.name = "Username";
        request.password = "Password";

        // and: the auth module is configured to return an error
        when(module.useCredentials(
                request.name,
                request.password,
                Agent.anonymous()
        )).thenReturn(Mono.error(Exception::new));

        // when: using the credentials
        var exchange = client.post()
                .uri("/api/credentials/use")
                .bodyValue(request)
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();

        // and: the response is empty
        exchange.expectBody().isEmpty();
    }

}
