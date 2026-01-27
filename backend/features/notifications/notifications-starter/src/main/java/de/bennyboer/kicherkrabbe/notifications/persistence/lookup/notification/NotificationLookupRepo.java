package de.bennyboer.kicherkrabbe.notifications.persistence.lookup.notification;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.EventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.notifications.DateRangeFilter;
import de.bennyboer.kicherkrabbe.notifications.notification.NotificationId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

public interface NotificationLookupRepo extends EventSourcingReadModelRepo<NotificationId, LookupNotification> {

    Mono<LookupNotification> findById(NotificationId id);

    Mono<LookupNotificationPage> query(DateRangeFilter dateRangeFilter, long skip, long limit);

    Flux<LookupNotification> findOlderThan(Instant instant);
    
}
