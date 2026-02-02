package de.bennyboer.kicherkrabbe.categories.http;

import de.bennyboer.kicherkrabbe.categories.CategoriesPage;
import de.bennyboer.kicherkrabbe.categories.CategoryDetails;
import de.bennyboer.kicherkrabbe.categories.CategoryId;
import de.bennyboer.kicherkrabbe.categories.CategoryName;
import de.bennyboer.kicherkrabbe.categories.http.api.CategoryGroupDTO;
import de.bennyboer.kicherkrabbe.categories.http.api.responses.QueryCategoriesResponse;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

import static de.bennyboer.kicherkrabbe.categories.CategoryGroup.CLOTHING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class GetCategoriesByGroupHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyGetCategoriesByGroup() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a successful response
        var resultingPage = CategoriesPage.of(
                2L,
                8L,
                4L,
                List.of(
                        CategoryDetails.of(
                                CategoryId.of("CATEGORY_ID_1"),
                                Version.zero(),
                                CategoryName.of("Top"),
                                CLOTHING,
                                Instant.parse("2024-03-18T11:25:00Z")
                        ),
                        CategoryDetails.of(
                                CategoryId.of("CATEGORY_ID_2"),
                                Version.zero(),
                                CategoryName.of("Dress"),
                                CLOTHING,
                                Instant.parse("2024-03-12T12:30:00Z")
                        )
                )
        );
        when(module.getCategoriesByGroup(
                CLOTHING,
                "term",
                2L,
                8L,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.just(resultingPage));

        // when: posting the request
        var exchange = client.get()
                .uri("/categories/groups/CLOTHING?searchTerm=term&skip=2&limit=8")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the categories
        var response = exchange.expectBody(QueryCategoriesResponse.class)
                .returnResult()
                .getResponseBody();
        assertThat(response.skip).isEqualTo(2L);
        assertThat(response.limit).isEqualTo(8L);
        assertThat(response.total).isEqualTo(4L);
        assertThat(response.categories).hasSize(2);
        var actualCategories = response.categories;
        assertThat(actualCategories.get(0).id).isEqualTo("CATEGORY_ID_1");
        assertThat(actualCategories.get(0).version).isEqualTo(0);
        assertThat(actualCategories.get(0).name).isEqualTo("Top");
        assertThat(actualCategories.get(0).group).isEqualTo(CategoryGroupDTO.CLOTHING);
        assertThat(actualCategories.get(0).createdAt).isEqualTo("2024-03-18T11:25:00Z");
        assertThat(actualCategories.get(1).id).isEqualTo("CATEGORY_ID_2");
        assertThat(actualCategories.get(1).version).isEqualTo(0);
        assertThat(actualCategories.get(1).name).isEqualTo("Dress");
        assertThat(actualCategories.get(1).group).isEqualTo(CategoryGroupDTO.CLOTHING);
        assertThat(actualCategories.get(1).createdAt).isEqualTo("2024-03-12T12:30:00Z");
    }

    @Test
    void shouldNotAllowUnauthorizedAccess() {
        // when: posting the request without a token
        var exchange = client.get()
                .uri("/categories/groups/CLOTHING?searchTerm=term&skip=2&limit=8")
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldNotAllowAccessWithInvalidToken() {
        // when: posting the request with an invalid token
        var exchange = client.get()
                .uri("/categories/groups/CLOTHING?searchTerm=term&skip=2&limit=8")
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

}
