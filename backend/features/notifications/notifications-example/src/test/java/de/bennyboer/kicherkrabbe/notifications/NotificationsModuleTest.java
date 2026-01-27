package de.bennyboer.kicherkrabbe.notifications;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.LoggingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.inmemory.InMemoryEventSourcingRepo;
import de.bennyboer.kicherkrabbe.notifications.api.requests.ActivateSystemChannelRequest;
import de.bennyboer.kicherkrabbe.notifications.samples.SampleNotification;
import de.bennyboer.kicherkrabbe.notifications.api.requests.DeactivateSystemChannelRequest;
import de.bennyboer.kicherkrabbe.notifications.api.requests.SendNotificationRequest;
import de.bennyboer.kicherkrabbe.notifications.api.requests.UpdateSystemChannelRequest;
import de.bennyboer.kicherkrabbe.notifications.api.responses.*;
import de.bennyboer.kicherkrabbe.notifications.notification.NotificationService;
import de.bennyboer.kicherkrabbe.notifications.persistence.lookup.notification.NotificationLookupRepo;
import de.bennyboer.kicherkrabbe.notifications.persistence.lookup.notification.inmemory.InMemoryNotificationLookupRepo;
import de.bennyboer.kicherkrabbe.notifications.settings.SettingsService;
import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import de.bennyboer.kicherkrabbe.permissions.persistence.inmemory.InMemoryPermissionsRepo;
import de.bennyboer.kicherkrabbe.persistence.MockReactiveTransactionManager;
import de.bennyboer.kicherkrabbe.testing.time.TestClock;
import org.springframework.transaction.ReactiveTransactionManager;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class NotificationsModuleTest {

    private final TestClock clock = new TestClock();

    private final NotificationsModuleConfig config = new NotificationsModuleConfig();

    private final NotificationService notificationService = new NotificationService(
            new InMemoryEventSourcingRepo(),
            new LoggingEventPublisher(),
            clock
    );

    private final SettingsService settingsService = new SettingsService(
            new InMemoryEventSourcingRepo(),
            new LoggingEventPublisher(),
            clock
    );

    private final NotificationLookupRepo notificationLookupRepo = new InMemoryNotificationLookupRepo();

    private final PermissionsService permissionsService = new PermissionsService(
            new InMemoryPermissionsRepo(),
            event -> Mono.empty()
    );

    private final ReactiveTransactionManager transactionManager = new MockReactiveTransactionManager();

    private final NotificationsModule module = config.notificationsModule(
            notificationService,
            settingsService,
            notificationLookupRepo,
            permissionsService,
            transactionManager,
            Optional.of(clock)
    );

    public void setTime(Instant instant) {
        clock.setNow(instant);
    }

    public SendNotificationResponse sendNotification(SendNotificationRequest request, Agent agent) {
        var result = module.sendNotification(request, agent).block();

        updateNotificationInLookup(result.id);
        allowSystemUserToDeleteNotification(result.id);

        return result;
    }

    public SendNotificationResponse sendNotification(SampleNotification sample, Agent agent) {
        return sendNotification(sample.toRequest(), agent);
    }

    public SendNotificationResponse sendSampleNotification(Agent agent) {
        return sendNotification(SampleNotification.builder().build(), agent);
    }

    public QueryNotificationsResponse getNotifications(
            DateRangeFilter dateRangeFilter,
            long skip,
            long limit,
            Agent agent
    ) {
        return module.getNotifications(dateRangeFilter, skip, limit, agent).block();
    }

    public QuerySettingsResponse getSettings(Agent agent) {
        return module.getSettings(agent).block();
    }

    public EnableSystemNotificationsResponse enableSystemNotifications(long version, Agent agent) {
        return module.enableSystemNotifications(version, agent).block();
    }

    public DisableSystemNotificationsResponse disableSystemNotifications(long version, Agent agent) {
        return module.disableSystemNotifications(version, agent).block();
    }

    public UpdateSystemChannelResponse updateSystemChannel(UpdateSystemChannelRequest request, Agent agent) {
        return module.updateSystemChannel(request, agent).block();
    }

    public ActivateSystemChannelResponse activateSystemChannel(
            ActivateSystemChannelRequest request,
            Agent agent
    ) {
        return module.activateSystemChannel(request, agent).block();
    }

    public DeactivateSystemChannelResponse deactivateSystemChannel(
            DeactivateSystemChannelRequest request,
            Agent agent
    ) {
        return module.deactivateSystemChannel(request, agent).block();
    }

    public List<String> cleanupOldNotifications(Agent agent) {
        var result = module.cleanupOldNotifications(agent).collectList().block();

        result.forEach(notificationId -> {
            removeNotificationFromLookup(notificationId);
            removePermissionsForNotification(notificationId);
        });

        return result;
    }

    public void allowSystemUserToSendNotifications() {
        module.allowSystemUserToSendNotifications().block();
    }

    public void allowSystemUserToDeleteNotification(String notificationId) {
        module.allowSystemUserToDeleteNotification(notificationId).block();
    }

    public void allowUserToReadNotificationsAndManageSettings(String userId) {
        module.allowUserToReadNotificationsAndManageSettings(userId).block();
    }

    public void updateNotificationInLookup(String notificationId) {
        module.updateNotificationInLookup(notificationId).block();
    }

    public void removeNotificationFromLookup(String notificationId) {
        module.removeNotificationFromLookup(notificationId).block();
    }

    public void removePermissionsForNotification(String notificationId) {
        module.removePermissionsForNotification(notificationId).block();
    }

}
