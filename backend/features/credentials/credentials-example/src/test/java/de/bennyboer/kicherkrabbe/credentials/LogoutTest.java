package de.bennyboer.kicherkrabbe.credentials;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class LogoutTest extends CredentialsModuleTest {

    @Test
    void shouldRevokeRefreshTokenOnLogout() {
        // given: some credentials and a login result
        createCredentials("TestName", "TestPassword", "USER_ID", Agent.system());
        var loginResult = useCredentials("TestName", "TestPassword", Agent.anonymous());

        // when: logging out
        logout(loginResult.getRefreshToken());

        // then: the refresh token can no longer be used
        assertThatThrownBy(() -> refreshTokens(loginResult.getRefreshToken()))
                .hasMessageContaining("Invalid refresh token");
    }

    @Test
    void shouldAllowReLoginAfterLogout() {
        // given: some credentials and a login result
        createCredentials("TestName", "TestPassword", "USER_ID", Agent.system());
        var loginResult = useCredentials("TestName", "TestPassword", Agent.anonymous());

        // and: the user logs out
        logout(loginResult.getRefreshToken());

        // when: logging in again
        var newLoginResult = useCredentials("TestName", "TestPassword", Agent.anonymous());

        // then: new tokens are valid
        var refreshResult = refreshTokens(newLoginResult.getRefreshToken());
        assertThatThrownBy(() -> refreshTokens(loginResult.getRefreshToken()))
                .hasMessageContaining("Invalid refresh token");
    }

}
