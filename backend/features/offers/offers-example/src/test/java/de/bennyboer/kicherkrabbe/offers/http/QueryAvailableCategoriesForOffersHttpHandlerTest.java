package de.bennyboer.kicherkrabbe.offers.http;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.offers.OfferCategory;
import de.bennyboer.kicherkrabbe.offers.OfferCategoryId;
import de.bennyboer.kicherkrabbe.offers.OfferCategoryName;
import de.bennyboer.kicherkrabbe.offers.api.responses.QueryAvailableCategoriesForOffersResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class QueryAvailableCategoriesForOffersHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyQueryAvailableCategories() {
        when(module.getAvailableCategoriesForOffers(any(Agent.class))).thenReturn(Flux.just(
                OfferCategory.of(OfferCategoryId.of("CAT_1"), OfferCategoryName.of("T-Shirts")),
                OfferCategory.of(OfferCategoryId.of("CAT_2"), OfferCategoryName.of("Hoodies"))
        ));

        var exchange = client.get()
                .uri("/offers/categories")
                .exchange();

        exchange.expectStatus().isOk();
        var response = exchange.expectBody(QueryAvailableCategoriesForOffersResponse.class)
                .returnResult()
                .getResponseBody();
        assertThat(response.categories).hasSize(2);
        assertThat(response.categories.get(0).id).isEqualTo("CAT_1");
        assertThat(response.categories.get(0).name).isEqualTo("T-Shirts");
        assertThat(response.categories.get(1).id).isEqualTo("CAT_2");
        assertThat(response.categories.get(1).name).isEqualTo("Hoodies");
    }

    @Test
    void shouldAllowAnonymousAccess() {
        when(module.getAvailableCategoriesForOffers(any(Agent.class))).thenReturn(Flux.empty());

        var exchange = client.get()
                .uri("/offers/categories")
                .exchange();

        exchange.expectStatus().isOk();
    }

}
