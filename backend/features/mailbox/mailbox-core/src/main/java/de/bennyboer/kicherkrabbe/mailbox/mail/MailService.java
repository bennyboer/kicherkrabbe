package de.bennyboer.kicherkrabbe.mailbox.mail;

import de.bennyboer.kicherkrabbe.eventsourcing.EventSourcingService;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateIdAndVersion;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateService;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.EventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.mailbox.mail.delete.DeleteCmd;
import de.bennyboer.kicherkrabbe.mailbox.mail.read.MarkAsReadCmd;
import de.bennyboer.kicherkrabbe.mailbox.mail.receive.ReceiveCmd;
import de.bennyboer.kicherkrabbe.mailbox.mail.unread.MarkAsUnreadCmd;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.util.List;

public class MailService extends AggregateService<Mail, MailId> {

    public MailService(EventSourcingRepo repo, EventPublisher eventPublisher, Clock clock) {
        super(new EventSourcingService<>(
                Mail.TYPE,
                Mail.init(),
                repo,
                eventPublisher,
                List.of(),
                clock
        ));
    }

    public Mono<AggregateIdAndVersion<MailId>> receive(
            Origin origin,
            Sender sender,
            Subject subject,
            Content content,
            Agent agent
    ) {
        var id = MailId.create();

        return dispatchCommandToLatest(id, agent, ReceiveCmd.of(origin, sender, subject, content))
                .map(version -> AggregateIdAndVersion.of(id, version));
    }

    public Mono<Version> markAsRead(MailId id, Version version, Agent agent) {
        return dispatchCommand(id, version, agent, MarkAsReadCmd.of());
    }

    public Mono<Version> markAsUnread(MailId id, Version version, Agent agent) {
        return dispatchCommand(id, version, agent, MarkAsUnreadCmd.of());
    }

    public Mono<Version> delete(MailId id, Version version, Agent agent) {
        return dispatchCommand(id, version, agent, DeleteCmd.of())
                .flatMap(v -> collapseEvents(id, v, agent));
    }

    @Override
    protected AggregateType getAggregateType() {
        return Mail.TYPE;
    }

    @Override
    protected AggregateId toAggregateId(MailId id) {
        return AggregateId.of(id.getValue());
    }

    @Override
    protected boolean isRemoved(Mail mail) {
        return mail.isDeleted();
    }

}
