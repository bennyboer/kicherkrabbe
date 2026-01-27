package de.bennyboer.kicherkrabbe.categories.http;

import de.bennyboer.kicherkrabbe.categories.http.api.CategoryGroupDTO;
import de.bennyboer.kicherkrabbe.categories.http.api.requests.CreateCategoryRequest;
import de.bennyboer.kicherkrabbe.categories.http.api.responses.CreateCategoryResponse;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static de.bennyboer.kicherkrabbe.categories.CategoryGroup.CLOTHING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class CreateCategoryHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyCreateCategory() {
        // given: a request to create a category
        var request = new CreateCategoryRequest();
        request.name = "Dress";
        request.group = CategoryGroupDTO.CLOTHING;

        // and: having a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a successful response
        when(module.createCategory(
                request.name,
                CLOTHING,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.just("CATEGORY_ID"));

        // when: posting the request
        var exchange = client.post()
                .uri("/api/categories/create")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the ID of the new category
        var response = exchange.expectBody(CreateCategoryResponse.class)
                .returnResult()
                .getResponseBody();
        assertThat(response.id).isEqualTo("CATEGORY_ID");
    }

    @Test
    void shouldNotAllowUnauthorizedAccess() {
        // given: a request to create a category
        var request = new CreateCategoryRequest();
        request.name = "Skirt";
        request.group = CategoryGroupDTO.CLOTHING;

        // when: posting the request without a token
        var exchange = client.post()
                .uri("/api/categories/create")
                .bodyValue(request)
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldNotAllowAccessWithInvalidToken() {
        // given: a request to create a category
        var request = new CreateCategoryRequest();
        request.name = "Skirt";
        request.group = CategoryGroupDTO.CLOTHING;

        // when: posting the request with an invalid token
        var exchange = client.post()
                .uri("/api/categories/create")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithBadRequestOnIllegalRequest() {
        // given: a request to create a category with an illegal name
        var request = new CreateCategoryRequest();
        request.name = " ";
        request.group = CategoryGroupDTO.CLOTHING;

        // and: having a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return an illegal argument exception
        when(module.createCategory(
                request.name,
                CLOTHING,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.error(new IllegalArgumentException("Illegal name")));

        // when: posting the request
        var exchange = client.post()
                .uri("/api/categories/create")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is bad request
        exchange.expectStatus().isBadRequest();
    }

}
