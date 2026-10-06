package de.bennyboer.kicherkrabbe.embroideries.http;

import de.bennyboer.kicherkrabbe.embroideries.CategoriesMissingError;
import de.bennyboer.kicherkrabbe.embroideries.EmbroideryCategoryId;
import de.bennyboer.kicherkrabbe.embroideries.http.api.requests.UpdateEmbroideryCategoriesRequest;
import de.bennyboer.kicherkrabbe.embroideries.http.api.requests.UpdateEmbroideryImageRequest;
import de.bennyboer.kicherkrabbe.embroideries.http.api.responses.UpdateEmbroideryCategoriesResponse;
import de.bennyboer.kicherkrabbe.embroideries.http.api.responses.UpdateEmbroideryImageResponse;
import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class UpdateEmbroideryHttpHandlerTest extends HttpHandlerTest {

    private final Agent agent = Agent.user(AgentId.of("USER_ID"));

    @Test
    void shouldSuccessfullyUpdateImage() {
        // given: the module is configured to return the new version
        when(module.updateEmbroideryImage("EMBROIDERY_ID", 0L, "NEW_IMAGE_ID", agent)).thenReturn(Mono.just(1L));

        // when: posting the request to update the image
        var exchange = postImage("NEW_IMAGE_ID", createTokenForUser("USER_ID"));

        // then: the response is successful and contains the new version
        exchange.expectStatus().isOk();
        exchange.expectBody(UpdateEmbroideryImageResponse.class)
                .value(response -> assertThat(response.version).isEqualTo(1L));
    }

    @Test
    void shouldRespondWithBadRequestOnInvalidImage() {
        // given: the module is configured to raise an illegal argument exception
        when(module.updateEmbroideryImage("EMBROIDERY_ID", 0L, "", agent))
                .thenReturn(Mono.error(new IllegalArgumentException("Image must not be blank")));

        // when: posting the request to update the image
        var exchange = postImage("", createTokenForUser("USER_ID"));

        // then: the response is bad request
        exchange.expectStatus().isBadRequest();
    }

    @Test
    void shouldRespondWith409OnOutdatedVersionWhenUpdatingImage() {
        // given: the module is configured to raise a version outdated error
        when(module.updateEmbroideryImage("EMBROIDERY_ID", 0L, "NEW_IMAGE_ID", agent))
                .thenReturn(Mono.error(new AggregateVersionOutdatedError(
                        AggregateType.of("EMBROIDERY"),
                        AggregateId.of("EMBROIDERY_ID"),
                        Version.zero()
                )));

        // when: posting the request to update the image
        var exchange = postImage("NEW_IMAGE_ID", createTokenForUser("USER_ID"));

        // then: the response is conflict
        exchange.expectStatus().isEqualTo(409);
    }

    @Test
    void shouldSuccessfullyUpdateCategories() {
        // given: the module is configured to return the new version
        when(module.updateEmbroideryCategories("EMBROIDERY_ID", 0L, Set.of("ANIMALS_ID"), agent))
                .thenReturn(Mono.just(1L));

        // when: posting the request to update the categories
        var exchange = postCategories(Set.of("ANIMALS_ID"), createTokenForUser("USER_ID"));

        // then: the response is successful and contains the new version
        exchange.expectStatus().isOk();
        exchange.expectBody(UpdateEmbroideryCategoriesResponse.class)
                .value(response -> assertThat(response.version).isEqualTo(1L));
    }

    @Test
    void shouldRespondWithPreconditionFailedOnMissingCategories() {
        // given: the module is configured to raise a categories missing error
        when(module.updateEmbroideryCategories("EMBROIDERY_ID", 0L, Set.of("UNKNOWN_ID"), agent))
                .thenReturn(Mono.error(new CategoriesMissingError(Set.of(EmbroideryCategoryId.of("UNKNOWN_ID")))));

        // when: posting the request to update the categories
        var exchange = postCategories(Set.of("UNKNOWN_ID"), createTokenForUser("USER_ID"));

        // then: the response is precondition failed
        exchange.expectStatus().isEqualTo(412);
    }

    @Test
    void shouldNotAllowUnauthorizedAccess() {
        // given: requests to update the image and categories
        var imageRequest = new UpdateEmbroideryImageRequest();
        imageRequest.version = 0L;
        imageRequest.image = "NEW_IMAGE_ID";
        var categoriesRequest = new UpdateEmbroideryCategoriesRequest();
        categoriesRequest.version = 0L;
        categoriesRequest.categories = Set.of();

        // when: updating the image without a token; then: the response is unauthorized
        client.post()
                .uri("/embroideries/EMBROIDERY_ID/update/image")
                .bodyValue(imageRequest)
                .exchange()
                .expectStatus()
                .isUnauthorized();

        // when: updating the categories without a token; then: the response is unauthorized
        client.post()
                .uri("/embroideries/EMBROIDERY_ID/update/categories")
                .bodyValue(categoriesRequest)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    private WebTestClient.ResponseSpec postImage(String image, String token) {
        var request = new UpdateEmbroideryImageRequest();
        request.version = 0L;
        request.image = image;

        return client.post()
                .uri("/embroideries/EMBROIDERY_ID/update/image")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();
    }

    private WebTestClient.ResponseSpec postCategories(Set<String> categories, String token) {
        var request = new UpdateEmbroideryCategoriesRequest();
        request.version = 0L;
        request.categories = categories;

        return client.post()
                .uri("/embroideries/EMBROIDERY_ID/update/categories")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();
    }

}
