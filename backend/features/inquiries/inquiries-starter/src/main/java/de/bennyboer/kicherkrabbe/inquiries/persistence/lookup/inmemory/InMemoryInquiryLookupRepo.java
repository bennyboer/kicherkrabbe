package de.bennyboer.kicherkrabbe.inquiries.persistence.lookup.inmemory;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.inmemory.InMemoryEventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.inquiries.InquiryId;
import de.bennyboer.kicherkrabbe.inquiries.RequestId;
import de.bennyboer.kicherkrabbe.inquiries.persistence.lookup.InquiryLookupRepo;
import de.bennyboer.kicherkrabbe.inquiries.persistence.lookup.LookupInquiry;
import reactor.core.publisher.Mono;

public class InMemoryInquiryLookupRepo
        extends InMemoryEventSourcingReadModelRepo<InquiryId, LookupInquiry>
        implements InquiryLookupRepo {

    @Override
    public Mono<LookupInquiry> find(InquiryId id) {
        return get(id);
    }

    @Override
    public Mono<LookupInquiry> findByRequestId(RequestId requestId) {
        return getAll()
                .filter(inquiry -> inquiry.getRequestId().equals(requestId))
                .next();
    }

}
