package de.bennyboer.kicherkrabbe.patterns.http;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.patterns.PatternCategory;
import de.bennyboer.kicherkrabbe.patterns.PatternCategoryId;
import de.bennyboer.kicherkrabbe.patterns.PatternCategoryName;
import de.bennyboer.kicherkrabbe.patterns.http.api.responses.QueryCategoriesResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class QueryCategoriesUsedInPatternsHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyQueryCategoriesUsedInPatternsAsUser() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a successful response
        when(module.getCategoriesUsedInPatterns(Agent.user(AgentId.of("USER_ID")))).thenReturn(Flux.just(
                PatternCategory.of(PatternCategoryId.of("CATEGORY_ID_1"), PatternCategoryName.of("CATEGORY_NAME_1")),
                PatternCategory.of(PatternCategoryId.of("CATEGORY_ID_2"), PatternCategoryName.of("CATEGORY_NAME_2"))
        ));

        // when: posting the request
        var exchange = client.get()
                .uri("/api/patterns/categories/used")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the categories
        var response = exchange.expectBody(QueryCategoriesResponse.class).returnResult().getResponseBody();
        assertThat(response.categories).hasSize(2);

        var category1 = response.categories.get(0);
        assertThat(category1.id).isEqualTo("CATEGORY_ID_1");
        assertThat(category1.name).isEqualTo("CATEGORY_NAME_1");

        var category2 = response.categories.get(1);
        assertThat(category2.id).isEqualTo("CATEGORY_ID_2");
        assertThat(category2.name).isEqualTo("CATEGORY_NAME_2");
    }

    @Test
    void shouldAllowUnauthorizedAccess() {
        // given: the module is configured to return a successful response
        when(module.getCategoriesUsedInPatterns(Agent.anonymous())).thenReturn(Flux.just(
                PatternCategory.of(PatternCategoryId.of("CATEGORY_ID_1"), PatternCategoryName.of("CATEGORY_NAME_1")),
                PatternCategory.of(PatternCategoryId.of("CATEGORY_ID_2"), PatternCategoryName.of("CATEGORY_NAME_2"))
        ));

        // when: posting the request without a token
        var exchange = client.get()
                .uri("/api/patterns/categories/used")
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the categories
        var response = exchange.expectBody(QueryCategoriesResponse.class).returnResult().getResponseBody();
        assertThat(response.categories).hasSize(2);

        var category1 = response.categories.get(0);
        assertThat(category1.id).isEqualTo("CATEGORY_ID_1");
        assertThat(category1.name).isEqualTo("CATEGORY_NAME_1");

        var category2 = response.categories.get(1);
        assertThat(category2.id).isEqualTo("CATEGORY_ID_2");
        assertThat(category2.name).isEqualTo("CATEGORY_NAME_2");
    }

    @Test
    void shouldNotAllowAccessWithInvalidToken() {
        // when: posting the request with an invalid token
        var exchange = client.get()
                .uri("/api/patterns/categories/used")
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

}
