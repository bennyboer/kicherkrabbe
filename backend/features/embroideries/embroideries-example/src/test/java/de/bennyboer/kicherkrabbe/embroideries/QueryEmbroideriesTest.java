package de.bennyboer.kicherkrabbe.embroideries;

import de.bennyboer.kicherkrabbe.embroideries.samples.SampleEmbroidery;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class QueryEmbroideriesTest extends EmbroideriesModuleTest {

    @Test
    void shouldOnlyReturnEmbroideriesTheUserIsAllowedToRead() {
        // given: two users are allowed to create embroideries
        allowUserToCreateEmbroideries("USER_ID_1");
        allowUserToCreateEmbroideries("USER_ID_2");
        var agent1 = Agent.user(AgentId.of("USER_ID_1"));
        var agent2 = Agent.user(AgentId.of("USER_ID_2"));

        // and: each user creates an embroidery
        String embroideryId1 = createSampleEmbroidery(agent1, "Crab");
        String embroideryId2 = createSampleEmbroidery(agent2, "Fox");

        // when: the first user queries the embroideries
        var embroideries = getEmbroideries(agent1);

        // then: only the embroidery of the first user is returned
        assertThat(embroideries).hasSize(1);
        assertThat(embroideries.getFirst().getId()).isEqualTo(EmbroideryId.of(embroideryId1));

        // when: the second user queries the embroideries
        embroideries = getEmbroideries(agent2);

        // then: only the embroidery of the second user is returned
        assertThat(embroideries).hasSize(1);
        assertThat(embroideries.getFirst().getId()).isEqualTo(EmbroideryId.of(embroideryId2));

        // and: anonymous users do not see any embroideries
        assertThat(getEmbroideries(Agent.anonymous())).isEmpty();
    }

    @Test
    void shouldQueryEmbroideriesBySearchTermAndCategories() {
        // given: a user is allowed to create embroideries
        allowUserToCreateEmbroideries("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: some categories are available
        markCategoryAsAvailable("ANIMALS_ID", "Animals");
        markCategoryAsAvailable("FLOWERS_ID", "Flowers");

        // and: some embroideries are created
        String crabId = createEmbroidery(SampleEmbroidery.builder().name("Crab").category("ANIMALS_ID").build(), agent);
        String foxId = createEmbroidery(SampleEmbroidery.builder().name("Fox").category("ANIMALS_ID").build(), agent);
        String roseId = createEmbroidery(SampleEmbroidery.builder().name("Rose").category("FLOWERS_ID").build(), agent);

        // when: querying embroideries by search term
        var page = getEmbroideries("o", Set.of(), 0, 10, agent);

        // then: the matching embroideries are returned
        assertThat(page.getResults())
                .extracting(EmbroideryDetails::getId)
                .containsExactlyInAnyOrder(EmbroideryId.of(foxId), EmbroideryId.of(roseId));

        // when: querying embroideries by category
        page = getEmbroideries("", Set.of("ANIMALS_ID"), 0, 10, agent);

        // then: the embroideries with that category are returned
        assertThat(page.getResults())
                .extracting(EmbroideryDetails::getId)
                .containsExactlyInAnyOrder(EmbroideryId.of(crabId), EmbroideryId.of(foxId));

        // when: querying embroideries with paging
        page = getEmbroideries("", Set.of(), 1, 1, agent);

        // then: a single embroidery is returned with the total count
        assertThat(page.getResults()).hasSize(1);
        assertThat(page.getTotal()).isEqualTo(3);
        assertThat(page.getSkip()).isEqualTo(1);
        assertThat(page.getLimit()).isEqualTo(1);
    }

    @Test
    void shouldQuerySingleEmbroidery() {
        // given: a user is allowed to create embroideries
        allowUserToCreateEmbroideries("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates an embroidery
        String embroideryId = createSampleEmbroidery(agent, "Crab");

        // when: the user queries the embroidery
        var embroidery = getEmbroidery(embroideryId, agent);

        // then: the embroidery is returned
        assertThat(embroidery.getId()).isEqualTo(EmbroideryId.of(embroideryId));
        assertThat(embroidery.getName()).isEqualTo(EmbroideryName.of("Crab"));
    }

    @Test
    void shouldNotQuerySingleEmbroideryWhenUserIsNotAllowed() {
        // given: an embroidery created by another user
        allowUserToCreateEmbroideries("OTHER_USER_ID");
        String embroideryId = createSampleEmbroidery(Agent.user(AgentId.of("OTHER_USER_ID")));

        // when: a user that is not allowed tries to read the embroidery; then: an error is raised
        assertThatThrownBy(() -> getEmbroidery(embroideryId, Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);

        // when: an anonymous user tries to read the embroidery; then: an error is raised
        assertThatThrownBy(() -> getEmbroidery(embroideryId, Agent.anonymous()))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

}
