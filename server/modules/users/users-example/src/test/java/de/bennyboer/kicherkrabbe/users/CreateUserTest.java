package de.bennyboer.kicherkrabbe.users;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import de.bennyboer.kicherkrabbe.users.create.MailAlreadyInUseError;
import org.junit.jupiter.api.Test;

import static de.bennyboer.kicherkrabbe.users.Actions.CREATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CreateUserTest extends UsersModuleTest {

    @Test
    void shouldCreateUserAsSystem() {
        // when: a user is created as system
        String userId = createUser("John", "Doe", "john.doe@kicherkrabbe.com", Agent.system());

        // then: the user details can be fetched
        UserDetails userDetails = getUserDetails(userId);

        // and: the user details are correct
        assertThat(userDetails.getUserId().getValue()).isEqualTo(userId);
        assertThat(userDetails.getName().getFirstName().getValue()).isEqualTo("John");
        assertThat(userDetails.getName().getLastName().getValue()).isEqualTo("Doe");
        assertThat(userDetails.getMail().getValue()).isEqualTo("john.doe@kicherkrabbe.com");
    }

    @Test
    void shouldCreateUserAsUserWithPermission() {
        // given: a user has permission to create another user
        userHasPermission(UserId.of("USER_ID"), CREATE);

        // when: a user is created for a user with permission
        String userId = createUser(
                "John",
                "Doe",
                "john.doe@kicherkrabbe.com",
                Agent.user(AgentId.of("USER_ID"))
        );

        // then: the user details can be fetched
        UserDetails userDetails = getUserDetails(userId);
        assertThat(userDetails.getUserId().getValue()).isEqualTo(userId);
    }

    @Test
    void shouldNotCreateUserAsUserWithoutPermission() {
        // when: a user is created as user without permission; then: an error is raised
        assertThatThrownBy(() -> createUser(
                "John",
                "Doe",
                "john.doe@kicherkrabbe.com",
                Agent.user(AgentId.of("USER_ID"))
        ))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldNotCreateUserAsAnonymous() {
        // when: a user is created as anonymous; then: an error is raised
        assertThatThrownBy(() -> createUser("John", "Doe", "john.doe@kicherkrabbe.com", Agent.anonymous()))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldRaiseErrorIfUserMailAlreadyExists() {
        // given: a user
        createUser("John", "Doe", "john.doe@kicherkrabbe.com", Agent.system());

        // when: trying to create a user with the same mail; then: an error is raised
        assertThatThrownBy(() -> createUser("Jane", "Doe", "john.doe@kicherkrabbe.com", Agent.system()))
                .matches(e -> e.getCause() instanceof MailAlreadyInUseError
                        && e.getCause().getMessage().equals("Mail already in use: john.doe@kicherkrabbe.com"));
    }

}
