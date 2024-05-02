package de.bennyboer.kicherkrabbe.credentials;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DeleteCredentialsTest extends CredentialsModuleTest {

    @Test
    void shouldDeleteCredentials() {
        // given: some credentials
        String credentialsId = createCredentials("TestName", "TestPassword", "USER_ID", Agent.system());

        // when: deleting the credentials
        deleteCredentials(credentialsId, Agent.system());

        // then: the credentials cannot be used anymore
        var result = useCredentials("TestName", "TestPassword", Agent.anonymous());
        assertThat(result).isNull();
    }

    @Test
    void shouldNotAllowDeletingCredentialsAsAnonymousUser() {
        // given: some credentials
        String credentialsId = createCredentials("TestName", "TestPassword", "USER_ID", Agent.system());

        // when: deleting the credentials as anonymous user; then an error is raised
        assertThatThrownBy(() -> deleteCredentials(credentialsId, Agent.anonymous()))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldNotAllowDeletingCredentialsAsUser() {
        // given: some credentials
        String credentialsId = createCredentials("TestName", "TestPassword", "USER_ID", Agent.system());

        // when: deleting the credentials as user; then an error is raised
        assertThatThrownBy(() -> deleteCredentials(credentialsId, Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldAllowCreatingCredentialsWithANameOfDeletedCredentials() {
        // given: some credentials
        String credentialsId = createCredentials("TestName", "TestPassword", "USER_ID", Agent.system());

        // when: deleting the credentials
        deleteCredentials(credentialsId, Agent.system());

        // then: the credentials can be created again
        createCredentials("TestName", "TestPassword", "USER_ID", Agent.system());
    }

    @Test
    void shouldDeleteCredentialsByUserId() {
        // given: multiple credentials for the same user ID
        createCredentials("TestName1", "TestPassword1", "USER_ID", Agent.system());
        createCredentials("TestName2", "TestPassword2", "USER_ID", Agent.system());

        // and: credentials for another user ID
        createCredentials("TestName3", "TestPassword3", "OTHER_USER_ID", Agent.system());

        // when: deleting the credentials by user ID
        deleteCredentialsByUserId("USER_ID", Agent.system());

        // then: the credentials cannot be used anymore
        assertThat(useCredentials("TestName1", "TestPassword1", Agent.anonymous())).isNull();
        assertThat(useCredentials("TestName2", "TestPassword2", Agent.anonymous())).isNull();

        // and: the credentials for the other user ID are still available
        assertThat(useCredentials("TestName3", "TestPassword3", Agent.anonymous())).isNotNull();
    }

    @Test
    void shouldNotBeAbleToDeleteCredentialsByUserIdAsAnonymousUser() {
        // given: some credentials
        createCredentials("TestName", "TestPassword", "USER_ID", Agent.system());

        // when: deleting the credentials by user ID as anonymous user; then an error is raised
        assertThatThrownBy(() -> deleteCredentialsByUserId("USER_ID", Agent.anonymous()))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldNotBeAbleToDeleteCredentialsByUserIdAsUser() {
        // given: some credentials
        createCredentials("TestName", "TestPassword", "USER_ID", Agent.system());

        // when: deleting the credentials by user ID as user; then an error is raised
        assertThatThrownBy(() -> deleteCredentialsByUserId("USER_ID", Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

}
