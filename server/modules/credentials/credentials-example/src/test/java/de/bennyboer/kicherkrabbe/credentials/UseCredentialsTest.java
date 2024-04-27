package de.bennyboer.kicherkrabbe.credentials;

import de.bennyboer.kicherkrabbe.credentials.internal.use.InvalidCredentialsUsedOrUserLockedError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UseCredentialsTest extends CredentialsModuleTest {

    @Test
    void useCredentials() {
        // given: some credentials
        createCredentials("TestName", "TestPassword", "USER_ID");

        // when: using the credentials
        var result = useCredentials("TestName", "TestPassword");

        // then: the result contains the token for the user
        assertThat(result.getToken()).isEqualTo("token-for-USER_ID");
    }

    @Test
    void shouldFailWhenTryingToUseIncorrectCredentials() {
        // given: some credentials
        createCredentials("TestName", "TestPassword", "USER_ID");

        // when: using invalid credentials; then: an error is raised
        assertThatThrownBy(() -> useCredentials("TestName", "WrongPassword"))
                .matches(e -> e.getCause() instanceof InvalidCredentialsUsedOrUserLockedError);
    }
    
}
