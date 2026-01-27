package de.bennyboer.kicherkrabbe.telegram;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import de.bennyboer.kicherkrabbe.telegram.api.requests.ClearBotApiTokenRequest;
import de.bennyboer.kicherkrabbe.telegram.api.requests.UpdateBotApiTokenRequest;
import de.bennyboer.kicherkrabbe.telegram.settings.BotApiTokenAlreadyClearedException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ClearBotApiTokenTest extends TelegramModuleTest {

    @Test
    void shouldClearBotApiToken() {
        // given: the current user is allowed to read and manage settings
        allowUserToReadAndManageSettings("USER_ID");

        // and: the api token for the bot is configured
        var settings = getSettings(Agent.user(AgentId.of("USER_ID")));
        var updateBotApiTokenRequest = new UpdateBotApiTokenRequest();
        updateBotApiTokenRequest.version = settings.settings.version;
        updateBotApiTokenRequest.apiToken = "SOME_API_TOKEN";
        var version = updateBotApiToken(updateBotApiTokenRequest, Agent.user(AgentId.of("USER_ID"))).version;

        // when: the bot API token is cleared
        var clearBotApiTokenRequest = new ClearBotApiTokenRequest();
        clearBotApiTokenRequest.version = version;
        version = clearBotApiToken(clearBotApiTokenRequest, Agent.user(AgentId.of("USER_ID"))).version;

        // then: the bot API token is cleared
        var response = getSettings(Agent.user(AgentId.of("USER_ID")));
        assertThat(response.settings.version).isEqualTo(version);
        assertThat(response.settings.botSettings.maskedApiToken).isNull();
    }

    @Test
    void shouldRaiseErrorIfThereIsNotApiTokenToClear() {
        // given: the current user is allowed to read and manage settings
        allowUserToReadAndManageSettings("USER_ID");

        // when: the bot API token is cleared; then: an error is raised
        var settings = getSettings(Agent.user(AgentId.of("USER_ID")));
        var clearBotApiTokenRequest = new ClearBotApiTokenRequest();
        clearBotApiTokenRequest.version = settings.settings.version;
        assertThatThrownBy(() -> clearBotApiToken(clearBotApiTokenRequest, Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e instanceof BotApiTokenAlreadyClearedException);
    }

    @Test
    void shouldNotClearBotApiTokenWhenUserDoesNotHavePermission() {
        // given: the current user is not allowed to read and manage settings

        // when: the settings are queried; then: an error is raised
        var request = new ClearBotApiTokenRequest();
        request.version = 0L;
        assertThatThrownBy(() -> clearBotApiToken(request, Agent.user(AgentId.of("USER_ID"))))
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
        var version = updateBotApiToken(updateBotApiTokenRequest, Agent.user(AgentId.of("USER_ID"))).version;

        // and: the bot API token is updated once more
        updateBotApiTokenRequest.version = version;
        version = updateBotApiToken(updateBotApiTokenRequest, Agent.user(AgentId.of("USER_ID"))).version;

        // when: clearing the bot API token with an outdated version; then: an error is raised
        var clearBotApiTokenRequest = new ClearBotApiTokenRequest();
        clearBotApiTokenRequest.version = 1L;
        assertThatThrownBy(() -> clearBotApiToken(clearBotApiTokenRequest, Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

}
