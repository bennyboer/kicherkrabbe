package de.bennyboer.kicherkrabbe.mailing;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.mailing.api.requests.ClearMailgunApiTokenRequest;
import de.bennyboer.kicherkrabbe.mailing.api.requests.UpdateMailgunApiTokenRequest;
import de.bennyboer.kicherkrabbe.mailing.settings.MailgunApiTokenAlreadyClearedException;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ClearMailgunApiTokenTest extends MailingModuleTest {

    @Test
    void shouldClearMailgunApiToken() {
        // given: the current user is allowed to read and manage settings
        allowUserToReadAndManageSettings("USER_ID");

        // and. the mailgun API token is set
        var settings = getSettings(Agent.user(AgentId.of("USER_ID")));
        var updateMailgunApiTokenRequest = new UpdateMailgunApiTokenRequest();
        updateMailgunApiTokenRequest.version = settings.settings.version;
        updateMailgunApiTokenRequest.apiToken = "SOME_API_TOKEN";
        var version = updateMailgunApiToken(updateMailgunApiTokenRequest, Agent.user(AgentId.of("USER_ID"))).version;

        // when: clearing the mailgun API token
        var request = new ClearMailgunApiTokenRequest();
        request.version = version;
        version = clearMailgunApiToken(request, Agent.user(AgentId.of("USER_ID"))).version;

        // then: the mailgun API token is cleared
        var updatedSettings = getSettings(Agent.user(AgentId.of("USER_ID")));
        assertThat(updatedSettings.settings.version).isEqualTo(version);
        assertThat(updatedSettings.settings.mailgun.maskedApiToken).isNull();
    }

    @Test
    void shouldRaiseErrorIfMailgunApiTokenIsAlreadyCleared() {
        // given: the current user is allowed to read and manage settings
        allowUserToReadAndManageSettings("USER_ID");

        // when: clearing the mailgun API token; then: an error is raised
        var settings = getSettings(Agent.user(AgentId.of("USER_ID")));
        var request = new ClearMailgunApiTokenRequest();
        request.version = settings.settings.version;
        assertThatThrownBy(() -> clearMailgunApiToken(request, Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e instanceof MailgunApiTokenAlreadyClearedException);
    }

    @Test
    void shouldNotClearMailgunApiTokenWhenUserDoesNotHavePermission() {
        // given: the user does not have permission to manage settings

        // when: the user clears the mailgun API token; then: an exception is thrown
        var request = new ClearMailgunApiTokenRequest();
        request.version = 0L;
        assertThatThrownBy(() -> clearMailgunApiToken(request, Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldRaiseExceptionGivenAnOutdatedVersion() {
        // given: the current user is allowed to read and manage settings
        allowUserToReadAndManageSettings("USER_ID");

        // and: the mailgun API token is updated
        var settings = getSettings(Agent.user(AgentId.of("USER_ID")));
        var updateMailgunApiTokenRequest = new UpdateMailgunApiTokenRequest();
        updateMailgunApiTokenRequest.version = settings.settings.version;
        updateMailgunApiTokenRequest.apiToken = "SOME_API_TOKEN";
        updateMailgunApiToken(updateMailgunApiTokenRequest, Agent.user(AgentId.of("USER_ID")));

        // when: clearing the mailgun API token with an outdated version; then: an exception is thrown
        var request = new ClearMailgunApiTokenRequest();
        request.version = 0L;
        assertThatThrownBy(() -> clearMailgunApiToken(request, Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

}
