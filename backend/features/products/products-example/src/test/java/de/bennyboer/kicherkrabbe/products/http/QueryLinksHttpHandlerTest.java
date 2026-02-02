package de.bennyboer.kicherkrabbe.products.http;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.*;
import de.bennyboer.kicherkrabbe.products.Actions;
import de.bennyboer.kicherkrabbe.products.api.LinkDTO;
import de.bennyboer.kicherkrabbe.products.api.LinkTypeDTO;
import de.bennyboer.kicherkrabbe.products.api.responses.QueryLinksResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class QueryLinksHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyQueryLinks() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a successful response
        var response = new QueryLinksResponse();
        response.total = 102;
        response.links = new ArrayList<>();

        var link1 = new LinkDTO();
        link1.type = LinkTypeDTO.PATTERN;
        link1.id = "PATTERN_ID_1";
        link1.name = "Pattern 1";
        response.links.add(link1);

        var link2 = new LinkDTO();
        link2.type = LinkTypeDTO.FABRIC;
        link2.id = "FABRIC_ID_1";
        link2.name = "Fabric 1";
        response.links.add(link2);

        when(module.getLinks(
                eq("Search term"),
                eq(100L),
                eq(300L),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.just(response));

        // when: posting the request
        var exchange = client.get()
                .uri(builder -> builder.path("/products/links")
                        .queryParam("searchTerm", "Search term")
                        .queryParam("skip", "100")
                        .queryParam("limit", "300")
                        .build())
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the expected links
        var result = exchange.expectBody(QueryLinksResponse.class).returnResult().getResponseBody();
        assertThat(result.total).isEqualTo(102);
        assertThat(result.links).hasSize(2);
        assertThat(result.links.get(0)).isEqualTo(link1);
        assertThat(result.links.get(1)).isEqualTo(link2);
    }

    @Test
    void shouldQueryLinksSuccessfullyWithoutQueryParams() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return a successful response
        var response = new QueryLinksResponse();
        response.total = 2;
        response.links = new ArrayList<>();

        var link1 = new LinkDTO();
        link1.type = LinkTypeDTO.PATTERN;
        link1.id = "PATTERN_ID_1";
        link1.name = "Pattern 1";
        response.links.add(link1);

        var link2 = new LinkDTO();
        link2.type = LinkTypeDTO.FABRIC;
        link2.id = "FABRIC_ID_1";
        link2.name = "Fabric 1";
        response.links.add(link2);

        when(module.getLinks(
                eq(""),
                eq(0L),
                eq(100L),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.just(response));

        // when: posting the request
        var exchange = client.get()
                .uri(builder -> builder.path("/products/links").build())
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the expected links
        var result = exchange.expectBody(QueryLinksResponse.class).returnResult().getResponseBody();
        assertThat(result.total).isEqualTo(2);
        assertThat(result.links).hasSize(2);
        assertThat(result.links.get(0)).isEqualTo(link1);
        assertThat(result.links.get(1)).isEqualTo(link2);
    }

    @Test
    void shouldCorrectNegativeSkipAndLimitQueryParams() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: a valid response
        var response = new QueryLinksResponse();
        response.total = 0;
        response.links = List.of();

        // and: the module is configured to return an error
        when(module.getLinks(
                eq(""),
                eq(0L),
                eq(100L),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.just(response));

        // when: posting the request
        var exchange = client.get()
                .uri(builder -> builder.path("/products/links")
                        .queryParam("skip", -100)
                        .queryParam("limit", -100)
                        .build())
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is ok
        exchange.expectStatus().isOk();

        // and: the response contains the expected links
        var result = exchange.expectBody(QueryLinksResponse.class).returnResult().getResponseBody();
        assertThat(result.total).isEqualTo(0);
        assertThat(result.links).isEmpty();
    }

    @Test
    void shouldRespondWithUnauthorizedForAnonymousAccess() {
        // when: posting the request
        var exchange = client.get()
                .uri("/products/links")
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithUnauthorizedForInvalidToken() {
        // when: posting the request
        var exchange = client.get()
                .uri("/products/links")
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithForbiddenForMissingPermission() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return an error
        when(module.getLinks(
                eq(""),
                eq(0L),
                eq(100L),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.error(new MissingPermissionError(
                Permission.builder()
                        .holder(Holder.user(HolderId.of("USER_ID")))
                        .isAllowedTo(Actions.READ)
                        .onType(ResourceType.of("PRODUCT_LINK"))
        )));

        // when: posting the request
        var exchange = client.get()
                .uri("/products/links")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is forbidden
        exchange.expectStatus().isForbidden();
    }

}
