package de.bennyboer.kicherkrabbe.notifications;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.notifications.api.*;
import de.bennyboer.kicherkrabbe.notifications.api.requests.ActivateSystemChannelRequest;
import de.bennyboer.kicherkrabbe.notifications.api.requests.SendNotificationRequest;
import de.bennyboer.kicherkrabbe.notifications.api.requests.UpdateSystemChannelRequest;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class QueryNotificationsTest extends NotificationsModuleTest {

    @Test
    void shouldQueryNotifications() {
        // given: the system user is allowed to send notifications
        allowSystemUserToSendNotifications();

        // and: the current user is allowed to read notifications and manage settings
        allowUserToReadNotificationsAndManageSettings("USER_ID");

        // and: the user configured the system to send notifications via mail and telegram
        var settings = getSettings(Agent.user(AgentId.of("USER_ID")));

        var updateMailChannelRequest = new UpdateSystemChannelRequest();
        updateMailChannelRequest.version = settings.settings.version;
        updateMailChannelRequest.channel = new ChannelDTO();
        updateMailChannelRequest.channel.type = ChannelTypeDTO.EMAIL;
        updateMailChannelRequest.channel.mail = "john.doe@kicherkrabbe.com";
        updateSystemChannel(updateMailChannelRequest, Agent.user(AgentId.of("USER_ID")));

        var activateMailChannelRequest = new ActivateSystemChannelRequest();
        activateMailChannelRequest.version = settings.settings.version + 1;
        activateMailChannelRequest.channelType = ChannelTypeDTO.EMAIL;
        activateSystemChannel(activateMailChannelRequest, Agent.user(AgentId.of("USER_ID")));

        var updateTelegramChannelRequest = new UpdateSystemChannelRequest();
        updateTelegramChannelRequest.version = settings.settings.version + 2;
        updateTelegramChannelRequest.channel = new ChannelDTO();
        updateTelegramChannelRequest.channel.type = ChannelTypeDTO.TELEGRAM;
        updateTelegramChannelRequest.channel.telegram = new TelegramDTO();
        updateTelegramChannelRequest.channel.telegram.chatId = "CHAT_ID";
        updateSystemChannel(updateTelegramChannelRequest, Agent.user(AgentId.of("USER_ID")));

        var activateTelegramChannelRequest = new ActivateSystemChannelRequest();
        activateTelegramChannelRequest.version = settings.settings.version + 3;
        activateTelegramChannelRequest.channelType = ChannelTypeDTO.TELEGRAM;
        activateSystemChannel(activateTelegramChannelRequest, Agent.user(AgentId.of("USER_ID")));

        // and: some sent notifications at different times
        setTime(Instant.parse("2024-12-08T10:15:30.000Z"));
        var request1 = new SendNotificationRequest();
        request1.origin = new OriginDTO();
        request1.origin.type = OriginTypeDTO.MAIL;
        request1.origin.id = "MAIL_ID_1";
        request1.target = new TargetDTO();
        request1.target.type = TargetTypeDTO.SYSTEM;
        request1.target.id = "SYSTEM";
        request1.title = "Mail received";
        request1.message = "You have received a new mail";
        sendNotification(request1, Agent.system());

        setTime(Instant.parse("2024-12-09T12:00:00.000Z"));
        var request2 = new SendNotificationRequest();
        request2.origin = new OriginDTO();
        request2.origin.type = OriginTypeDTO.MAIL;
        request2.origin.id = "MAIL_ID_2";
        request2.target = new TargetDTO();
        request2.target.type = TargetTypeDTO.SYSTEM;
        request2.target.id = "SYSTEM";
        request2.title = "Mail received";
        request2.message = "You have received a new mail";
        sendNotification(request2, Agent.system());

        // when: querying the notifications
        var notifications = getNotifications(DateRangeFilter.empty(), 0, 10, Agent.user(AgentId.of("USER_ID")));

        // then: the response contains the expected notifications ordered in descending order by sentAt
        assertThat(notifications.total).isEqualTo(2);
        assertThat(notifications.notifications).hasSize(2);

        var notification1 = notifications.notifications.get(0);
        assertThat(notification1.origin.type).isEqualTo(OriginTypeDTO.MAIL);
        assertThat(notification1.origin.id).isEqualTo("MAIL_ID_2");
        assertThat(notification1.target.type).isEqualTo(TargetTypeDTO.SYSTEM);
        assertThat(notification1.target.id).isEqualTo("SYSTEM");
        assertThat(notification1.title).isEqualTo("Mail received");
        assertThat(notification1.message).isEqualTo("You have received a new mail");
        assertThat(notification1.sentAt).isEqualTo(Instant.parse("2024-12-09T12:00:00.000Z"));
        assertThat(notification1.channels).hasSize(2);

        var mailChannel1 = notification1.channels
                .stream()
                .filter(channel -> channel.type == ChannelTypeDTO.EMAIL)
                .findFirst()
                .orElseThrow();
        assertThat(mailChannel1.mail).isEqualTo("john.doe@kicherkrabbe.com");

        var telegramChannel1 = notification1.channels
                .stream()
                .filter(channel -> channel.type == ChannelTypeDTO.TELEGRAM)
                .findFirst()
                .orElseThrow();
        assertThat(telegramChannel1.telegram.chatId).isEqualTo("CHAT_ID");

        var notification2 = notifications.notifications.get(1);
        assertThat(notification2.origin.type).isEqualTo(OriginTypeDTO.MAIL);
        assertThat(notification2.origin.id).isEqualTo("MAIL_ID_1");
        assertThat(notification2.target.type).isEqualTo(TargetTypeDTO.SYSTEM);
        assertThat(notification2.target.id).isEqualTo("SYSTEM");
        assertThat(notification2.title).isEqualTo("Mail received");
        assertThat(notification2.message).isEqualTo("You have received a new mail");
        assertThat(notification2.sentAt).isEqualTo(Instant.parse("2024-12-08T10:15:30.000Z"));
        assertThat(notification2.channels).hasSize(2);

        var mailChannel2 = notification2.channels
                .stream()
                .filter(channel -> channel.type == ChannelTypeDTO.EMAIL)
                .findFirst()
                .orElseThrow();
        assertThat(mailChannel2.mail).isEqualTo("john.doe@kicherkrabbe.com");

        var telegramChannel2 = notification2.channels
                .stream()
                .filter(channel -> channel.type == ChannelTypeDTO.TELEGRAM)
                .findFirst()
                .orElseThrow();
        assertThat(telegramChannel2.telegram.chatId).isEqualTo("CHAT_ID");

        // when: querying the notifications with paging
        var notificationsPage1 = getNotifications(DateRangeFilter.empty(), 0, 1, Agent.user(AgentId.of("USER_ID")));

        // then: the response contains the expected notifications
        assertThat(notificationsPage1.total).isEqualTo(2);
        assertThat(notificationsPage1.notifications).hasSize(1);
        assertThat(notificationsPage1.notifications.get(0)).isEqualTo(notification1);

        // when: querying the notifications with date range filter
        var notificationsPage2 = getNotifications(
                DateRangeFilter.of(
                        Instant.parse("2024-12-08T00:00:00.000Z"),
                        Instant.parse("2024-12-08T23:59:59.999Z")
                ),
                0,
                10,
                Agent.user(AgentId.of("USER_ID"))
        );

        // then: the response contains the expected notifications
        assertThat(notificationsPage2.total).isEqualTo(1);
        assertThat(notificationsPage2.notifications).hasSize(1);
        assertThat(notificationsPage2.notifications.get(0)).isEqualTo(notification2);
    }

    @Test
    void shouldNotQueryNotificationWhenUserDoesNotHavePermission() {
        // given: the user does not have permission to read notifications

        // when: the user queries the notifications; then: an exception is thrown
        assertThatThrownBy(() -> getNotifications(DateRangeFilter.empty(), 0, 10, Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldReturnEmptyListIfNoNotificationIsReceivedYet() {
        // given: the user is allowed to read notifications and manage settings
        allowUserToReadNotificationsAndManageSettings("USER_ID");

        // when: querying the notifications
        var notifications = getNotifications(DateRangeFilter.empty(), 0, 10, Agent.user(AgentId.of("USER_ID")));

        // then: the response contains no notifications
        assertThat(notifications.total).isEqualTo(0);
        assertThat(notifications.notifications).isEmpty();
    }

}
