package de.bennyboer.kicherkrabbe.credentials;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UseCredentialsTest extends CredentialsModuleTest {

    @Test
    void useCredentials() {
        // given: some credentials
        createCredentials("TestName", "TestPassword", "USER_ID");

        // when: using the credentials
        var result = useCredentials("TestName", "TestPassword");

        // then: the result contains a token
        assertThat(result.getToken()).isNotNull();
    }

}
