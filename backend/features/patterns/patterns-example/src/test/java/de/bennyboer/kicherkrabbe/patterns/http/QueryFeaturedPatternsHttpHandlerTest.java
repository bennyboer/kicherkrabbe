package de.bennyboer.kicherkrabbe.patterns.http;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.patterns.PatternId;
import de.bennyboer.kicherkrabbe.patterns.PatternName;
import de.bennyboer.kicherkrabbe.patterns.http.api.responses.QueryFeaturedPatternsResponse;
import de.bennyboer.kicherkrabbe.patterns.samples.SamplePublishedPattern;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class QueryFeaturedPatternsHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyQueryFeaturedPatterns() {
        var token = createTokenForUser("USER_ID");

        when(module.getFeaturedPatterns(Agent.user(AgentId.of("USER_ID")))).thenReturn(Flux.just(
                SamplePublishedPattern.builder().build().toModel()
        ));

        var exchange = client.get()
                .uri("/patterns/featured")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        exchange.expectStatus().isOk();

        var response = exchange.expectBody(QueryFeaturedPatternsResponse.class).returnResult().getResponseBody();
        assertThat(response.patterns).hasSize(1);
    }

    @Test
    void shouldAllowUnauthorizedAccess() {
        when(module.getFeaturedPatterns(Agent.anonymous())).thenReturn(Flux.just(
                SamplePublishedPattern.builder().build().toModel()
        ));

        var exchange = client.get()
                .uri("/patterns/featured")
                .exchange();

        exchange.expectStatus().isOk();

        var response = exchange.expectBody(QueryFeaturedPatternsResponse.class).returnResult().getResponseBody();
        assertThat(response.patterns).hasSize(1);
    }

    @Test
    void shouldNotAllowAccessWithInvalidToken() {
        var exchange = client.get()
                .uri("/patterns/featured")
                .headers(headers -> headers.setBearerAuth("INVALID_TOKEN"))
                .exchange();

        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldReturnDeterministicOrderWithSameSeed() {
        var pattern1 = SamplePublishedPattern.builder().id(PatternId.of("PATTERN_1")).name(PatternName.of("Pattern 1")).build().toModel();
        var pattern2 = SamplePublishedPattern.builder().id(PatternId.of("PATTERN_2")).name(PatternName.of("Pattern 2")).build().toModel();
        var pattern3 = SamplePublishedPattern.builder().id(PatternId.of("PATTERN_3")).name(PatternName.of("Pattern 3")).build().toModel();

        when(module.getFeaturedPatterns(Agent.anonymous())).thenReturn(Flux.just(pattern1, pattern2, pattern3));

        var exchange1 = client.get()
                .uri("/patterns/featured?seed=12345")
                .exchange();
        exchange1.expectStatus().isOk();
        var response1 = exchange1.expectBody(QueryFeaturedPatternsResponse.class).returnResult().getResponseBody();
        var ids1 = response1.patterns.stream().map(p -> p.id).toList();

        var exchange2 = client.get()
                .uri("/patterns/featured?seed=12345")
                .exchange();
        exchange2.expectStatus().isOk();
        var response2 = exchange2.expectBody(QueryFeaturedPatternsResponse.class).returnResult().getResponseBody();
        var ids2 = response2.patterns.stream().map(p -> p.id).toList();

        assertThat(ids1).isEqualTo(ids2);
    }

    @Test
    void shouldReturnDifferentOrderWithDifferentSeeds() {
        var pattern1 = SamplePublishedPattern.builder().id(PatternId.of("PATTERN_1")).name(PatternName.of("Pattern 1")).build().toModel();
        var pattern2 = SamplePublishedPattern.builder().id(PatternId.of("PATTERN_2")).name(PatternName.of("Pattern 2")).build().toModel();
        var pattern3 = SamplePublishedPattern.builder().id(PatternId.of("PATTERN_3")).name(PatternName.of("Pattern 3")).build().toModel();
        var pattern4 = SamplePublishedPattern.builder().id(PatternId.of("PATTERN_4")).name(PatternName.of("Pattern 4")).build().toModel();
        var pattern5 = SamplePublishedPattern.builder().id(PatternId.of("PATTERN_5")).name(PatternName.of("Pattern 5")).build().toModel();

        when(module.getFeaturedPatterns(Agent.anonymous())).thenReturn(Flux.just(pattern1, pattern2, pattern3, pattern4, pattern5));

        var exchange1 = client.get()
                .uri("/patterns/featured?seed=11111")
                .exchange();
        exchange1.expectStatus().isOk();
        var response1 = exchange1.expectBody(QueryFeaturedPatternsResponse.class).returnResult().getResponseBody();
        var ids1 = response1.patterns.stream().map(p -> p.id).toList();

        var exchange2 = client.get()
                .uri("/patterns/featured?seed=99999")
                .exchange();
        exchange2.expectStatus().isOk();
        var response2 = exchange2.expectBody(QueryFeaturedPatternsResponse.class).returnResult().getResponseBody();
        var ids2 = response2.patterns.stream().map(p -> p.id).toList();

        assertThat(ids1).isNotEqualTo(ids2);
    }

    @Test
    void shouldWorkWithoutSeed() {
        var pattern1 = SamplePublishedPattern.builder().id(PatternId.of("PATTERN_1")).name(PatternName.of("Pattern 1")).build().toModel();
        var pattern2 = SamplePublishedPattern.builder().id(PatternId.of("PATTERN_2")).name(PatternName.of("Pattern 2")).build().toModel();
        var pattern3 = SamplePublishedPattern.builder().id(PatternId.of("PATTERN_3")).name(PatternName.of("Pattern 3")).build().toModel();

        when(module.getFeaturedPatterns(Agent.anonymous())).thenReturn(Flux.just(pattern1, pattern2, pattern3));

        var exchange = client.get()
                .uri("/patterns/featured")
                .exchange();
        exchange.expectStatus().isOk();
        var response = exchange.expectBody(QueryFeaturedPatternsResponse.class).returnResult().getResponseBody();

        assertThat(response.patterns).hasSize(3);
        assertThat(response.patterns.stream().map(p -> p.id).toList()).isEqualTo(List.of("PATTERN_1", "PATTERN_2", "PATTERN_3"));
    }

}
