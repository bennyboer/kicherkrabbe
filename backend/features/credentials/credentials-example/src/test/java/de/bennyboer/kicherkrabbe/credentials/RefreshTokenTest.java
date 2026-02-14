package de.bennyboer.kicherkrabbe.credentials;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RefreshTokenTest extends CredentialsModuleTest {

    @Test
    void shouldRefreshTokens() {
        // given: some credentials and a login result
        createCredentials("TestName", "TestPassword", "USER_ID", Agent.system());
        var loginResult = useCredentials("TestName", "TestPassword", Agent.anonymous());

        // when: refreshing with the refresh token
        var refreshResult = refreshTokens(loginResult.getRefreshToken());

        // then: new tokens are returned
        assertThat(refreshResult.getAccessToken()).isEqualTo("token-for-USER_ID");
        assertThat(refreshResult.getRefreshToken()).isNotBlank();
        assertThat(refreshResult.getRefreshToken()).isNotEqualTo(loginResult.getRefreshToken());
    }

    @Test
    void shouldDetectRefreshTokenReuse() {
        // given: some credentials and a login result
        createCredentials("TestName", "TestPassword", "USER_ID", Agent.system());
        var loginResult = useCredentials("TestName", "TestPassword", Agent.anonymous());

        // and: the refresh token has been used once
        var refreshResult = refreshTokens(loginResult.getRefreshToken());

        // when: using the old refresh token again; then: reuse is detected and family is revoked
        assertThatThrownBy(() -> refreshTokens(loginResult.getRefreshToken()))
                .hasMessageContaining("reuse detected");

        // and: the new refresh token is also revoked (entire family)
        assertThatThrownBy(() -> refreshTokens(refreshResult.getRefreshToken()))
                .hasMessageContaining("Invalid refresh token");
    }

    @Test
    void shouldFailWithInvalidRefreshToken() {
        // when: refreshing with an invalid token; then: an error is raised
        assertThatThrownBy(() -> refreshTokens("invalid-token"))
                .hasMessageContaining("Invalid refresh token");
    }

    @Test
    void shouldFailWithExpiredRefreshToken() {
        createCredentials("TestName", "TestPassword", "USER_ID", Agent.system());
        var loginResult = useCredentials("TestName", "TestPassword", Agent.anonymous());

        clock.add(Duration.ofDays(8));

        assertThatThrownBy(() -> refreshTokens(loginResult.getRefreshToken()))
                .hasMessageContaining("Refresh token expired");
    }

    @Test
    void shouldAllowChainedRefreshes() {
        // given: some credentials and a login result
        createCredentials("TestName", "TestPassword", "USER_ID", Agent.system());
        var loginResult = useCredentials("TestName", "TestPassword", Agent.anonymous());

        // when: refreshing multiple times in succession
        var refresh1 = refreshTokens(loginResult.getRefreshToken());
        var refresh2 = refreshTokens(refresh1.getRefreshToken());
        var refresh3 = refreshTokens(refresh2.getRefreshToken());

        // then: each refresh returns valid tokens
        assertThat(refresh3.getAccessToken()).isEqualTo("token-for-USER_ID");
        assertThat(refresh3.getRefreshToken()).isNotBlank();
    }

}
