package de.bennyboer.kicherkrabbe.embroideries;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DeleteEmbroideryTest extends EmbroideriesModuleTest {

    @Test
    void shouldDeleteEmbroidery() {
        // given: a user is allowed to create embroideries
        allowUserToCreateEmbroideries("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates two embroideries
        String embroideryId1 = createSampleEmbroidery(agent, "Crab");
        String embroideryId2 = createSampleEmbroidery(agent, "Fox");

        // when: the user deletes the first embroidery
        deleteEmbroidery(embroideryId1, 0L, agent);

        // then: only the second embroidery is left
        var embroideries = getEmbroideries(agent);
        assertThat(embroideries).hasSize(1);
        assertThat(embroideries.getFirst().getId()).isEqualTo(EmbroideryId.of(embroideryId2));

        // and: the deleted embroidery can no longer be read
        assertThatThrownBy(() -> getEmbroidery(embroideryId1, agent))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldNotDeleteEmbroideryWhenUserIsNotAllowed() {
        // given: an embroidery created by another user
        allowUserToCreateEmbroideries("OTHER_USER_ID");
        String embroideryId = createSampleEmbroidery(Agent.user(AgentId.of("OTHER_USER_ID")));

        // when: a user that is not allowed to delete the embroidery tries to delete it; then: an error is raised
        assertThatThrownBy(() -> deleteEmbroidery(embroideryId, 0L, Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldNotDeleteEmbroideryGivenAnOutdatedVersion() {
        // given: a user is allowed to create embroideries
        allowUserToCreateEmbroideries("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates and renames an embroidery
        String embroideryId = createSampleEmbroidery(agent, "Crab");
        renameEmbroidery(embroideryId, 0L, "Fox", agent);

        // when: the user tries to delete the embroidery with an outdated version; then: an error is raised
        assertThatThrownBy(() -> deleteEmbroidery(embroideryId, 0L, agent))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

}
