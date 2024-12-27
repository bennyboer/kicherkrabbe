package de.bennyboer.kicherkrabbe.notifications;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.notifications.api.ChannelDTO;
import de.bennyboer.kicherkrabbe.notifications.api.ChannelTypeDTO;
import de.bennyboer.kicherkrabbe.notifications.api.TelegramDTO;
import de.bennyboer.kicherkrabbe.notifications.api.requests.ActivateSystemChannelRequest;
import de.bennyboer.kicherkrabbe.notifications.api.requests.UpdateSystemChannelRequest;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class QuerySettingsTest extends NotificationsModuleTest {

    @Test
    void shouldQuerySettings() {
        // given: the current user is allowed to read notifications and manage settings
        allowUserToReadNotificationsAndManageSettings("USER_ID");

        // and: the user enabled the system notifications
        var settings = getSettings(Agent.user(AgentId.of("USER_ID")));
        enableSystemNotifications(settings.settings.version, Agent.user(AgentId.of("USER_ID")));

        // and: the user configured the system to send notifications via mail and telegram
        var updateMailChannelRequest = new UpdateSystemChannelRequest();
        updateMailChannelRequest.version = settings.settings.version + 1;
        updateMailChannelRequest.channel = new ChannelDTO();
        updateMailChannelRequest.channel.type = ChannelTypeDTO.EMAIL;
        updateMailChannelRequest.channel.mail = "john.doe@kicherkrabbe.com";
        updateSystemChannel(updateMailChannelRequest, Agent.user(AgentId.of("USER_ID")));

        var activateMailChannelRequest = new ActivateSystemChannelRequest();
        activateMailChannelRequest.version = settings.settings.version + 2;
        activateMailChannelRequest.channelType = ChannelTypeDTO.EMAIL;
        activateSystemChannel(activateMailChannelRequest, Agent.user(AgentId.of("USER_ID")));

        var updateTelegramChannelRequest = new UpdateSystemChannelRequest();
        updateTelegramChannelRequest.version = settings.settings.version + 3;
        updateTelegramChannelRequest.channel = new ChannelDTO();
        updateTelegramChannelRequest.channel.type = ChannelTypeDTO.TELEGRAM;
        updateTelegramChannelRequest.channel.telegram = new TelegramDTO();
        updateTelegramChannelRequest.channel.telegram.chatId = "CHAT_ID";
        updateSystemChannel(updateTelegramChannelRequest, Agent.user(AgentId.of("USER_ID")));

        var activateTelegramChannelRequest = new ActivateSystemChannelRequest();
        activateTelegramChannelRequest.version = settings.settings.version + 4;
        activateTelegramChannelRequest.channelType = ChannelTypeDTO.TELEGRAM;
        activateSystemChannel(activateTelegramChannelRequest, Agent.user(AgentId.of("USER_ID")));

        // when: querying the settings
        settings = getSettings(Agent.user(AgentId.of("USER_ID")));

        // then: the response contains the expected settings
        assertThat(settings.settings.version).isEqualTo(5L);
        assertThat(settings.settings.systemSettings.enabled).isTrue();

        var mailChannel = settings.settings.systemSettings.channels.stream()
                .filter(channel -> channel.channel.type == ChannelTypeDTO.EMAIL)
                .findFirst()
                .orElseThrow();
        assertThat(mailChannel.active).isTrue();
        assertThat(mailChannel.channel.mail).isEqualTo("john.doe@kicherkrabbe.com");

        var telegramChannel = settings.settings.systemSettings.channels.stream()
                .filter(channel -> channel.channel.type == ChannelTypeDTO.TELEGRAM)
                .findFirst()
                .orElseThrow();
        assertThat(telegramChannel.active).isTrue();
        assertThat(telegramChannel.channel.telegram.chatId).isEqualTo("CHAT_ID");
    }

    @Test
    void shouldNotQuerySettingsWhenUserDoesNotHavePermission() {
        // given: the user does not have permission to read settings

        // when: the user queries the settings; then: an exception is thrown
        assertThatThrownBy(() -> getSettings(Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldReturnInitialSettingsState() {
        // given: the user is allowed to read notifications and manage settings
        allowUserToReadNotificationsAndManageSettings("USER_ID");

        // when: querying the settings
        var settings = getSettings(Agent.user(AgentId.of("USER_ID")));

        // then: the response contains the initial settings state
        assertThat(settings.settings.version).isEqualTo(0L);
        assertThat(settings.settings.systemSettings.enabled).isFalse();
        assertThat(settings.settings.systemSettings.channels).isEmpty();
    }

}
