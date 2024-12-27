package de.bennyboer.kicherkrabbe.notifications.notification;

import de.bennyboer.kicherkrabbe.eventsourcing.EventSourcingService;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateIdAndVersion;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateService;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.EventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.notifications.notification.delete.DeleteCmd;
import de.bennyboer.kicherkrabbe.notifications.notification.send.SendCmd;
import de.bennyboer.kicherkrabbe.notifications.channel.Channel;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.util.List;
import java.util.Set;

public class NotificationService extends AggregateService<Notification, NotificationId> {

    public NotificationService(EventSourcingRepo repo, EventPublisher eventPublisher, Clock clock) {
        super(new EventSourcingService<>(
                Notification.TYPE,
                Notification.init(),
                repo,
                eventPublisher,
                List.of(),
                clock
        ));
    }

    public Mono<AggregateIdAndVersion<NotificationId>> send(
            Origin origin,
            Target target,
            Set<Channel> channels,
            Title title,
            Message message,
            Agent agent
    ) {
        var id = NotificationId.create();

        return dispatchCommandToLatest(id, agent, SendCmd.of(origin, target, channels, title, message))
                .map(version -> AggregateIdAndVersion.of(id, version));
    }

    public Mono<Version> delete(NotificationId id, Version version, Agent agent) {
        return dispatchCommand(
                id,
                version,
                agent,
                DeleteCmd.of()
        ).flatMap(v -> collapseEvents(id, v, agent));
    }

    @Override
    protected AggregateType getAggregateType() {
        return Notification.TYPE;
    }

    @Override
    protected AggregateId toAggregateId(NotificationId id) {
        return AggregateId.of(id.getValue());
    }

    @Override
    protected boolean isRemoved(Notification notification) {
        return notification.isDeleted();
    }

}
