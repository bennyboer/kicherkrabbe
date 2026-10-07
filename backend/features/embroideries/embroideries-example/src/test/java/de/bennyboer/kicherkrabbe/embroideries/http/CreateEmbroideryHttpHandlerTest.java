package de.bennyboer.kicherkrabbe.embroideries.http;

import de.bennyboer.kicherkrabbe.embroideries.AliasAlreadyInUseError;
import de.bennyboer.kicherkrabbe.embroideries.CategoriesMissingError;
import de.bennyboer.kicherkrabbe.embroideries.EmbroideryAlias;
import de.bennyboer.kicherkrabbe.embroideries.EmbroideryCategoryId;
import de.bennyboer.kicherkrabbe.embroideries.EmbroideryId;
import de.bennyboer.kicherkrabbe.embroideries.http.api.requests.CreateEmbroideryRequest;
import de.bennyboer.kicherkrabbe.embroideries.http.api.responses.CreateEmbroideryResponse;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class CreateEmbroideryHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullyCreateEmbroidery() {
        // given: a request to create an embroidery
        var request = sampleRequest();

        // and: having a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to return the ID of the created embroidery
        when(module.createEmbroidery("Crab", "IMAGE_ID", Set.of("ANIMALS_ID"), Agent.user(AgentId.of("USER_ID"))))
                .thenReturn(Mono.just("EMBROIDERY_ID"));

        // when: posting the request
        var exchange = postRequest(request, token);

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response contains the ID of the created embroidery
        exchange.expectBody(CreateEmbroideryResponse.class)
                .value(response -> assertThat(response.id).isEqualTo("EMBROIDERY_ID"));
    }

    @Test
    void shouldRespondWithBadRequestOnInvalidInput() {
        // given: a request to create an embroidery
        var request = sampleRequest();

        // and: having a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to raise an illegal argument exception
        when(module.createEmbroidery("Crab", "IMAGE_ID", Set.of("ANIMALS_ID"), Agent.user(AgentId.of("USER_ID"))))
                .thenReturn(Mono.error(new IllegalArgumentException("Invalid")));

        // when: posting the request
        var exchange = postRequest(request, token);

        // then: the response is bad request
        exchange.expectStatus().isBadRequest();
    }

    @Test
    void shouldRespondWithPreconditionFailedOnMissingCategories() {
        // given: a request to create an embroidery
        var request = sampleRequest();

        // and: having a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to raise a categories missing error
        when(module.createEmbroidery("Crab", "IMAGE_ID", Set.of("ANIMALS_ID"), Agent.user(AgentId.of("USER_ID"))))
                .thenReturn(Mono.error(new CategoriesMissingError(Set.of(EmbroideryCategoryId.of("ANIMALS_ID")))));

        // when: posting the request
        var exchange = postRequest(request, token);

        // then: the response is precondition failed
        exchange.expectStatus().isEqualTo(412);
    }

    @Test
    void shouldRespondWithPreconditionFailedAndReasonOnAliasAlreadyInUse() {
        // given: a request to create an embroidery
        var request = sampleRequest();

        // and: having a valid token for a user
        var token = createTokenForUser("USER_ID");

        // and: the module is configured to raise an alias already in use error
        when(module.createEmbroidery("Crab", "IMAGE_ID", Set.of("ANIMALS_ID"), Agent.user(AgentId.of("USER_ID"))))
                .thenReturn(Mono.error(new AliasAlreadyInUseError(
                        EmbroideryId.of("OTHER_EMBROIDERY_ID"),
                        EmbroideryAlias.of("crab")
                )));

        // when: posting the request
        var exchange = postRequest(request, token);

        // then: the response is precondition failed
        exchange.expectStatus().isEqualTo(412);

        // and: the response contains the reason and the conflicting embroidery
        exchange.expectBody(Map.class).isEqualTo(Map.of(
                "reason", "ALIAS_ALREADY_IN_USE",
                "embroideryId", "OTHER_EMBROIDERY_ID",
                "alias", "crab"
        ));
    }

    @Test
    void shouldNotAllowUnauthorizedAccess() {
        // when: posting the request without a token
        var exchange = client.post()
                .uri("/embroideries/create")
                .bodyValue(sampleRequest())
                .exchange();

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    @Test
    void shouldNotAllowAccessWithInvalidToken() {
        // when: posting the request with an invalid token
        var exchange = postRequest(sampleRequest(), "INVALID_TOKEN");

        // then: the response is unauthorized
        exchange.expectStatus().isUnauthorized();
    }

    private CreateEmbroideryRequest sampleRequest() {
        var request = new CreateEmbroideryRequest();
        request.name = "Crab";
        request.image = "IMAGE_ID";
        request.categories = Set.of("ANIMALS_ID");
        return request;
    }

    private WebTestClient.ResponseSpec postRequest(
            CreateEmbroideryRequest request,
            String token
    ) {
        return client.post()
                .uri("/embroideries/create")
                .bodyValue(request)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();
    }

}
