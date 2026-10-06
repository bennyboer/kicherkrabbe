package de.bennyboer.kicherkrabbe.embroideries;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class QueryPublishedEmbroideryTest extends EmbroideriesModuleTest {

    @Test
    void shouldQueryPublishedEmbroideryByIdOrAlias() {
        // given: a user is allowed to create embroideries
        allowUserToCreateEmbroideries("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates and publishes an embroidery
        String embroideryId = createSampleEmbroidery(agent, "Little Crab");
        publishEmbroidery(embroideryId, 0L, agent);

        // when: an anonymous user queries the published embroidery by ID
        var embroidery = getPublishedEmbroidery(embroideryId, Agent.anonymous());

        // then: the embroidery is returned
        assertThat(embroidery.getId()).isEqualTo(EmbroideryId.of(embroideryId));
        assertThat(embroidery.getName()).isEqualTo(EmbroideryName.of("Little Crab"));
        assertThat(embroidery.getAlias()).isEqualTo(EmbroideryAlias.of("little-crab"));

        // when: an anonymous user queries the published embroidery by alias
        embroidery = getPublishedEmbroidery("little-crab", Agent.anonymous());

        // then: the embroidery is returned
        assertThat(embroidery.getId()).isEqualTo(EmbroideryId.of(embroideryId));

        // when: the system queries the published embroidery
        embroidery = getPublishedEmbroidery(embroideryId, Agent.system());

        // then: the embroidery is returned
        assertThat(embroidery.getId()).isEqualTo(EmbroideryId.of(embroideryId));
    }

    @Test
    void shouldFindPublishedEmbroideryByAliasAfterRename() {
        // given: a user is allowed to create embroideries
        allowUserToCreateEmbroideries("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates and publishes an embroidery
        String embroideryId = createSampleEmbroidery(agent, "Crab");
        publishEmbroidery(embroideryId, 0L, agent);

        // when: the embroidery is renamed
        renameEmbroidery(embroideryId, 1L, "Happy Crab", agent);

        // then: the embroidery is found by the new alias
        assertThat(getPublishedEmbroidery("happy-crab", Agent.anonymous()).getId())
                .isEqualTo(EmbroideryId.of(embroideryId));

        // and: the embroidery is no longer found by the old alias
        assertThat(getPublishedEmbroidery("crab", Agent.anonymous())).isNull();
    }

    @Test
    void shouldNotReturnUnpublishedEmbroidery() {
        // given: a user is allowed to create embroideries
        allowUserToCreateEmbroideries("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates an embroidery without publishing it
        String embroideryId = createSampleEmbroidery(agent, "Crab");

        // when: an anonymous user queries the unpublished embroidery by ID; then: nothing is returned
        assertThat(getPublishedEmbroidery(embroideryId, Agent.anonymous())).isNull();

        // when: an anonymous user queries the unpublished embroidery by alias; then: nothing is returned
        assertThat(getPublishedEmbroidery("crab", Agent.anonymous())).isNull();

        // when: even the owner queries the unpublished embroidery as published; then: nothing is returned
        assertThat(getPublishedEmbroidery(embroideryId, agent)).isNull();
    }

    @Test
    void shouldNotReturnUnknownEmbroidery() {
        // when: an anonymous user queries an unknown embroidery; then: nothing is returned
        assertThat(getPublishedEmbroidery("UNKNOWN", Agent.anonymous())).isNull();
    }

}
