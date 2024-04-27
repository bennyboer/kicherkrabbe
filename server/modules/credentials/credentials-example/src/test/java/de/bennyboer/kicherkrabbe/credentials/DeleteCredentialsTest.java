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

}
