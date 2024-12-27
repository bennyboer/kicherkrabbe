package de.bennyboer.kicherkrabbe.notifications;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.notifications.api.OriginDTO;
import de.bennyboer.kicherkrabbe.notifications.api.OriginTypeDTO;
import de.bennyboer.kicherkrabbe.notifications.api.TargetDTO;
import de.bennyboer.kicherkrabbe.notifications.api.TargetTypeDTO;
import de.bennyboer.kicherkrabbe.notifications.api.requests.SendNotificationRequest;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class CleanupOldNotificationsTest extends NotificationsModuleTest {

    @Test
    void shouldCleanupOldNotifications() {
        // given: the system user is allowed to send notifications
        allowSystemUserToSendNotifications();

        // and: the current user is allowed to read notifications and manage settings
        allowUserToReadNotificationsAndManageSettings("USER_ID");

        // and: some sent notifications at different times
        setTime(Instant.parse("2024-02-08T10:15:30.000Z"));
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

        setTime(Instant.parse("2024-03-02T12:00:00.000Z"));
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

        setTime(Instant.parse("2024-03-02T12:00:00.000Z").plus(90, ChronoUnit.DAYS).plusMillis(1));
        var request3 = new SendNotificationRequest();
        request3.origin = new OriginDTO();
        request3.origin.type = OriginTypeDTO.MAIL;
        request3.origin.id = "MAIL_ID_3";
        request3.target = new TargetDTO();
        request3.target.type = TargetTypeDTO.SYSTEM;
        request3.target.id = "SYSTEM";
        request3.title = "Mail received";
        request3.message = "You have received a new mail";
        sendNotification(request3, Agent.system());

        // when: cleaning up old notifications
        cleanupOldNotifications(Agent.system());

        // then: the first two notifications are removed since they are older than 90 days
        var notifications = getNotifications(DateRangeFilter.empty(), 0, 10, Agent.user(AgentId.of("USER_ID")));
        assertThat(notifications.total).isEqualTo(1);
        assertThat(notifications.notifications).hasSize(1);
        assertThat(notifications.notifications.get(0).origin.id).isEqualTo("MAIL_ID_3");
    }

}
