package de.bennyboer.kicherkrabbe.telegram;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import de.bennyboer.kicherkrabbe.telegram.api.requests.SendMessageViaBotRequest;
import de.bennyboer.kicherkrabbe.telegram.api.requests.UpdateBotApiTokenRequest;
import de.bennyboer.kicherkrabbe.telegram.settings.BotApiTokenMissingException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SendMessageViaBotTest extends TelegramModuleTest {

    @Test
    void shouldSendMessageViaBot() {
        // given: the system user is allowed to send messages via bot
        allowSystemUserToSendMessagesViaBot();

        // and: the current user is allowed to read and manage settings
        allowUserToReadAndManageSettings("USER_ID");

        // and: the api token for the bot is configured
        var settings = getSettings(Agent.user(AgentId.of("USER_ID")));
        var updateBotApiTokenRequest = new UpdateBotApiTokenRequest();
        updateBotApiTokenRequest.version = settings.settings.version;
        updateBotApiTokenRequest.apiToken = "SOME_API_TOKEN";
        updateBotApiToken(updateBotApiTokenRequest, Agent.user(AgentId.of("USER_ID")));

        // and: a request to send a message via bot
        var request = new SendMessageViaBotRequest();
        request.chatId = "SOME_CHAT_ID";
        request.text = "Hello, world!";

        // when: the system user sends the message
        sendMessageViaBot(request, Agent.system());

        // then: the message is sent via telegram API
        assertThat(telegramApi.getMessagesSentViaBot()).hasSize(1);
        var message = telegramApi.getMessagesSentViaBot().get(0);
        assertThat(message.getChatId().getValue()).isEqualTo("SOME_CHAT_ID");
        assertThat(message.getMessage().getValue()).isEqualTo("Hello, world!");
        assertThat(message.getBotApiToken().getValue()).isEqualTo("SOME_API_TOKEN");
    }

    @Test
    void shouldNotSendMessageViaBotWhenTheApiTokenIsNotConfigured() {
        // given: the system user is allowed to send messages via bot
        allowSystemUserToSendMessagesViaBot();

        // and: the current user is allowed to read and manage settings
        allowUserToReadAndManageSettings("USER_ID");

        // and: the api token for the bot is not configured

        // and: a request to send a message via bot
        var request = new SendMessageViaBotRequest();
        request.chatId = "SOME_CHAT_ID";
        request.text = "Hello, world!";

        // when: the system user sends the message; then: an error is raised
        assertThatThrownBy(() -> sendMessageViaBot(request, Agent.system()))
                .isInstanceOf(BotApiTokenMissingException.class);
    }

    @Test
    void shouldNotSendMessageWhenSystemUserDoesNotHavePermission() {
        // given: the system user is not allowed to send messages via bot

        // and: the current user is allowed to read and manage settings
        allowUserToReadAndManageSettings("USER_ID");

        // and: the api token for the bot is configured
        var settings = getSettings(Agent.user(AgentId.of("USER_ID")));
        var updateBotApiTokenRequest = new UpdateBotApiTokenRequest();
        updateBotApiTokenRequest.version = settings.settings.version;
        updateBotApiTokenRequest.apiToken = "SOME_API_TOKEN";
        updateBotApiToken(updateBotApiTokenRequest, Agent.user(AgentId.of("USER_ID")));

        // and: a request to send a message via bot
        var request = new SendMessageViaBotRequest();
        request.chatId = "SOME_CHAT_ID";
        request.text = "Hello, world!";

        // when: the system user sends the message; then: an error is raised
        assertThatThrownBy(() -> sendMessageViaBot(request, Agent.system()))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldNotAcceptInvalidRequests() {
        // given: the system user is allowed to send messages via bot
        allowSystemUserToSendMessagesViaBot();

        // and: the current user is allowed to read and manage settings
        allowUserToReadAndManageSettings("USER_ID");

        // and: the api token for the bot is configured
        var settings = getSettings(Agent.user(AgentId.of("USER_ID")));
        var updateBotApiTokenRequest = new UpdateBotApiTokenRequest();
        updateBotApiTokenRequest.version = settings.settings.version;
        updateBotApiTokenRequest.apiToken = "SOME_API_TOKEN";
        updateBotApiToken(updateBotApiTokenRequest, Agent.user(AgentId.of("USER_ID")));

        // and: a request to send a message via bot
        var request = new SendMessageViaBotRequest();
        request.chatId = "SOME_CHAT_ID";
        request.text = "SOME_MESSAGE";

        // when: the system user sends the message with a null text; then: an error is raised
        request.text = null;
        assertThatThrownBy(() -> sendMessageViaBot(request, Agent.system()))
                .isInstanceOf(IllegalArgumentException.class);
        request.text = "SOME_MESSAGE";

        // when: the system user sends an empty message; then: an error is raised
        request.text = "";
        assertThatThrownBy(() -> sendMessageViaBot(request, Agent.system()))
                .isInstanceOf(IllegalArgumentException.class);

        // when: the system user sends the message with a null chat ID; then: an error is raised
        request.chatId = null;
        assertThatThrownBy(() -> sendMessageViaBot(request, Agent.system()))
                .isInstanceOf(IllegalArgumentException.class);
        request.chatId = "SOME_CHAT_ID";

        // when: the system user sends an empty chat ID; then: an error is raised
        request.chatId = "";
        assertThatThrownBy(() -> sendMessageViaBot(request, Agent.system()))
                .isInstanceOf(IllegalArgumentException.class);
    }

}
