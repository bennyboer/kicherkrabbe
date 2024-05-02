package de.bennyboer.kicherkrabbe.users;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RenameUserTest extends UsersModuleTest {

    @Test
    void shouldRenameUser() {
        // given: a user
        String userId = createUser("John", "Doe", "john.doe@kicherkrabbe.com", Agent.system());

        // when: the user is renamed by itself
        renameUser(userId, "Jane", "Doe", Agent.user(AgentId.of(userId)));

        // then: the user details are correct
        UserDetails userDetails = getUserDetails(userId);
        assertThat(userDetails.getName().getFirstName().getValue()).isEqualTo("Jane");
        assertThat(userDetails.getName().getLastName().getValue()).isEqualTo("Doe");
    }

    @Test
    void shouldNotBeAbleToRenameUserAsUserWithoutPermission() {
        // given: a user
        String userId = createUser("John", "Doe", "john.doe@kicherkrabbe.com", Agent.system());

        // when: the user is renamed by another user; then: an exception is thrown
        assertThatThrownBy(() -> renameUser(userId, "Jane", "Doe", Agent.user(AgentId.of("ANOTHER_USER_ID"))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldNotBeAbleToRenameUserAsSystem() {
        // given: a user
        String userId = createUser("John", "Doe", "john.doe@kicherkrabbe.com", Agent.system());

        // when: the user is renamed by the system; then: an exception is thrown
        assertThatThrownBy(() -> renameUser(userId, "Jane", "Doe", Agent.system()))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldNotBeAbleToRenameUserAsAnonymous() {
        // given: a user
        String userId = createUser("John", "Doe", "john.doe@kicherkrabbe.com", Agent.system());

        // when: the user is renamed by an anonymous agent; then: an exception is thrown
        assertThatThrownBy(() -> renameUser(userId, "Jane", "Doe", Agent.anonymous()))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

}
