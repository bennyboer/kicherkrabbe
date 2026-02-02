package de.bennyboer.kicherkrabbe.inquiries.http;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.inquiries.api.DateRangeDTO;
import de.bennyboer.kicherkrabbe.inquiries.api.RequestStatisticsDTO;
import de.bennyboer.kicherkrabbe.inquiries.api.responses.QueryRequestStatisticsResponse;
import de.bennyboer.kicherkrabbe.permissions.*;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.ArrayList;

import static de.bennyboer.kicherkrabbe.inquiries.Actions.QUERY_STATISTICS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class QueryStatisticsHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyQueryStatistics() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: a request to query statistics
        var from = Instant.parse("2024-03-10T12:30:00.000Z");
        var to = Instant.parse("2024-03-12T13:30:00.000Z");

        // and: the module is configured to return a successful response
        var response = new QueryRequestStatisticsResponse();
        response.statistics = new ArrayList<>();

        var stats1 = new RequestStatisticsDTO();
        stats1.dateRange = new DateRangeDTO();
        stats1.dateRange.from = Instant.parse("2024-03-10T12:30:00.000Z");
        stats1.dateRange.to = Instant.parse("2024-03-11T00:00:00.000Z");
        stats1.totalRequests = 3;
        response.statistics.add(stats1);

        var stats2 = new RequestStatisticsDTO();
        stats2.dateRange = new DateRangeDTO();
        stats2.dateRange.from = Instant.parse("2024-03-11T00:00:00.000Z");
        stats2.dateRange.to = Instant.parse("2024-03-12T00:00:00.000Z");
        stats2.totalRequests = 5;
        response.statistics.add(stats2);

        var stats3 = new RequestStatisticsDTO();
        stats3.dateRange = new DateRangeDTO();
        stats3.dateRange.from = Instant.parse("2024-03-12T00:00:00.000Z");
        stats3.dateRange.to = Instant.parse("2024-03-12T13:30:00.000Z");
        stats3.totalRequests = 8;
        response.statistics.add(stats3);

        when(module.getRequestStatistics(eq(from), eq(to), eq(Agent.user(AgentId.of("USER_ID")))))
                .thenReturn(Mono.just(response));

        // when: posting the request
        var exchange = client.get()
                .uri(uri -> uri.path("/inquiries/statistics")
                        .queryParam("from", from.toString())
                        .queryParam("to", to.toString())
                        .build())
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the expected settings
        var result = exchange.expectBody(QueryRequestStatisticsResponse.class).returnResult().getResponseBody();
        assertThat(result.statistics).hasSize(3);
        assertThat(result.statistics.get(0)).isEqualTo(stats1);
        assertThat(result.statistics.get(1)).isEqualTo(stats2);
        assertThat(result.statistics.get(2)).isEqualTo(stats3);
    }

    @Test
    void shouldSuccessfullyQueryStatisticsWithoutQueryParams() {
        // given: a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: we are currently at 2024-03-12T13:30:00.000Z
        var now = Instant.parse("2024-03-12T13:30:00.000Z");
        setTime(now);

        // and: the module is configured to return a successful response
        var response = new QueryRequestStatisticsResponse();
        response.statistics = new ArrayList<>();

        var stats1 = new RequestStatisticsDTO();
        stats1.dateRange = new DateRangeDTO();
        stats1.dateRange.from = Instant.parse("2024-03-10T12:30:00.000Z");
        stats1.dateRange.to = Instant.parse("2024-03-11T00:00:00.000Z");
        stats1.totalRequests = 3;
        response.statistics.add(stats1);

        var stats2 = new RequestStatisticsDTO();
        stats2.dateRange = new DateRangeDTO();
        stats2.dateRange.from = Instant.parse("2024-03-11T00:00:00.000Z");
        stats2.dateRange.to = Instant.parse("2024-03-12T00:00:00.000Z");
        stats2.totalRequests = 5;
        response.statistics.add(stats2);

        var stats3 = new RequestStatisticsDTO();
        stats3.dateRange = new DateRangeDTO();
        stats3.dateRange.from = Instant.parse("2024-03-12T00:00:00.000Z");
        stats3.dateRange.to = Instant.parse("2024-03-12T13:30:00.000Z");
        stats3.totalRequests = 8;
        response.statistics.add(stats3);

        when(module.getRequestStatistics(
                eq(Instant.parse("2024-02-12T00:00:00.000Z")),
                eq(Instant.parse("2024-03-13T00:00:00.000Z")),
                eq(Agent.user(AgentId.of("USER_ID")))
        )).thenReturn(Mono.just(response));

        // when: posting the request
        var exchange = client.get()
                .uri(uri -> uri.path("/inquiries/statistics").build())
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the expected settings
        var result = exchange.expectBody(QueryRequestStatisticsResponse.class).returnResult().getResponseBody();
        assertThat(result.statistics).hasSize(3);
        assertThat(result.statistics.get(0)).isEqualTo(stats1);
        assertThat(result.statistics.get(1)).isEqualTo(stats2);
        assertThat(result.statistics.get(2)).isEqualTo(stats3);
    }

    @Test
    void shouldRespondWithUnauthorizedForAnonymousAccess() {
        // when: posting the request
        var exchange = client.get()
                .uri("/inquiries/statistics")
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondWithUnauthorizedForInvalidToken() {
        // when: posting the request
        var exchange = client.get()
                .uri("/inquiries/statistics")
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
        var from = Instant.parse("2024-03-10T12:30:00.000Z");
        var to = Instant.parse("2024-03-12T13:30:00.000Z");

        var response = new QueryRequestStatisticsResponse();
        response.statistics = new ArrayList<>();

        var stats1 = new RequestStatisticsDTO();
        stats1.dateRange = new DateRangeDTO();
        stats1.dateRange.from = Instant.parse("2024-03-10T12:30:00.000Z");
        stats1.dateRange.to = Instant.parse("2024-03-11T00:00:00.000Z");
        stats1.totalRequests = 3;
        response.statistics.add(stats1);

        var stats2 = new RequestStatisticsDTO();
        stats2.dateRange = new DateRangeDTO();
        stats2.dateRange.from = Instant.parse("2024-03-11T00:00:00.000Z");
        stats2.dateRange.to = Instant.parse("2024-03-12T00:00:00.000Z");
        stats2.totalRequests = 5;
        response.statistics.add(stats2);

        var stats3 = new RequestStatisticsDTO();
        stats3.dateRange = new DateRangeDTO();
        stats3.dateRange.from = Instant.parse("2024-03-12T00:00:00.000Z");
        stats3.dateRange.to = Instant.parse("2024-03-12T13:30:00.000Z");
        stats3.totalRequests = 8;
        response.statistics.add(stats3);

        when(module.getRequestStatistics(eq(from), eq(to), eq(Agent.user(AgentId.of("USER_ID")))))
                .thenReturn(Mono.error(new MissingPermissionError(
                        Permission.builder()
                                .holder(Holder.user(HolderId.of("USER_ID")))
                                .isAllowedTo(QUERY_STATISTICS)
                                .onType(ResourceType.of("INQUIRY"))
                )));

        // when: posting the request
        var exchange = client.get()
                .uri(uri -> uri.path("/inquiries/statistics")
                        .queryParam("from", from.toString())
                        .queryParam("to", to.toString())
                        .build())
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // then: the response is forbidden
        exchange.expectStatus().isForbidden();
    }

}
