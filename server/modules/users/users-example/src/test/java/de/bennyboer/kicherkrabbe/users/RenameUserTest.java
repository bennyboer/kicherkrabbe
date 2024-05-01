package de.bennyboer.kicherkrabbe.users;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RenameUserTest extends UsersModuleTest {

    @Test
    void shouldRenameUser() {
        // given: a user
        String userId = createUser("John", "Doe", "john.doe@kicherkrabbe.com", Agent.system());

        // when: the user is renamed
        renameUser(userId, "Jane", "Doe");

        // then: the user details are correct
        UserDetails userDetails = getUserDetails(userId);
        assertThat(userDetails.getName().getFirstName().getValue()).isEqualTo("Jane");
        assertThat(userDetails.getName().getLastName().getValue()).isEqualTo("Doe");
    }

}
