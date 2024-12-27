package de.bennyboer.kicherkrabbe.notifications;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.notifications.api.*;
import de.bennyboer.kicherkrabbe.notifications.api.requests.ActivateSystemChannelRequest;
import de.bennyboer.kicherkrabbe.notifications.api.requests.SendNotificationRequest;
import de.bennyboer.kicherkrabbe.notifications.api.requests.UpdateSystemChannelRequest;
import de.bennyboer.kicherkrabbe.notifications.notification.SystemNotificationsDisabledException;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SendNotificationTest extends NotificationsModuleTest {

    @Test
    void shouldSendNotification() {
        // given: the system user is allowed to send notifications
        allowSystemUserToSendNotifications();

        // and: the current user is allowed to read notifications and manage settings
        allowUserToReadNotificationsAndManageSettings("USER_ID");

        // and: we are at a fixed point in time
        setTime(Instant.parse("2024-12-08T10:15:30.000Z"));

        // and: system notifications are enabled
        var settings = getSettings(Agent.user(AgentId.of("USER_ID")));
        var settingsVersion = enableSystemNotifications(
                settings.settings.version,
                Agent.user(AgentId.of("USER_ID"))
        ).version;

        // and: the user configured the system to send notifications via mail and telegram
        var updateMailChannelRequest = new UpdateSystemChannelRequest();
        updateMailChannelRequest.version = settingsVersion;
        updateMailChannelRequest.channel = new ChannelDTO();
        updateMailChannelRequest.channel.type = ChannelTypeDTO.EMAIL;
        updateMailChannelRequest.channel.mail = "john.doe@kicherkrabbe.com";
        settingsVersion = updateSystemChannel(updateMailChannelRequest, Agent.user(AgentId.of("USER_ID"))).version;

        var activateMailChannelRequest = new ActivateSystemChannelRequest();
        activateMailChannelRequest.version = settingsVersion;
        activateMailChannelRequest.channelType = ChannelTypeDTO.EMAIL;
        settingsVersion = activateSystemChannel(activateMailChannelRequest, Agent.user(AgentId.of("USER_ID"))).version;

        var updateTelegramChannelRequest = new UpdateSystemChannelRequest();
        updateTelegramChannelRequest.version = settingsVersion;
        updateTelegramChannelRequest.channel = new ChannelDTO();
        updateTelegramChannelRequest.channel.type = ChannelTypeDTO.TELEGRAM;
        updateTelegramChannelRequest.channel.telegram = new TelegramDTO();
        updateTelegramChannelRequest.channel.telegram.chatId = "CHAT_ID";
        settingsVersion = updateSystemChannel(updateTelegramChannelRequest, Agent.user(AgentId.of("USER_ID"))).version;

        var activateTelegramChannelRequest = new ActivateSystemChannelRequest();
        activateTelegramChannelRequest.version = settingsVersion;
        activateTelegramChannelRequest.channelType = ChannelTypeDTO.TELEGRAM;
        settingsVersion = activateSystemChannel(
                activateTelegramChannelRequest,
                Agent.user(AgentId.of("USER_ID"))
        ).version;

        // and: a request to send a notification
        var request = new SendNotificationRequest();
        request.origin = new OriginDTO();
        request.origin.type = OriginTypeDTO.MAIL;
        request.origin.id = "MAIL_ID";
        request.target = new TargetDTO();
        request.target.type = TargetTypeDTO.SYSTEM;
        request.target.id = "SYSTEM";
        request.title = "Mail received";
        request.message = "You have received a new mail";

        // when: the system user sends the notification
        var result = sendNotification(request, Agent.system());

        // then: the notification is sent
        assertThat(result.id).isNotBlank();
        assertThat(result.version).isEqualTo(0L);

        // and: the sent notification is correct
        var notifications = getNotifications(DateRangeFilter.empty(), 0, 10, Agent.user(AgentId.of("USER_ID")));
        assertThat(notifications.total).isEqualTo(1);
        assertThat(notifications.notifications).hasSize(1);

        var notification = notifications.notifications.stream().findFirst().orElseThrow();
        assertThat(notification.id).isEqualTo(result.id);
        assertThat(notification.version).isEqualTo(0L);
        assertThat(notification.origin.type).isEqualTo(OriginTypeDTO.MAIL);
        assertThat(notification.origin.id).isEqualTo("MAIL_ID");
        assertThat(notification.target.type).isEqualTo(TargetTypeDTO.SYSTEM);
        assertThat(notification.target.id).isEqualTo("SYSTEM");
        assertThat(notification.title).isEqualTo("Mail received");
        assertThat(notification.message).isEqualTo("You have received a new mail");
        assertThat(notification.sentAt).isEqualTo(Instant.parse("2024-12-08T10:15:30.000Z"));
        assertThat(notification.channels).hasSize(2);

        var mailChannel = notification.channels
                .stream()
                .filter(channel -> channel.type == ChannelTypeDTO.EMAIL)
                .findFirst()
                .orElseThrow();
        assertThat(mailChannel.mail).isEqualTo("john.doe@kicherkrabbe.com");

        var telegramChannel = notification.channels
                .stream()
                .filter(channel -> channel.type == ChannelTypeDTO.TELEGRAM)
                .findFirst()
                .orElseThrow();
        assertThat(telegramChannel.telegram.chatId).isEqualTo("CHAT_ID");
    }

    @Test
    void shouldNotSendSystemNotificationWhenSystemNotificationsAreDisabled() {
        // given: the system user is allowed to send notifications
        allowSystemUserToSendNotifications();

        // and: the current user is allowed to read notifications and manage settings
        allowUserToReadNotificationsAndManageSettings("USER_ID");

        // and: system notifications are disabled

        // and: the user configured the system to send notifications via mail and telegram
        var settings = getSettings(Agent.user(AgentId.of("USER_ID")));
        var settingsVersion = settings.settings.version;
        var updateMailChannelRequest = new UpdateSystemChannelRequest();
        updateMailChannelRequest.version = settingsVersion;
        updateMailChannelRequest.channel = new ChannelDTO();
        updateMailChannelRequest.channel.type = ChannelTypeDTO.EMAIL;
        updateMailChannelRequest.channel.mail = "john.doe@kicherkrabbe.com";
        settingsVersion = updateSystemChannel(updateMailChannelRequest, Agent.user(AgentId.of("USER_ID"))).version;

        var activateMailChannelRequest = new ActivateSystemChannelRequest();
        activateMailChannelRequest.version = settingsVersion;
        activateMailChannelRequest.channelType = ChannelTypeDTO.EMAIL;
        settingsVersion = activateSystemChannel(activateMailChannelRequest, Agent.user(AgentId.of("USER_ID"))).version;

        var updateTelegramChannelRequest = new UpdateSystemChannelRequest();
        updateTelegramChannelRequest.version = settingsVersion;
        updateTelegramChannelRequest.channel = new ChannelDTO();
        updateTelegramChannelRequest.channel.type = ChannelTypeDTO.TELEGRAM;
        updateTelegramChannelRequest.channel.telegram = new TelegramDTO();
        updateTelegramChannelRequest.channel.telegram.chatId = "CHAT_ID";
        settingsVersion = updateSystemChannel(updateTelegramChannelRequest, Agent.user(AgentId.of("USER_ID"))).version;

        var activateTelegramChannelRequest = new ActivateSystemChannelRequest();
        activateTelegramChannelRequest.version = settingsVersion;
        activateTelegramChannelRequest.channelType = ChannelTypeDTO.TELEGRAM;
        settingsVersion = activateSystemChannel(
                activateTelegramChannelRequest,
                Agent.user(AgentId.of("USER_ID"))
        ).version;

        // and: a request to send a system notification
        var request = new SendNotificationRequest();
        request.origin = new OriginDTO();
        request.origin.type = OriginTypeDTO.MAIL;
        request.origin.id = "MAIL_ID";
        request.target = new TargetDTO();
        request.target.type = TargetTypeDTO.SYSTEM;
        request.target.id = "SYSTEM";
        request.title = "Mail received";
        request.message = "You have received a new mail";

        // when: the system user sends the system notification; then: an exception is thrown
        assertThatThrownBy(() -> sendNotification(request, Agent.system()))
                .matches(e -> e instanceof SystemNotificationsDisabledException);
    }

    @Test
    void shouldNotSendNotificationWhenSystemUserDoesNotHavePermission() {
        // given: the system user is not allowed to sent notifications

        // and: a request to send a notification
        var request = new SendNotificationRequest();
        request.origin = new OriginDTO();
        request.origin.type = OriginTypeDTO.MAIL;
        request.origin.id = "MAIL_ID";
        request.target = new TargetDTO();
        request.target.type = TargetTypeDTO.SYSTEM;
        request.target.id = "SYSTEM";
        request.title = "Mail received";
        request.message = "You have received a new mail";

        // when: the system user sends the notification; then: an exception is thrown
        assertThatThrownBy(() -> sendNotification(request, Agent.system()))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldNotAcceptInvalidRequests() {
        // given: the system user is allowed to send notifications
        allowSystemUserToSendNotifications();

        // and: a request to receive a notification with an invalid origin
        var request = new SendNotificationRequest();
        request.origin = new OriginDTO();
        request.origin.type = OriginTypeDTO.MAIL;
        request.origin.id = "MAIL_ID";
        request.target = new TargetDTO();
        request.target.type = TargetTypeDTO.SYSTEM;
        request.target.id = "SYSTEM";
        request.title = "Mail received";
        request.message = "You have received a new mail";

        // when: the notification is sent with invalid origin type; then: an exception is thrown
        request.origin.type = null;
        assertThatThrownBy(() -> sendNotification(request, Agent.system()))
                .isInstanceOf(IllegalArgumentException.class);
        request.origin.type = OriginTypeDTO.MAIL;

        // when: the notification is sent with invalid origin ID; then: an exception is thrown
        request.origin.id = null;
        assertThatThrownBy(() -> sendNotification(request, Agent.system()))
                .isInstanceOf(IllegalArgumentException.class);
        request.origin.id = "MAIL_ID";

        // when: the notification is received with invalid target type; then: an exception is thrown
        request.target.type = null;
        assertThatThrownBy(() -> sendNotification(request, Agent.system()))
                .isInstanceOf(IllegalArgumentException.class);
        request.target.type = TargetTypeDTO.SYSTEM;

        // when: the notification is received with invalid target ID; then: an exception is thrown
        request.target.id = null;
        assertThatThrownBy(() -> sendNotification(request, Agent.system()))
                .isInstanceOf(IllegalArgumentException.class);
        request.target.id = "SYSTEM";

        // when: the notification is received with an invalid title; then: an exception is thrown
        request.title = null;
        assertThatThrownBy(() -> sendNotification(request, Agent.system()))
                .isInstanceOf(IllegalArgumentException.class);
        request.title = "Mail received";

        // when: the notification is received with an invalid title; then: an exception is thrown
        request.title = "";
        assertThatThrownBy(() -> sendNotification(request, Agent.system()))
                .isInstanceOf(IllegalArgumentException.class);
        request.title = "Mail received";

        // when: the notification is received with an invalid message; then: an exception is thrown
        request.message = null;
        assertThatThrownBy(() -> sendNotification(request, Agent.system()))
                .isInstanceOf(IllegalArgumentException.class);
        request.message = "You have received a new mail";

        // when: the notification is received with an invalid message; then: an exception is thrown
        request.message = "";
        assertThatThrownBy(() -> sendNotification(request, Agent.system()))
                .isInstanceOf(IllegalArgumentException.class);
    }

}
