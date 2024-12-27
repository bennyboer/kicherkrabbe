package de.bennyboer.kicherkrabbe.notifications.notification;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.LoggingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.inmemory.InMemoryEventSourcingRepo;
import de.bennyboer.kicherkrabbe.notifications.notification.snapshot.SnapshottedEvent;
import de.bennyboer.kicherkrabbe.notifications.channel.Channel;
import de.bennyboer.kicherkrabbe.notifications.channel.mail.EMail;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.util.Set;

import static de.bennyboer.kicherkrabbe.notifications.notification.OriginType.MAIL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SettingsServiceTest {

    private final EventSourcingRepo repo = new InMemoryEventSourcingRepo();

    private final LoggingEventPublisher eventPublisher = new LoggingEventPublisher();

    private final NotificationService notificationService = new NotificationService(
            repo,
            eventPublisher,
            Clock.systemUTC()
    );

    @Test
    void shouldSendNotification() {
        // when: sending a notification
        var origin = Origin.of(MAIL, OriginId.of("MAIL_ID"));
        var target = Target.system();
        var channels = Set.of(Channel.mail(EMail.of("notify@kicherkrabbe.com")));
        var id = send(
                origin,
                target,
                channels,
                Title.of("Mail received"),
                Message.of("You have received a new mail.")
        );

        // then: the notification is sent
        var notification = get(id);
        assertThat(notification.getId()).isEqualTo(id);
        assertThat(notification.getVersion()).isEqualTo(Version.zero());
        assertThat(notification.getOrigin()).isEqualTo(origin);
        assertThat(notification.getTarget()).isEqualTo(target);
        assertThat(notification.getTitle()).isEqualTo(Title.of("Mail received"));
        assertThat(notification.getMessage()).isEqualTo(Message.of("You have received a new mail."));
        assertThat(notification.isDeleted()).isFalse();
    }

    @Test
    void shouldDeleteNotification() {
        // given: a sent notification
        var origin = Origin.of(MAIL, OriginId.of("MAIL_ID"));
        var target = Target.system();
        var channels = Set.of(Channel.mail(EMail.of("notify@kicherkrabbe.com")));
        var id = send(
                origin,
                target,
                channels,
                Title.of("Mail received"),
                Message.of("You have received a new mail.")
        );

        // when: deleting the notification
        delete(id, Version.zero());

        // then: the notification is deleted
        var notification = get(id);
        assertThat(notification).isNull();

        // and: there is only a single snapshot event in the repository since it is collapsed
        var events = repo.findEventsByAggregateIdAndType(
                AggregateId.of(id.getValue()),
                Notification.TYPE,
                Version.zero()
        ).collectList().block();
        assertThat(events).hasSize(1);
        var event = events.get(0);
        assertThat(event.getMetadata().isSnapshot()).isTrue();

        // and: the snapshot event is anonymized
        var snapshot = (SnapshottedEvent) event.getEvent();
        assertThat(snapshot.getTitle().getValue()).isEqualTo("ANONYMIZED");
        assertThat(snapshot.getMessage().getValue()).isEqualTo("ANONYMIZED");

        // and: there can be no more events for the notification
        assertThatThrownBy(() -> delete(id, event.getMetadata().getAggregateVersion()))
                .matches(e -> e instanceof IllegalArgumentException && e.getMessage()
                        .equals("Cannot apply command to deleted aggregate"));
    }

    private NotificationId send(Origin origin, Target target, Set<Channel> channels, Title title, Message message) {
        return notificationService.send(origin, target, channels, title, message, Agent.system()).block().getId();
    }

    private Notification get(NotificationId id) {
        return notificationService.get(id).block();
    }

    private Version delete(NotificationId id, Version version) {
        return notificationService.delete(id, version, Agent.system()).block();
    }

}
