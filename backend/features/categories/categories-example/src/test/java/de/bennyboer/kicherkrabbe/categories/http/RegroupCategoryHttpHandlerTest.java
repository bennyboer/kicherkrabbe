package de.bennyboer.kicherkrabbe.categories.http;

import de.bennyboer.kicherkrabbe.categories.http.api.CategoryGroupDTO;
import de.bennyboer.kicherkrabbe.categories.http.api.requests.RegroupCategoryRequest;
import de.bennyboer.kicherkrabbe.categories.http.api.responses.RegroupCategoryResponse;
import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static de.bennyboer.kicherkrabbe.categories.CategoryGroup.CLOTHING;
import static de.bennyboer.kicherkrabbe.categories.CategoryGroup.NONE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class RegroupCategoryHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyRegroupCategory() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: a request to regroup a category
        var request = new RegroupCategoryRequest();
        request.group = CategoryGroupDTO.NONE;
        request.version = 2;

        // and: the module is configured to return a successful response
        when(module.regroupCategory(
                "CATEGORY_ID",
                2L,
                NONE,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.just(3L));

        // when: posting the request
        var exchange = client.post()
                .uri("/categories/CATEGORY_ID/regroup")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the version of the regrouped category
        var response = exchange.expectBody(RegroupCategoryResponse.class)
                .returnResult()
                .getResponseBody();
        assertThat(response.version).isEqualTo(3L);
    }

    @Test
    void shouldNotAllowUnauthorizedAccess() {
        // given: a request to regroup a category
        var request = new RegroupCategoryRequest();
        request.group = CategoryGroupDTO.CLOTHING;
        request.version = 2;

        // when: posting the request without a token
        var exchange = client.post()
                .uri("/categories/CATEGORY_ID/regroup")
                .bodyValue(request)
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldNotAllowAccessWithInvalidToken() {
        // when: posting the request with an invalid token
        var exchange = client.delete()
                .uri("/categories/CATEGORY_ID/regroup")
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWith409ConflictIfVersionIsOutdated() {
        // given: a request to regroup a category
        var request = new RegroupCategoryRequest();
        request.group = CategoryGroupDTO.CLOTHING;
        request.version = 2;

        // and: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a conflict response
        when(module.regroupCategory(
                "CATEGORY_ID",
                2L,
                CLOTHING,
                Agent.user(AgentId.of("USER_ID"))
        )).thenReturn(Mono.error(new AggregateVersionOutdatedError(
                AggregateType.of("CATEGORY"),
                AggregateId.of("CATEGORY_ID"),
                Version.of(2L)
        )));

        // when: posting the request
        var exchange = client.post()
                .uri("/categories/CATEGORY_ID/regroup")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is a conflict
        exchange.expectStatus().isEqualTo(409);
    }

    @Test
    void shouldRespondWith400BadRequestOnIllegalRequest() {
        // given: a request to regroup a category with an illegal name
        var request = new RegroupCategoryRequest();
        request.version = 2;

        // and: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // when: posting the request
        var exchange = client.post()
                .uri("/categories/CATEGORY_ID/regroup")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is a bad request
        exchange.expectStatus().isBadRequest();
    }

}
