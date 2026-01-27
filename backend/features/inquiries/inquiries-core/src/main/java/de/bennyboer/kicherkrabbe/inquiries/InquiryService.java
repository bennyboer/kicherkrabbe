package de.bennyboer.kicherkrabbe.inquiries;

import de.bennyboer.kicherkrabbe.eventsourcing.EventSourcingService;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateIdAndVersion;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateService;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.EventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.inquiries.delete.DeleteCmd;
import de.bennyboer.kicherkrabbe.inquiries.send.SendCmd;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.util.List;

public class InquiryService extends AggregateService<Inquiry, InquiryId> {

    public InquiryService(EventSourcingRepo repo, EventPublisher eventPublisher, Clock clock) {
        super(new EventSourcingService<>(
                Inquiry.TYPE,
                Inquiry.init(),
                repo,
                eventPublisher,
                List.of(),
                clock
        ));
    }

    public Mono<AggregateIdAndVersion<InquiryId>> send(
            RequestId requestId,
            Sender sender,
            Subject subject,
            Message message,
            Fingerprint fingerprint,
            Agent agent
    ) {
        var id = InquiryId.create();

        return dispatchCommandToLatest(id, agent, SendCmd.of(requestId, sender, subject, message, fingerprint))
                .map(version -> AggregateIdAndVersion.of(id, version));
    }

    public Mono<Version> delete(InquiryId id, Agent agent) {
        return dispatchCommandToLatest(id, agent, DeleteCmd.of())
                .flatMap(v -> collapseEvents(id, v, agent));
    }

    @Override
    protected AggregateType getAggregateType() {
        return Inquiry.TYPE;
    }

    @Override
    protected AggregateId toAggregateId(InquiryId id) {
        return AggregateId.of(id.getValue());
    }

    @Override
    protected boolean isRemoved(Inquiry inquiry) {
        return inquiry.isDeleted();
    }

}
