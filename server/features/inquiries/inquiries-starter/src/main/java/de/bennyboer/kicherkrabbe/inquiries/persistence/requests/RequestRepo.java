package de.bennyboer.kicherkrabbe.inquiries.persistence.requests;

import de.bennyboer.kicherkrabbe.inquiries.EMail;
import de.bennyboer.kicherkrabbe.inquiries.RequestId;
import reactor.core.publisher.Mono;

import java.time.Instant;

public interface RequestRepo {

    Mono<Void> insert(Request request);

    Mono<Request> findById(RequestId id);

    Mono<Long> countRecentByMail(EMail mail, Instant since);

    Mono<Long> countRecentByIpAddress(String ipAddress, Instant since);

    Mono<Long> countRecent(Instant since);

}
