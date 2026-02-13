package de.bennyboer.kicherkrabbe.credentials;

import de.bennyboer.kicherkrabbe.credentials.use.InvalidCredentialsUsedOrUserLockedError;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UseCredentialsTest extends CredentialsModuleTest {

    @Test
    void useCredentials() {
        // given: some credentials
        createCredentials("TestName", "TestPassword", "USER_ID", Agent.system());

        // when: using the credentials
        var result = useCredentials("TestName", "TestPassword", Agent.anonymous());

        // then: the result contains the token for the user
        assertThat(result.getToken()).isEqualTo("token-for-USER_ID");
        assertThat(result.getRefreshToken()).isNotBlank();
    }

    @Test
    void shouldFailWhenTryingToUseIncorrectCredentials() {
        // given: some credentials
        createCredentials("TestName", "TestPassword", "USER_ID", Agent.system());

        // when: using invalid credentials; then: an error is raised
        assertThatThrownBy(() -> useCredentials("TestName", "WrongPassword", Agent.anonymous()))
                .matches(e -> e.getCause() instanceof InvalidCredentialsUsedOrUserLockedError);
    }

    @Test
    void shouldNotBeAbleToUseCredentialsAsSystem() {
        // given: some credentials
        createCredentials("TestName", "TestPassword", "USER_ID", Agent.system());

        // when: using the credentials as system; then: an error is raised
        assertThatThrownBy(() -> useCredentials("TestName", "TestPassword", Agent.system()))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldAllowUsingCredentialsAsUser() {
        // given: some credentials
        createCredentials("TestName", "TestPassword", "USER_ID", Agent.system());

        // when: using the credentials as user
        var result = useCredentials("TestName", "TestPassword", Agent.user(AgentId.of("USER_ID")));

        // then: the result contains the token for the user
        assertThat(result.getToken()).isEqualTo("token-for-USER_ID");
        assertThat(result.getRefreshToken()).isNotBlank();
    }

}
