package de.bennyboer.kicherkrabbe.assets.http;

import de.bennyboer.kicherkrabbe.assets.AssetDetails;
import de.bennyboer.kicherkrabbe.assets.AssetId;
import de.bennyboer.kicherkrabbe.assets.AssetsPage;
import de.bennyboer.kicherkrabbe.assets.ContentType;
import de.bennyboer.kicherkrabbe.assets.http.api.requests.QueryAssetsRequest;
import de.bennyboer.kicherkrabbe.assets.http.api.responses.QueryAssetsResponse;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class QueryAssetsHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyQueryAssets() {
        var token = createTokenForUser("USER_ID");

        when(module.getAssets(
                eq(""),
                eq(Set.of()),
                eq("CREATED_AT"),
                eq("DESCENDING"),
                eq(0L),
                eq(30L),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.just(AssetsPage.of(
                0,
                30,
                1,
                List.of(AssetDetails.of(
                        AssetId.of("ASSET_ID"),
                        Version.of(0),
                        ContentType.of("image/jpeg"),
                        1024,
                        Instant.parse("2024-01-01T00:00:00Z"),
                        List.of()
                ))
        )));

        var request = new QueryAssetsRequest();
        request.searchTerm = "";
        request.contentTypes = Set.of();
        request.sortProperty = "CREATED_AT";
        request.sortDirection = "DESCENDING";
        request.skip = 0;
        request.limit = 30;

        var exchange = client.post()
                .uri("/assets/")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        exchange.expectStatus().isOk();

        var response = exchange.expectBody(QueryAssetsResponse.class)
                .returnResult()
                .getResponseBody();
        assertThat(response.total).isEqualTo(1);
        assertThat(response.assets).hasSize(1);
        assertThat(response.assets.getFirst().id).isEqualTo("ASSET_ID");
        assertThat(response.assets.getFirst().contentType).isEqualTo("image/jpeg");
        assertThat(response.assets.getFirst().fileSize).isEqualTo(1024);
    }

    @Test
    void shouldNotAllowUnauthorizedAccessToQueryAssets() {
        var request = new QueryAssetsRequest();
        request.skip = 0;
        request.limit = 30;

        var exchange = client.post()
                .uri("/assets/")
                .bodyValue(request)
                .exchange();

        exchange.expectStatus().isUnauthorized();
    }

}
