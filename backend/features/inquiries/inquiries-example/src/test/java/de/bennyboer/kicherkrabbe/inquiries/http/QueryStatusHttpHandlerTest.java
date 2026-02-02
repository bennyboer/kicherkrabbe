package de.bennyboer.kicherkrabbe.inquiries.http;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.inquiries.api.responses.QueryStatusResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class QueryStatusHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyQueryEnabledStatus() {
        // given: the module is configured to return a successful response
        var response = new QueryStatusResponse();
        response.enabled = true;
        when(module.getStatus(eq(Agent.anonymous()))).thenReturn(Mono.just(response));

        // when: posting the request
        var exchange = client.get()
                .uri("/inquiries/status")
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the expected enabled status
        exchange.expectBody().jsonPath("$.enabled").isEqualTo(true);
    }

    @Test
    void shouldSuccessfullyQueryEnabledStatus2() {
        // given: the module is configured to return a successful response
        var response = new QueryStatusResponse();
        response.enabled = false;
        when(module.getStatus(eq(Agent.anonymous()))).thenReturn(Mono.just(response));

        // when: posting the request
        var exchange = client.get()
                .uri("/inquiries/status")
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the expected enabled status
        exchange.expectBody().jsonPath("$.enabled").isEqualTo(false);
    }

}
