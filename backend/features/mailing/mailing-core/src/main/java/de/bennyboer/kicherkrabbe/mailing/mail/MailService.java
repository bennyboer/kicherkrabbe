package de.bennyboer.kicherkrabbe.mailing.mail;

import de.bennyboer.kicherkrabbe.eventsourcing.EventSourcingService;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateIdAndVersion;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateService;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.EventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.mailing.MailingService;
import de.bennyboer.kicherkrabbe.mailing.mail.delete.DeleteCmd;
import de.bennyboer.kicherkrabbe.mailing.mail.send.SendCmd;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.util.List;
import java.util.Set;

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

    public Mono<AggregateIdAndVersion<MailId>> send(
            Sender sender,
            Set<Receiver> receivers,
            Subject subject,
            Text text,
            MailingService mailingService,
            Agent agent
    ) {
        var id = MailId.create();
        var cmd = SendCmd.of(
                sender,
                receivers,
                subject,
                text,
                mailingService
        );

        return dispatchCommandToLatest(id, agent, cmd)
                .map(version -> AggregateIdAndVersion.of(id, version));
    }

    public Mono<Version> delete(MailId id, Version version, Agent agent) {
        return dispatchCommand(
                id,
                version,
                agent,
                DeleteCmd.of()
        ).flatMap(v -> collapseEvents(id, v, agent));
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
