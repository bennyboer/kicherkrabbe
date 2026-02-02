package de.bennyboer.kicherkrabbe.categories.http;

import de.bennyboer.kicherkrabbe.categories.CategoryDetails;
import de.bennyboer.kicherkrabbe.categories.CategoryId;
import de.bennyboer.kicherkrabbe.categories.CategoryName;
import de.bennyboer.kicherkrabbe.categories.http.api.CategoryDTO;
import de.bennyboer.kicherkrabbe.categories.http.api.CategoryGroupDTO;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.Instant;

import static de.bennyboer.kicherkrabbe.categories.CategoryGroup.CLOTHING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class GetCategoryHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyGetCategory() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a successful response
        when(module.getCategory(
                "CATEGORY_ID",
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.just(CategoryDetails.of(
                CategoryId.of("CATEGORY_ID"),
                Version.zero(),
                CategoryName.of("Top"),
                CLOTHING,
                Instant.parse("2024-03-18T11:25:00Z")
        )));

        // when: posting the request
        var exchange = client.get()
                .uri("/categories/CATEGORY_ID")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the category
        var response = exchange.expectBody(CategoryDTO.class)
                .returnResult()
                .getResponseBody();
        assertThat(response.id).isEqualTo("CATEGORY_ID");
        assertThat(response.version).isEqualTo(0L);
        assertThat(response.name).isEqualTo("Top");
        assertThat(response.group).isEqualTo(CategoryGroupDTO.CLOTHING);
        assertThat(response.createdAt).isEqualTo(Instant.parse("2024-03-18T11:25:00Z"));
    }

    @Test
    void shouldNotAllowUnauthorizedAccess() {
        // when: posting the request without a token
        var exchange = client.get()
                .uri("/categories/CATEGORY_ID")
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldNotAllowAccessWithInvalidToken() {
        // when: posting the request with an invalid token
        var exchange = client.get()
                .uri("/categories/CATEGORY_ID")
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

}
