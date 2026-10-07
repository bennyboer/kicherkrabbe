package de.bennyboer.kicherkrabbe.embroideries.http;

import de.bennyboer.kicherkrabbe.embroideries.EmbroideryAlias;
import de.bennyboer.kicherkrabbe.embroideries.EmbroideryCategory;
import de.bennyboer.kicherkrabbe.embroideries.EmbroideryCategoryId;
import de.bennyboer.kicherkrabbe.embroideries.EmbroideryCategoryName;
import de.bennyboer.kicherkrabbe.embroideries.EmbroideryId;
import de.bennyboer.kicherkrabbe.embroideries.EmbroideryName;
import de.bennyboer.kicherkrabbe.embroideries.ImageId;
import de.bennyboer.kicherkrabbe.embroideries.PublishedEmbroideriesPage;
import de.bennyboer.kicherkrabbe.embroideries.PublishedEmbroidery;
import de.bennyboer.kicherkrabbe.embroideries.http.api.EmbroideriesSortDTO;
import de.bennyboer.kicherkrabbe.embroideries.http.api.requests.QueryPublishedEmbroideriesRequest;
import de.bennyboer.kicherkrabbe.embroideries.http.api.responses.QueryCategoriesResponse;
import de.bennyboer.kicherkrabbe.embroideries.http.api.responses.QueryFeaturedEmbroideriesResponse;
import de.bennyboer.kicherkrabbe.embroideries.http.api.responses.QueryPublishedEmbroideriesResponse;
import de.bennyboer.kicherkrabbe.embroideries.http.api.responses.QueryPublishedEmbroideryResponse;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

import static de.bennyboer.kicherkrabbe.embroideries.http.api.EmbroideriesSortDirectionDTO.DESCENDING;
import static de.bennyboer.kicherkrabbe.embroideries.http.api.EmbroideriesSortPropertyDTO.ALPHABETICAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class QueryPublishedEmbroideriesHttpHandlerTest extends HttpHandlerTest {

    private final PublishedEmbroidery crab = PublishedEmbroidery.of(
            EmbroideryId.of("CRAB_ID"),
            EmbroideryName.of("Crab"),
            EmbroideryAlias.of("crab"),
            ImageId.of("CRAB_IMAGE_ID"),
            Set.of(EmbroideryCategoryId.of("ANIMALS_ID"))
    );

    private final PublishedEmbroidery fox = PublishedEmbroidery.of(
            EmbroideryId.of("FOX_ID"),
            EmbroideryName.of("Fox"),
            EmbroideryAlias.of("fox"),
            ImageId.of("FOX_IMAGE_ID"),
            Set.of()
    );

    @Test
    void shouldQueryPublishedEmbroideriesAsAnonymousUser() {
        // given: a request to query published embroideries sorted descending
        var request = new QueryPublishedEmbroideriesRequest();
        request.searchTerm = "c";
        request.categories = Set.of("ANIMALS_ID");
        request.sort = new EmbroideriesSortDTO();
        request.sort.property = ALPHABETICAL;
        request.sort.direction = DESCENDING;
        request.skip = 0;
        request.limit = 10;

        // and: the module is configured to return a page of published embroideries
        when(module.getPublishedEmbroideries("c", Set.of("ANIMALS_ID"), false, 0, 10, Agent.anonymous()))
                .thenReturn(Mono.just(PublishedEmbroideriesPage.of(0, 10, 1, List.of(crab))));

        // when: posting the request without a token
        var exchange = client.post()
                .uri("/embroideries/published")
                .bodyValue(request)
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the published embroideries
        exchange.expectBody(QueryPublishedEmbroideriesResponse.class).value(response -> {
            assertThat(response.total).isEqualTo(1);
            assertThat(response.embroideries).hasSize(1);

            var embroidery = response.embroideries.getFirst();
            assertThat(embroidery.id).isEqualTo("CRAB_ID");
            assertThat(embroidery.name).isEqualTo("Crab");
            assertThat(embroidery.alias).isEqualTo("crab");
            assertThat(embroidery.image).isEqualTo("CRAB_IMAGE_ID");
            assertThat(embroidery.categories).containsExactly("ANIMALS_ID");
        });
    }

    @Test
    void shouldDefaultToAscendingOrderAndNoFiltersWhenNotGiven() {
        // given: a request without search term, categories and sort
        var request = new QueryPublishedEmbroideriesRequest();
        request.skip = 0;
        request.limit = 10;

        // and: the module is configured to return all published embroideries in ascending order
        when(module.getPublishedEmbroideries("", Set.of(), true, 0, 10, Agent.anonymous()))
                .thenReturn(Mono.just(PublishedEmbroideriesPage.of(0, 10, 2, List.of(crab, fox))));

        // when: posting the request without a token
        var exchange = client.post()
                .uri("/embroideries/published")
                .bodyValue(request)
                .exchange();

        // then: the response is successful and contains all published embroideries
        exchange.expectStatus().isOk();
        exchange.expectBody(QueryPublishedEmbroideriesResponse.class)
                .value(response -> assertThat(response.embroideries).extracting(e -> e.id)
                        .containsExactly("CRAB_ID", "FOX_ID"));
    }

    @Test
    void shouldQueryPublishedEmbroideryByIdOrAliasAsAnonymousUser() {
        // given: the module is configured to return the published embroidery by alias
        when(module.getPublishedEmbroidery("crab", Agent.anonymous())).thenReturn(Mono.just(crab));

        // when: requesting the published embroidery without a token
        var exchange = client.get()
                .uri("/embroideries/crab/published")
                .exchange();

        // then: the response is successful and contains the embroidery
        exchange.expectStatus().isOk();
        exchange.expectBody(QueryPublishedEmbroideryResponse.class)
                .value(response -> assertThat(response.embroidery.id).isEqualTo("CRAB_ID"));
    }

    @Test
    void shouldRespondWith404WhenPublishedEmbroideryIsNotAvailable() {
        // given: the module is configured to return nothing
        when(module.getPublishedEmbroidery("unknown", Agent.anonymous())).thenReturn(Mono.empty());

        // when: requesting the published embroidery without a token
        var exchange = client.get()
                .uri("/embroideries/unknown/published")
                .exchange();

        // then: the response is not found
        exchange.expectStatus().isNotFound();
    }

    @Test
    void shouldQueryFeaturedEmbroideriesAsAnonymousUser() {
        // given: the module is configured to return the featured embroideries
        when(module.getFeaturedEmbroideries(Agent.anonymous())).thenReturn(Flux.just(crab, fox));

        // when: requesting the featured embroideries without a token
        var exchange = client.get()
                .uri("/embroideries/featured")
                .exchange();

        // then: the response is successful and contains the featured embroideries in order
        exchange.expectStatus().isOk();
        exchange.expectBody(QueryFeaturedEmbroideriesResponse.class)
                .value(response -> assertThat(response.embroideries).extracting(e -> e.id)
                        .containsExactly("CRAB_ID", "FOX_ID"));
    }

    @Test
    void shouldShuffleFeaturedEmbroideriesDeterministicallyGivenASeed() {
        // given: the module is configured to return the featured embroideries
        when(module.getFeaturedEmbroideries(Agent.anonymous())).thenReturn(Flux.just(crab, fox));

        // when: requesting the featured embroideries twice with the same seed
        var first = client.get()
                .uri("/embroideries/featured?seed=42")
                .exchange()
                .expectStatus().isOk()
                .expectBody(QueryFeaturedEmbroideriesResponse.class)
                .returnResult()
                .getResponseBody();
        var second = client.get()
                .uri("/embroideries/featured?seed=42")
                .exchange()
                .expectStatus().isOk()
                .expectBody(QueryFeaturedEmbroideriesResponse.class)
                .returnResult()
                .getResponseBody();

        // then: both responses contain all featured embroideries in the same order
        assertThat(first.embroideries).extracting(e -> e.id).containsExactlyInAnyOrder("CRAB_ID", "FOX_ID");
        assertThat(second.embroideries).isEqualTo(first.embroideries);
    }

    @Test
    void shouldQueryCategoriesUsedInEmbroideriesAsAnonymousUser() {
        // given: the module is configured to return the used categories
        when(module.getCategoriesUsedInEmbroideries(Agent.anonymous())).thenReturn(Flux.just(
                EmbroideryCategory.of(EmbroideryCategoryId.of("ANIMALS_ID"), EmbroideryCategoryName.of("Animals"))
        ));

        // when: requesting the used categories without a token
        var exchange = client.get()
                .uri("/embroideries/categories/used")
                .exchange();

        // then: the response is successful and contains the categories
        exchange.expectStatus().isOk();
        exchange.expectBody(QueryCategoriesResponse.class).value(response -> {
            assertThat(response.categories).hasSize(1);
            assertThat(response.categories.getFirst().id).isEqualTo("ANIMALS_ID");
            assertThat(response.categories.getFirst().name).isEqualTo("Animals");
        });
    }

    @Test
    void shouldStillRequireAuthenticationForAvailableCategories() {
        // when: requesting the available categories without a token
        var exchange = client.get()
                .uri("/embroideries/categories")
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

}
