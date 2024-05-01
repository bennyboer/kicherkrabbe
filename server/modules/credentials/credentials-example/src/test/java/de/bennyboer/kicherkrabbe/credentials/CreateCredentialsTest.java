package de.bennyboer.kicherkrabbe.credentials;

import de.bennyboer.kicherkrabbe.credentials.create.NameAlreadyTakenError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CreateCredentialsTest extends CredentialsModuleTest {

    @Test
    void shouldCreateCredentials() {
        // when: creating some credentials
        createCredentials("TestName", "TestPassword", "USER_ID");

        // then: the credentials can be used
        var result = useCredentials("TestName", "TestPassword");
        assertThat(result.getToken()).isEqualTo("token-for-USER_ID");
    }

    @Test
    void shouldFailWhenTryingToCreateCredentialsWithTheSameName() {
        // given: some credentials
        createCredentials("TestName", "TestPassword", "USER_ID");

        // when: trying to create credentials with the same name; then: an error is raised
        assertThatThrownBy(() -> createCredentials("TestName", "TestPassword", "USER_ID"))
                .matches(e -> e.getCause() instanceof NameAlreadyTakenError
                        && e.getCause().getMessage().equals("Name already taken: TestName"));
    }

}
