package de.bennyboer.kicherkrabbe.users;

import de.bennyboer.kicherkrabbe.users.create.MailAlreadyInUseError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CreateUserTest extends UsersModuleTest {

    @Test
    void shouldCreateUser() {
        // when: a user is created
        String userId = createUser("John", "Doe", "john.doe@kicherkrabbe.com");

        // then: the user details can be fetched
        UserDetails userDetails = getUserDetails(userId);

        // and: the user details are correct
        assertThat(userDetails.getUserId().getValue()).isEqualTo(userId);
        assertThat(userDetails.getName().getFirstName().getValue()).isEqualTo("John");
        assertThat(userDetails.getName().getLastName().getValue()).isEqualTo("Doe");
        assertThat(userDetails.getMail().getValue()).isEqualTo("john.doe@kicherkrabbe.com");
    }

    @Test
    void shouldRaiseErrorIfUserMailAlreadyExists() {
        // given: a user
        createUser("John", "Doe", "john.doe@kicherkrabbe.com");

        // when: trying to create a user with the same mail; then: an error is raised
        assertThatThrownBy(() -> createUser("Jane", "Doe", "john.doe@kicherkrabbe.com"))
                .matches(e -> e.getCause() instanceof MailAlreadyInUseError
                        && e.getCause().getMessage().equals("Mail already in use: john.doe@kicherkrabbe.com"));
    }

}
