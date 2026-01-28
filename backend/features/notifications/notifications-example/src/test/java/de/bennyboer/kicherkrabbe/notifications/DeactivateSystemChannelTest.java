package de.bennyboer.kicherkrabbe.notifications;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.notifications.api.ChannelDTO;
import de.bennyboer.kicherkrabbe.notifications.api.ChannelTypeDTO;
import de.bennyboer.kicherkrabbe.notifications.api.TelegramDTO;
import de.bennyboer.kicherkrabbe.notifications.api.requests.ActivateSystemChannelRequest;
import de.bennyboer.kicherkrabbe.notifications.api.requests.DeactivateSystemChannelRequest;
import de.bennyboer.kicherkrabbe.notifications.api.requests.UpdateSystemChannelRequest;
import de.bennyboer.kicherkrabbe.notifications.settings.ChannelUnavailableException;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DeactivateSystemChannelTest extends NotificationsModuleTest {

    @Test
    void shouldDeactivateSystemChannel() {
        // given: the current user is allowed to read notifications and manage settings
        allowUserToReadNotificationsAndManageSettings("USER_ID");

        // and: some system channels are available
        var settings = getSettings(Agent.user(AgentId.of("USER_ID")));
        var request1 = new UpdateSystemChannelRequest();
        request1.version = settings.settings.version;
        request1.channel = new ChannelDTO();
        request1.channel.type = ChannelTypeDTO.EMAIL;
        request1.channel.mail = "john.doe@kicherkrabbe.com";
        var version = updateSystemChannel(request1, Agent.user(AgentId.of("USER_ID"))).version;

        var request2 = new UpdateSystemChannelRequest();
        request2.version = version;
        request2.channel = new ChannelDTO();
        request2.channel.type = ChannelTypeDTO.TELEGRAM;
        request2.channel.telegram = new TelegramDTO();
        request2.channel.telegram.chatId = "CHAT_ID";
        version = updateSystemChannel(request2, Agent.user(AgentId.of("USER_ID"))).version;

        // and: the channels are both activated
        var activateRequest = new ActivateSystemChannelRequest();
        activateRequest.version = version;
        activateRequest.channelType = ChannelTypeDTO.EMAIL;
        version = activateSystemChannel(activateRequest, Agent.user(AgentId.of("USER_ID"))).version;

        activateRequest = new ActivateSystemChannelRequest();
        activateRequest.version = version;
        activateRequest.channelType = ChannelTypeDTO.TELEGRAM;
        version = activateSystemChannel(activateRequest, Agent.user(AgentId.of("USER_ID"))).version;

        // when: deactivating the mail system channel
        var deactivateRequest = new DeactivateSystemChannelRequest();
        deactivateRequest.version = version;
        deactivateRequest.channelType = ChannelTypeDTO.EMAIL;
        version = deactivateSystemChannel(deactivateRequest, Agent.user(AgentId.of("USER_ID"))).version;

        // then: the mail system channel is deactivated, while the telegram system channel remains active
        settings = getSettings(Agent.user(AgentId.of("USER_ID")));
        assertThat(settings.settings.version).isEqualTo(version);
        assertThat(settings.settings.systemSettings.channels).hasSize(2);

        var mailChannel = settings.settings.systemSettings.channels.stream()
                .filter(c -> c.channel.type == ChannelTypeDTO.EMAIL)
                .findFirst()
                .orElseThrow();
        assertThat(mailChannel.active).isFalse();

        var telegramChannel = settings.settings.systemSettings.channels.stream()
                .filter(c -> c.channel.type == ChannelTypeDTO.TELEGRAM)
                .findFirst()
                .orElseThrow();
        assertThat(telegramChannel.active).isTrue();

        // when: deactivating the telegram system channel
        deactivateRequest = new DeactivateSystemChannelRequest();
        deactivateRequest.version = version;
        deactivateRequest.channelType = ChannelTypeDTO.TELEGRAM;
        version = deactivateSystemChannel(deactivateRequest, Agent.user(AgentId.of("USER_ID"))).version;

        // then: the telegram system channel is deactivated
        settings = getSettings(Agent.user(AgentId.of("USER_ID")));
        assertThat(settings.settings.version).isEqualTo(version);
        assertThat(settings.settings.systemSettings.channels).hasSize(2);

        mailChannel = settings.settings.systemSettings.channels.stream()
                .filter(c -> c.channel.type == ChannelTypeDTO.EMAIL)
                .findFirst()
                .orElseThrow();
        assertThat(mailChannel.active).isFalse();

        telegramChannel = settings.settings.systemSettings.channels.stream()
                .filter(c -> c.channel.type == ChannelTypeDTO.TELEGRAM)
                .findFirst()
                .orElseThrow();
        assertThat(telegramChannel.active).isFalse();
    }

    @Test
    void shouldNotDeactivateSystemChannelWhenUserDoesNotHavePermission() {
        // given: the user does not have permission to manage settings

        // when: the user activates system channel; then: an exception is thrown
        var request = new DeactivateSystemChannelRequest();
        request.version = 0L;
        request.channelType = ChannelTypeDTO.EMAIL;
        assertThatThrownBy(() -> deactivateSystemChannel(request, Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldRaiseExceptionIfChannelIsUnavailable() {
        // given: the current user is allowed to read notifications and manage settings
        allowUserToReadNotificationsAndManageSettings("USER_ID");

        // when: deactivating a system channel that is not available; then: an exception is thrown
        var settings = getSettings(Agent.user(AgentId.of("USER_ID")));
        var request = new DeactivateSystemChannelRequest();
        request.version = settings.settings.version;
        request.channelType = ChannelTypeDTO.EMAIL;
        assertThatThrownBy(() -> deactivateSystemChannel(request, Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e instanceof ChannelUnavailableException);
    }

    @Test
    void shouldRaiseExceptionGivenAnOutdatedVersion() {
        // given: the current user is allowed to read notifications and manage settings
        allowUserToReadNotificationsAndManageSettings("USER_ID");

        // and: some system channels are available
        var settings = getSettings(Agent.user(AgentId.of("USER_ID")));
        var request1 = new UpdateSystemChannelRequest();
        request1.version = settings.settings.version;
        request1.channel = new ChannelDTO();
        request1.channel.type = ChannelTypeDTO.EMAIL;
        request1.channel.mail = "john.doe@kicherkrabbe.com";
        var version = updateSystemChannel(request1, Agent.user(AgentId.of("USER_ID"))).version;

        // and: the system channel is activated
        var activateRequest = new ActivateSystemChannelRequest();
        activateRequest.version = version;
        activateRequest.channelType = ChannelTypeDTO.EMAIL;
        version = activateSystemChannel(activateRequest, Agent.user(AgentId.of("USER_ID"))).version;

        // and: the user enabled system notifications
        version = enableSystemNotifications(version, Agent.user(AgentId.of("USER_ID"))).version;

        // when: deactivating a system channel with an outdated version; then: an exception is thrown
        var request = new DeactivateSystemChannelRequest();
        request.version = 0L;
        request.channelType = ChannelTypeDTO.EMAIL;
        assertThatThrownBy(() -> deactivateSystemChannel(request, Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

}
