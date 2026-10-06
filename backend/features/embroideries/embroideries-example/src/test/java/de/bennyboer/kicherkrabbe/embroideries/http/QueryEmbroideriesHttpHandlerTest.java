package de.bennyboer.kicherkrabbe.embroideries.http;

import de.bennyboer.kicherkrabbe.embroideries.EmbroideriesPage;
import de.bennyboer.kicherkrabbe.embroideries.EmbroideryCategory;
import de.bennyboer.kicherkrabbe.embroideries.EmbroideryCategoryId;
import de.bennyboer.kicherkrabbe.embroideries.EmbroideryCategoryName;
import de.bennyboer.kicherkrabbe.embroideries.EmbroideryDetails;
import de.bennyboer.kicherkrabbe.embroideries.EmbroideryId;
import de.bennyboer.kicherkrabbe.embroideries.EmbroideryName;
import de.bennyboer.kicherkrabbe.embroideries.EmbroideryNotFoundError;
import de.bennyboer.kicherkrabbe.embroideries.ImageId;
import de.bennyboer.kicherkrabbe.embroideries.http.api.requests.QueryEmbroideriesRequest;
import de.bennyboer.kicherkrabbe.embroideries.http.api.responses.QueryCategoriesResponse;
import de.bennyboer.kicherkrabbe.embroideries.http.api.responses.QueryEmbroideriesResponse;
import de.bennyboer.kicherkrabbe.embroideries.http.api.responses.QueryEmbroideryResponse;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class QueryEmbroideriesHttpHandlerTest extends HttpHandlerTest {

    private final EmbroideryDetails sampleEmbroidery = EmbroideryDetails.of(
            EmbroideryId.of("EMBROIDERY_ID"),
            Version.of(3),
            true,
            false,
            EmbroideryName.of("Crab"),
            ImageId.of("IMAGE_ID"),
            Set.of(EmbroideryCategoryId.of("ANIMALS_ID")),
            Instant.parse("2024-03-12T12:30:00.00Z")
    );

    @Test
    void shouldSuccessfullyQueryEmbroideries() {
        // given: a request to query embroideries
        var request = new QueryEmbroideriesRequest();
        request.searchTerm = "cr";
        request.categories = Set.of("ANIMALS_ID");
        request.skip = 0;
        request.limit = 10;

        // and: having a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a page of embroideries
        when(module.getEmbroideries(
                "cr",
                Set.of("ANIMALS_ID"),
                0,
                10,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.just(EmbroideriesPage.of(0, 10, 1, List.of(sampleEmbroidery))));

        // when: posting the request
        var exchange = client.post()
                .uri("/embroideries")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the embroideries
        exchange.expectBody(QueryEmbroideriesResponse.class).value(response -> {
            assertThat(response.total).isEqualTo(1);
            assertThat(response.skip).isEqualTo(0);
            assertThat(response.limit).isEqualTo(10);
            assertThat(response.embroideries).hasSize(1);

            var embroidery = response.embroideries.getFirst();
            assertThat(embroidery.id).isEqualTo("EMBROIDERY_ID");
            assertThat(embroidery.version).isEqualTo(3L);
            assertThat(embroidery.published).isTrue();
            assertThat(embroidery.featured).isFalse();
            assertThat(embroidery.name).isEqualTo("Crab");
            assertThat(embroidery.image).isEqualTo("IMAGE_ID");
            assertThat(embroidery.categories).containsExactly("ANIMALS_ID");
            assertThat(embroidery.createdAt).isEqualTo(Instant.parse("2024-03-12T12:30:00.00Z"));
        });
    }

    @Test
    void shouldSuccessfullyQuerySingleEmbroidery() {
        // given: having a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return the embroidery
        when(module.getEmbroidery("EMBROIDERY_ID", Agent.user(AgentId.of("USER_ID"))))
                .thenReturn(Mono.just(sampleEmbroidery));

        // when: requesting the embroidery
        var exchange = client.get()
                .uri("/embroideries/EMBROIDERY_ID")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the embroidery
        exchange.expectBody(QueryEmbroideryResponse.class)
                .value(response -> assertThat(response.embroidery.name).isEqualTo("Crab"));
    }

    @Test
    void shouldRespondWith404WhenEmbroideryIsNotFound() {
        // given: having a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to raise a not found error
        when(module.getEmbroidery("EMBROIDERY_ID", Agent.user(AgentId.of("USER_ID"))))
                .thenReturn(Mono.error(new EmbroideryNotFoundError(EmbroideryId.of("EMBROIDERY_ID"))));

        // when: requesting the embroidery
        var exchange = client.get()
                .uri("/embroideries/EMBROIDERY_ID")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is not found
        exchange.expectStatus().isNotFound();
    }

    @Test
    void shouldSuccessfullyQueryAvailableCategories() {
        // given: having a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return the available categories
        when(module.getAvailableCategoriesForEmbroideries(Agent.user(AgentId.of("USER_ID")))).thenReturn(Flux.just(
                EmbroideryCategory.of(EmbroideryCategoryId.of("ANIMALS_ID"), EmbroideryCategoryName.of("Animals"))
        ));

        // when: requesting the available categories
        var exchange = client.get()
                .uri("/embroideries/categories")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the categories
        exchange.expectBody(QueryCategoriesResponse.class).value(response -> {
            assertThat(response.categories).hasSize(1);
            assertThat(response.categories.getFirst().id).isEqualTo("ANIMALS_ID");
            assertThat(response.categories.getFirst().name).isEqualTo("Animals");
        });
    }

    @Test
    void shouldNotAllowUnauthorizedAccess() {
        // when: querying embroideries without a token
        var exchange = client.post()
                .uri("/embroideries")
                .bodyValue(new QueryEmbroideriesRequest())
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();

        // when: querying a single embroidery without a token; then: the response is unauthorized
        client.get().uri("/embroideries/EMBROIDERY_ID").exchange().expectStatus().isUnauthorized();

        // when: querying the available categories without a token; then: the response is unauthorized
        client.get().uri("/embroideries/categories").exchange().expectStatus().isUnauthorized();
    }

}
