package de.bennyboer.kicherkrabbe.notifications.persistence.lookup.notification;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.notifications.DateRangeFilter;
import de.bennyboer.kicherkrabbe.notifications.channel.Channel;
import de.bennyboer.kicherkrabbe.notifications.channel.mail.EMail;
import de.bennyboer.kicherkrabbe.notifications.notification.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class NotificationLookupRepoTest {

    private NotificationLookupRepo repo;

    protected abstract NotificationLookupRepo createRepo();

    @BeforeEach
    public void setUp() {
        repo = createRepo();
    }

    @Test
    void shouldUpdateNotification() {
        // given: a notification to update
        var notification = LookupNotification.of(
                NotificationId.create(),
                Version.zero(),
                Origin.of(OriginType.MAIL, OriginId.of("MAIL_ID")),
                Target.system(),
                Set.of(Channel.mail(EMail.of("john.doe@kicherkrabbe.com"))),
                Title.of("New mail"),
                Message.of("You have received a new mail."),
                Instant.parse("2024-03-12T12:30:00.00Z")
        );

        // when: updating the notification
        update(notification);

        // then: the notification is updated
        var actualNotification = findById(notification.getId());
        assertThat(actualNotification).isEqualTo(notification);
    }

    @Test
    void shouldRemoveNotification() {
        // given: some notifications
        var notification1 = LookupNotification.of(
                NotificationId.create(),
                Version.zero(),
                Origin.of(OriginType.MAIL, OriginId.of("MAIL_ID_1")),
                Target.system(),
                Set.of(Channel.mail(EMail.of("john.doe@kicherkrabbe.com"))),
                Title.of("New mail"),
                Message.of("You have received a new mail."),
                Instant.parse("2024-03-12T12:30:00.00Z")
        );
        var notification2 = LookupNotification.of(
                NotificationId.create(),
                Version.zero(),
                Origin.of(OriginType.MAIL, OriginId.of("MAIL_ID_2")),
                Target.system(),
                Set.of(Channel.mail(EMail.of("john.doe@kicherkrabbe.com"))),
                Title.of("New mail"),
                Message.of("You have received a new mail."),
                Instant.parse("2024-03-12T12:45:00.00Z")
        );
        update(notification1);
        update(notification2);

        // when: removing a notification
        remove(notification1.getId());

        // then: the notification is removed
        var actualNotification = findById(notification1.getId());
        assertThat(actualNotification).isNull();

        // and: the other notification is still there
        actualNotification = findById(notification2.getId());
        assertThat(actualNotification).isEqualTo(notification2);
    }

    @Test
    void shouldQueryNotifications() {
        // given: some notifications
        var notification1 = LookupNotification.of(
                NotificationId.create(),
                Version.zero(),
                Origin.of(OriginType.MAIL, OriginId.of("MAIL_ID_1")),
                Target.system(),
                Set.of(Channel.mail(EMail.of("john.doe@kicherkrabbe.com"))),
                Title.of("New mail"),
                Message.of("You have received a new mail."),
                Instant.parse("2024-03-12T12:30:00.00Z")
        );
        var notification2 = LookupNotification.of(
                NotificationId.create(),
                Version.zero(),
                Origin.of(OriginType.MAIL, OriginId.of("MAIL_ID_2")),
                Target.system(),
                Set.of(Channel.mail(EMail.of("john.doe@kicherkrabbe.com"))),
                Title.of("New mail"),
                Message.of("You have received a new mail."),
                Instant.parse("2024-03-12T12:45:00.00Z")
        );
        update(notification1);
        update(notification2);

        // when: querying all notifications
        var page = query(DateRangeFilter.empty(), 0, 10);

        // then: all notifications are found ordered reversed by creation date
        assertThat(page.getTotal()).isEqualTo(2);
        assertThat(page.getNotifications()).containsExactly(notification2, notification1);

        // when: querying notifications with paging
        page = query(DateRangeFilter.empty(), 1, 1);

        // then: only the first notification is found
        assertThat(page.getTotal()).isEqualTo(2);
        assertThat(page.getNotifications()).containsExactly(notification1);

        // when: querying notifications with a date range filter
        page = query(
                DateRangeFilter.of(
                        Instant.parse("2024-03-12T12:40:00.00Z"),
                        Instant.parse("2024-03-12T12:46:00.00Z")
                ), 0, 10
        );

        // then: only the second notification is found
        assertThat(page.getTotal()).isEqualTo(1);
        assertThat(page.getNotifications()).containsExactly(notification2);

        // when: querying notifications with another date range filter
        page = query(
                DateRangeFilter.of(
                        null,
                        Instant.parse("2024-03-12T12:35:00.00Z")
                ), 0, 10
        );

        // then: only the first notification is found
        assertThat(page.getTotal()).isEqualTo(1);
        assertThat(page.getNotifications()).containsExactly(notification1);

        // when: querying notifications with another date range filter
        page = query(
                DateRangeFilter.of(
                        Instant.parse("2024-03-12T12:35:00.00Z"),
                        null
                ), 0, 10
        );

        // then: only the second notification is found
        assertThat(page.getTotal()).isEqualTo(1);
        assertThat(page.getNotifications()).containsExactly(notification2);

        // when: querying notifications with another date range filter
        page = query(
                DateRangeFilter.of(
                        Instant.parse("2024-03-12T12:35:00.00Z"),
                        Instant.parse("2024-03-12T12:40:00.00Z")
                ), 0, 10
        );

        // then: no notification is found
        assertThat(page.getTotal()).isEqualTo(0);
        assertThat(page.getNotifications()).isEmpty();
    }

    @Test
    void shouldFindOldNotifications() {
        // given: some notifications
        var notification1 = LookupNotification.of(
                NotificationId.create(),
                Version.zero(),
                Origin.of(OriginType.MAIL, OriginId.of("MAIL_ID_1")),
                Target.system(),
                Set.of(Channel.mail(EMail.of("john.doe@kicherkrabbe.com"))),
                Title.of("New mail"),
                Message.of("You have received a new mail."),
                Instant.parse("2024-03-12T12:30:00.00Z")
        );
        var notification2 = LookupNotification.of(
                NotificationId.create(),
                Version.zero(),
                Origin.of(OriginType.MAIL, OriginId.of("MAIL_ID_2")),
                Target.system(),
                Set.of(Channel.mail(EMail.of("john.doe@kicherkrabbe.com"))),
                Title.of("New mail"),
                Message.of("You have received a new mail."),
                Instant.parse("2024-03-12T12:45:00.00Z")
        );
        update(notification1);
        update(notification2);

        // when: finding old notifications
        var notifications = findOlderThan(Instant.parse("2024-03-12T12:40:00.00Z"));

        // then: only the first notification is found
        assertThat(notifications).containsExactly(notification1);

        // when: finding old notifications
        notifications = findOlderThan(Instant.parse("2024-03-12T12:50:00.00Z"));

        // then: all notifications are found
        assertThat(notifications).containsExactlyInAnyOrder(notification1, notification2);
    }

    private void update(LookupNotification notification) {
        repo.update(notification).block();
    }

    private void remove(NotificationId notificationId) {
        repo.remove(notificationId).block();
    }

    private LookupNotification findById(NotificationId notificationId) {
        return repo.findById(notificationId).block();
    }

    private List<LookupNotification> findOlderThan(Instant instant) {
        return repo.findOlderThan(instant).collectList().block();
    }

    private LookupNotificationPage query(DateRangeFilter dateRangeFilter, long skip, long limit) {
        return repo.query(dateRangeFilter, skip, limit).block();
    }

}
