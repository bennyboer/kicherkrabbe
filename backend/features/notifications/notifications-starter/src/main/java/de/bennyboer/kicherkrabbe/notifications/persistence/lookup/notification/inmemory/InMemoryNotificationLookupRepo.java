package de.bennyboer.kicherkrabbe.notifications.persistence.lookup.notification.inmemory;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.inmemory.InMemoryEventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.notifications.DateRangeFilter;
import de.bennyboer.kicherkrabbe.notifications.notification.NotificationId;
import de.bennyboer.kicherkrabbe.notifications.persistence.lookup.notification.LookupNotification;
import de.bennyboer.kicherkrabbe.notifications.persistence.lookup.notification.LookupNotificationPage;
import de.bennyboer.kicherkrabbe.notifications.persistence.lookup.notification.NotificationLookupRepo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Comparator;

public class InMemoryNotificationLookupRepo
        extends InMemoryEventSourcingReadModelRepo<NotificationId, LookupNotification>
        implements NotificationLookupRepo {

    @Override
    public Mono<LookupNotification> findById(NotificationId id) {
        return get(id);
    }

    @Override
    public Mono<LookupNotificationPage> query(DateRangeFilter dateRangeFilter, long skip, long limit) {
        return getAll()
                .filter(notification -> dateRangeFilter.contains(notification.getSentAt()))
                .sort(Comparator.comparing(LookupNotification::getSentAt).reversed())
                .collectList()
                .map(notifications -> {
                    long total = notifications.size();
                    long from = Math.min(skip, total);
                    long to = Math.min(skip + limit, total);

                    return LookupNotificationPage.of(total, notifications.subList((int) from, (int) to));
                });
    }

    @Override
    public Flux<LookupNotification> findOlderThan(Instant instant) {
        return getAll()
                .filter(notification -> notification.getSentAt().isBefore(instant));
    }

    @Override
    protected NotificationId getId(LookupNotification notification) {
        return notification.getId();
    }

}
