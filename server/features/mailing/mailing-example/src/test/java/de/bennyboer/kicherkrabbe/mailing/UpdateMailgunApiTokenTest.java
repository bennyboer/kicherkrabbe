package de.bennyboer.kicherkrabbe.mailing;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.mailing.api.requests.UpdateMailgunApiTokenRequest;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UpdateMailgunApiTokenTest extends MailingModuleTest {

    @Test
    void shouldUpdateMailgunApiToken() {
        // given: the current user is allowed to read and manage settings
        allowUserToReadAndManageSettings("USER_ID");

        // when: updating the mailgun API token
        var settings = getSettings(Agent.user(AgentId.of("USER_ID")));
        var request = new UpdateMailgunApiTokenRequest();
        request.version = settings.settings.version;
        request.apiToken = "SOME_API_TOKEN";
        updateMailgunApiToken(request, Agent.user(AgentId.of("USER_ID")));

        // then: the mailgun API token is updated
        var updatedSettings = getSettings(Agent.user(AgentId.of("USER_ID")));
        assertThat(updatedSettings.settings.version).isEqualTo(1L);
        assertThat(updatedSettings.settings.mailgun.maskedApiToken).isEqualTo("****OKEN");
    }

    @Test
    void shouldRaiseErrorWhenRequestIsInvalid() {
        // given: the current user is allowed to read and manage settings
        allowUserToReadAndManageSettings("USER_ID");

        // when: updating the mailgun API token with a null token; then: an exception is thrown
        var settings = getSettings(Agent.user(AgentId.of("USER_ID")));
        var request = new UpdateMailgunApiTokenRequest();
        request.version = settings.settings.version;
        request.apiToken = null;
        assertThatThrownBy(() -> updateMailgunApiToken(request, Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e instanceof IllegalArgumentException);

        // when: updating the mailgun API token with an empty token; then: an exception is thrown
        request.apiToken = "";
        assertThatThrownBy(() -> updateMailgunApiToken(request, Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e instanceof IllegalArgumentException);
    }

    @Test
    void shouldNotUpdateMailgunApiTokenWhenUserDoesNotHavePermission() {
        // given: the user does not have permission to manage settings

        // when: the user updates the mailgun API token; then: an exception is thrown
        var request = new UpdateMailgunApiTokenRequest();
        request.version = 0L;
        request.apiToken = "SOME_API_TOKEN";
        assertThatThrownBy(() -> updateMailgunApiToken(request, Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldRaiseExceptionGivenAnOutdatedVersion() {
        // given: the current user is allowed to read and manage settings
        allowUserToReadAndManageSettings("USER_ID");

        // and: the mailgun API token is updated
        var settings = getSettings(Agent.user(AgentId.of("USER_ID")));
        var request = new UpdateMailgunApiTokenRequest();
        request.version = settings.settings.version;
        request.apiToken = "SOME_API_TOKEN";
        updateMailgunApiToken(request, Agent.user(AgentId.of("USER_ID")));

        // when: updating the mailgun API token with an outdated version; then: an exception is thrown
        assertThatThrownBy(() -> updateMailgunApiToken(request, Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

}
