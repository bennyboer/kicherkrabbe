package de.bennyboer.kicherkrabbe.embroideries;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RenameEmbroideryTest extends EmbroideriesModuleTest {

    @Test
    void shouldRenameEmbroidery() {
        // given: a user is allowed to create embroideries
        allowUserToCreateEmbroideries("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates an embroidery
        String embroideryId = createSampleEmbroidery(agent, "Crab");

        // when: the user renames the embroidery
        renameEmbroidery(embroideryId, 0L, "Happy Crab", agent);

        // then: the embroidery is renamed
        var embroidery = getEmbroidery(embroideryId, agent);
        assertThat(embroidery.getVersion()).isEqualTo(Version.of(1));
        assertThat(embroidery.getName()).isEqualTo(EmbroideryName.of("Happy Crab"));
    }

    @Test
    void shouldNotRenameEmbroideryGivenAnInvalidName() {
        // given: a user is allowed to create embroideries
        allowUserToCreateEmbroideries("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates an embroidery
        String embroideryId = createSampleEmbroidery(agent);

        // when: the user tries to rename the embroidery to a blank name; then: an error is raised
        assertThatThrownBy(() -> renameEmbroidery(embroideryId, 0L, "", agent))
                .isInstanceOf(IllegalArgumentException.class);

        // when: the user tries to rename the embroidery to no name; then: an error is raised
        assertThatThrownBy(() -> renameEmbroidery(embroideryId, 0L, null, agent))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldNotRenameEmbroideryWhenUserIsNotAllowed() {
        // given: an embroidery created by another user
        allowUserToCreateEmbroideries("OTHER_USER_ID");
        String embroideryId = createSampleEmbroidery(Agent.user(AgentId.of("OTHER_USER_ID")));

        // when: a user that is not allowed to rename the embroidery tries to rename it; then: an error is raised
        assertThatThrownBy(() -> renameEmbroidery(embroideryId, 0L, "Fox", Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldNotRenameEmbroideryGivenAnOutdatedVersion() {
        // given: a user is allowed to create embroideries
        allowUserToCreateEmbroideries("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates and renames an embroidery
        String embroideryId = createSampleEmbroidery(agent, "Crab");
        renameEmbroidery(embroideryId, 0L, "Happy Crab", agent);

        // when: the user tries to rename the embroidery with an outdated version; then: an error is raised
        assertThatThrownBy(() -> renameEmbroidery(embroideryId, 0L, "Sad Crab", agent))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldNotRenameEmbroideryWhenAliasIsAlreadyInUse() {
        // given: a user is allowed to create embroideries
        allowUserToCreateEmbroideries("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: two embroideries are created
        String embroideryId1 = createSampleEmbroidery(agent, "Little Crab");
        String embroideryId2 = createSampleEmbroidery(agent, "Fox");

        // when: the user tries to rename the second embroidery to the name of the first; then: an error is raised
        assertThatThrownBy(() -> renameEmbroidery(embroideryId2, 0L, "Little Crab", agent))
                .matches(e -> e.getCause() instanceof AliasAlreadyInUseError
                        && ((AliasAlreadyInUseError) e.getCause()).getConflictingEmbroideryId()
                        .equals(EmbroideryId.of(embroideryId1)));

        // when: the user renames the second embroidery to a unique name
        renameEmbroidery(embroideryId2, 0L, "Little Fox", agent);

        // then: the embroidery is renamed
        assertThat(getEmbroidery(embroideryId2, agent).getName()).isEqualTo(EmbroideryName.of("Little Fox"));
    }

    @Test
    void shouldAllowRenamingEmbroideryToANameWithTheSameAlias() {
        // given: a user is allowed to create embroideries
        allowUserToCreateEmbroideries("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: an embroidery is created
        String embroideryId = createSampleEmbroidery(agent, "Little Crab");

        // when: the user only changes the casing of the name
        renameEmbroidery(embroideryId, 0L, "Little CRAB", agent);

        // then: the embroidery is renamed
        assertThat(getEmbroidery(embroideryId, agent).getName()).isEqualTo(EmbroideryName.of("Little CRAB"));
    }

}
