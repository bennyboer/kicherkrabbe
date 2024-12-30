package de.bennyboer.kicherkrabbe.telegram;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import de.bennyboer.kicherkrabbe.telegram.api.requests.UpdateBotApiTokenRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UpdateBotApiTokenTest extends TelegramModuleTest {

    @Test
    void shouldUpdateBotApiToken() {
        // given: the current user is allowed to read and manage settings
        allowUserToReadAndManageSettings("USER_ID");

        // and: the api token for the bot is configured
        var settings = getSettings(Agent.user(AgentId.of("USER_ID")));
        var updateBotApiTokenRequest = new UpdateBotApiTokenRequest();
        updateBotApiTokenRequest.version = settings.settings.version;
        updateBotApiTokenRequest.apiToken = "SOME_API_TOKEN";
        updateBotApiToken(updateBotApiTokenRequest, Agent.user(AgentId.of("USER_ID")));

        // when: the settings are queried
        var response = getSettings(Agent.user(AgentId.of("USER_ID")));

        // then: the response contains the masked api token
        assertThat(response.settings.version).isEqualTo(1L);
        assertThat(response.settings.botSettings.maskedApiToken).isEqualTo("****OKEN");
    }

    @Test
    void shouldNotUpdateBotApiTokenWhenUserDoesNotHavePermission() {
        // given: the current user is not allowed to read and manage settings

        // when: the settings are queried; then: an error is raised
        var request = new UpdateBotApiTokenRequest();
        request.version = 0L;
        request.apiToken = "SOME_API_TOKEN";
        assertThatThrownBy(() -> updateBotApiToken(request, Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldRaiseErrorWhenVersionIsOutdated() {
        // given: the current user is allowed to read and manage settings
        allowUserToReadAndManageSettings("USER_ID");

        // and: the api token for the bot is configured
        var settings = getSettings(Agent.user(AgentId.of("USER_ID")));
        var updateBotApiTokenRequest = new UpdateBotApiTokenRequest();
        updateBotApiTokenRequest.version = settings.settings.version;
        updateBotApiTokenRequest.apiToken = "SOME_API_TOKEN";
        updateBotApiToken(updateBotApiTokenRequest, Agent.user(AgentId.of("USER_ID")));

        // when: updating the bot API token with an outdated version; then: an error is raised
        assertThatThrownBy(() -> updateBotApiToken(updateBotApiTokenRequest, Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

}
