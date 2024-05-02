package de.bennyboer.kicherkrabbe.users;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DeleteUserTest extends UsersModuleTest {

    @Test
    void shouldDeleteUser() {
        // given: a user
        String userId = createUser("John", "Doe", "john.doe@kicherkrabbe.com", Agent.system());

        // when: the user is deleted
        deleteUser(userId, Agent.user(AgentId.of(userId)));

        // then: the user details cannot be fetched
        UserDetails userDetails = getUserDetails(userId);
        assertThat(userDetails).isNull();
    }

    @Test
    void shouldBeAbleToCreateUserAfterDeletingOneForTheSameMail() {
        // given: a user
        String userId = createUser("John", "Doe", "john.doe@kicherkrabbe.com", Agent.system());

        // when: the user is deleted
        deleteUser(userId, Agent.user(AgentId.of(userId)));

        // then: another user with the same mail can be created
        createUser("Jane", "Doe", "john.doe@kicherkrabbe.com", Agent.system());
    }

    @Test
    void shouldNotBeAbleToDeleteUserAsAnotherUser() {
        // given: a user
        String userId = createUser("John", "Doe", "john.doe@kicherkrabbe.com", Agent.system());

        // when: the user is deleted by another user; then: an exception is thrown
        assertThatThrownBy(() -> deleteUser(userId, Agent.user(AgentId.of("ANOTHER_USER_ID"))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldNotBeAbleToDeleteUserAsSystem() {
        // given: a user
        String userId = createUser("John", "Doe", "john.doe@kicherkrabbe.com", Agent.system());

        // when: the user is deleted by the system; then: an exception is thrown
        assertThatThrownBy(() -> deleteUser(userId, Agent.system()))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldNotBeAbleToDeleteUserAsAnonymous() {
        // given: a user
        String userId = createUser("John", "Doe", "john.doe@kicherkrabbe.com", Agent.system());

        // when: the user is deleted by an anonymous user; then: an exception is thrown
        assertThatThrownBy(() -> deleteUser(userId, Agent.anonymous()))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

}
