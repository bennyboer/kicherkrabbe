package de.bennyboer.kicherkrabbe.credentials;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DeleteCredentialsTest extends CredentialsModuleTest {

    @Test
    void shouldDeleteCredentials() {
        // given: some credentials
        String credentialsId = createCredentials("TestName", "TestPassword", "USER_ID");

        // when: deleting the credentials
        deleteCredentials(credentialsId);

        // then: the credentials cannot be used anymore
        var result = useCredentials("TestName", "TestPassword");
        assertThat(result).isNull();
    }

    @Test
    void shouldAllowCreatingCredentialsWithANameOfDeletedCredentials() {
        // given: some credentials
        String credentialsId = createCredentials("TestName", "TestPassword", "USER_ID");

        // when: deleting the credentials
        deleteCredentials(credentialsId);

        // then: the credentials can be created again
        createCredentials("TestName", "TestPassword", "USER_ID");
    }

    @Test
    void shouldDeleteCredentialsByUserId() {
        // given: multiple credentials for the same user ID
        createCredentials("TestName1", "TestPassword1", "USER_ID");
        createCredentials("TestName2", "TestPassword2", "USER_ID");

        // and: credentials for another user ID
        createCredentials("TestName3", "TestPassword3", "OTHER_USER_ID");

        // when: deleting the credentials by user ID
        deleteCredentialsByUserId("USER_ID");

        // then: the credentials cannot be used anymore
        assertThat(useCredentials("TestName1", "TestPassword1")).isNull();
        assertThat(useCredentials("TestName2", "TestPassword2")).isNull();

        // and: the credentials for the other user ID are still available
        assertThat(useCredentials("TestName3", "TestPassword3")).isNotNull();
    }

}
