package de.bennyboer.kicherkrabbe.assets.http;

import de.bennyboer.kicherkrabbe.assets.StorageInfo;
import de.bennyboer.kicherkrabbe.assets.http.api.responses.QueryStorageInfoResponse;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class QueryStorageInfoHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyQueryStorageInfo() {
        var token = createTokenForUser("USER_ID");

        when(module.getStorageInfo(
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.just(StorageInfo.of(1024, 2048)));

        var exchange = client.get()
                .uri("/assets/storage-info")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        exchange.expectStatus().isOk();

        var response = exchange.expectBody(QueryStorageInfoResponse.class)
                .returnResult()
                .getResponseBody();
        assertThat(response.usedBytes).isEqualTo(1024);
        assertThat(response.limitBytes).isEqualTo(2048);
    }

    @Test
    void shouldNotAllowUnauthorizedAccess() {
        client.get()
                .uri("/assets/storage-info")
                .exchange()
                .expectStatus().isUnauthorized();
    }

}
