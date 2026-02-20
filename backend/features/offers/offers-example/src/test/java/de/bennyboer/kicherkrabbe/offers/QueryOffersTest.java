package de.bennyboer.kicherkrabbe.offers;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class QueryOffersTest extends OffersModuleTest {

    @Test
    void shouldQueryOffers() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        createSampleOffer(agent);
        createSampleOffer(agent);
        createSampleOffer(agent);

        var page = getOffers("", 0, 10, agent);
        assertThat(page.getTotal()).isEqualTo(3);
        assertThat(page.getResults()).hasSize(3);
        assertThat(page.getSkip()).isEqualTo(0);
        assertThat(page.getLimit()).isEqualTo(10);
    }

    @Test
    void shouldQueryOffersWithPaging() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        createSampleOffer(agent);
        createSampleOffer(agent);
        createSampleOffer(agent);

        var page = getOffers("", 0, 2, agent);
        assertThat(page.getTotal()).isEqualTo(3);
        assertThat(page.getResults()).hasSize(2);

        page = getOffers("", 2, 2, agent);
        assertThat(page.getTotal()).isEqualTo(3);
        assertThat(page.getResults()).hasSize(1);
    }

    @Test
    void shouldNotSeeOtherUsersOffers() {
        allowUserToCreateOffers("USER_ID_1");
        allowUserToCreateOffers("USER_ID_2");
        var agent1 = Agent.user(AgentId.of("USER_ID_1"));
        var agent2 = Agent.user(AgentId.of("USER_ID_2"));

        createSampleOffer(agent1);
        createSampleOffer(agent1);
        createSampleOffer(agent2);

        var offers1 = getOffers(agent1);
        assertThat(offers1).hasSize(2);

        var offers2 = getOffers(agent2);
        assertThat(offers2).hasSize(1);
    }
}
