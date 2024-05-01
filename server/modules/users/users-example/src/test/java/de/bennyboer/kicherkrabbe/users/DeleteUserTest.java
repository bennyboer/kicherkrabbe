package de.bennyboer.kicherkrabbe.users;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DeleteUserTest extends UsersModuleTest {

    @Test
    void shouldDeleteUser() {
        // given: a user
        String userId = createUser("John", "Doe", "john.doe@kicherkrabbe.com", Agent.system());

        // when: the user is deleted
        deleteUser(userId);

        // then: the user details cannot be fetched
        UserDetails userDetails = getUserDetails(userId);
        assertThat(userDetails).isNull();
    }

    @Test
    void shouldBeAbleToCreateUserAfterDeletingOneForTheSameMail() {
        // given: a user
        String userId = createUser("John", "Doe", "john.doe@kicherkrabbe.com", Agent.system());

        // when: the user is deleted
        deleteUser(userId);

        // then: another user with the same mail can be created
        createUser("Jane", "Doe", "john.doe@kicherkrabbe.com", Agent.system());
    }

}
