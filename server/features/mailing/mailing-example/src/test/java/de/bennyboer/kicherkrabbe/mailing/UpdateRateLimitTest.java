package de.bennyboer.kicherkrabbe.mailing;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.mailing.api.requests.UpdateRateLimitRequest;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UpdateRateLimitTest extends MailingModuleTest {

    @Test
    void shouldUpdateRateLimit() {
        // given: the current user is allowed to read and manage settings
        allowUserToReadAndManageSettings("USER_ID");

        // when: updating the rate limit
        var settings = getSettings(Agent.user(AgentId.of("USER_ID")));
        var request = new UpdateRateLimitRequest();
        request.version = settings.settings.version;
        request.durationInMs = 60 * 60 * 1000;
        request.limit = 50;
        updateRateLimit(request, Agent.user(AgentId.of("USER_ID")));

        // then: the rate limit is updated
        var updatedSettings = getSettings(Agent.user(AgentId.of("USER_ID")));
        assertThat(updatedSettings.settings.version).isEqualTo(1L);
        assertThat(updatedSettings.settings.rateLimit.durationInMs).isEqualTo(60 * 60 * 1000);
        assertThat(updatedSettings.settings.rateLimit.limit).isEqualTo(50);
    }

    @Test
    void shouldRaiseErrorWhenRequestIsInvalid() {
        // given: the current user is allowed to read and manage settings
        allowUserToReadAndManageSettings("USER_ID");

        // when: updating the rate limit with a negative limit; then: an exception is thrown
        var settings = getSettings(Agent.user(AgentId.of("USER_ID")));
        var request = new UpdateRateLimitRequest();
        request.version = settings.settings.version;
        request.durationInMs = 60 * 60 * 1000;
        request.limit = -10;
        assertThatThrownBy(() -> updateRateLimit(request, Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e instanceof IllegalArgumentException);

        // when: updating the rate limit with a negative duration; then: an exception is thrown
        request.durationInMs = -60 * 60 * 1000;
        request.limit = 50;
        assertThatThrownBy(() -> updateRateLimit(request, Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e instanceof IllegalArgumentException);

        // when: updating the rate limit with a zero duration; then: an exception is thrown
        request.durationInMs = 0;
        assertThatThrownBy(() -> updateRateLimit(request, Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e instanceof IllegalArgumentException);
    }

    @Test
    void shouldNotUpdateRateLimitWhenUserDoesNotHavePermission() {
        // given: the user does not have permission to manage settings

        // when: the user updates the rate limit; then: an exception is thrown
        var request = new UpdateRateLimitRequest();
        request.version = 0L;
        request.durationInMs = 60 * 60 * 1000;
        request.limit = 50;
        assertThatThrownBy(() -> updateRateLimit(request, Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldRaiseExceptionGivenAnOutdatedVersion() {
        // given: the current user is allowed to read and manage settings
        allowUserToReadAndManageSettings("USER_ID");

        // and: the rate limit is updated
        var settings = getSettings(Agent.user(AgentId.of("USER_ID")));
        var request = new UpdateRateLimitRequest();
        request.version = settings.settings.version;
        request.durationInMs = 60 * 60 * 1000;
        request.limit = 50;
        updateRateLimit(request, Agent.user(AgentId.of("USER_ID")));

        // when: updating the rate limit with an outdated version; then: an exception is thrown
        assertThatThrownBy(() -> updateRateLimit(request, Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

}
