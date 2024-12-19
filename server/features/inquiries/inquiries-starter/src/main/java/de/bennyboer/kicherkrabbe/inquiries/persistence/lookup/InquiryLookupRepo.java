package de.bennyboer.kicherkrabbe.inquiries.persistence.lookup;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.EventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.inquiries.InquiryId;
import de.bennyboer.kicherkrabbe.inquiries.RequestId;
import reactor.core.publisher.Mono;

public interface InquiryLookupRepo extends EventSourcingReadModelRepo<InquiryId, LookupInquiry> {

    Mono<LookupInquiry> find(InquiryId id);

    Mono<LookupInquiry> findByRequestId(RequestId requestId);

}
