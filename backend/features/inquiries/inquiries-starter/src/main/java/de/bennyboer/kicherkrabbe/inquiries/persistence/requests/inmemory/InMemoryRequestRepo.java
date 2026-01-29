package de.bennyboer.kicherkrabbe.inquiries.persistence.requests.inmemory;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.inmemory.InMemoryEventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.inquiries.EMail;
import de.bennyboer.kicherkrabbe.inquiries.RequestId;
import de.bennyboer.kicherkrabbe.inquiries.persistence.requests.Request;
import de.bennyboer.kicherkrabbe.inquiries.persistence.requests.RequestRepo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

public class InMemoryRequestRepo
        extends InMemoryEventSourcingReadModelRepo<RequestId, Request>
        implements RequestRepo {

    @Override
    public Mono<Void> insert(Request request) {
        return update(request);
    }

    @Override
    public Mono<Request> findById(RequestId id) {
        return get(id);
    }

    @Override
    public Mono<Long> countRecentByMail(EMail mail, Instant since) {
        return getAll()
                .filter(request -> request.getMail().equals(mail))
                .filter(request -> request.getCreatedAt().isAfter(since))
                .count();
    }

    @Override
    public Mono<Long> countRecentByIpAddress(String ipAddress, Instant since) {
        return getAll()
                .filter(request -> request.getIpAddress()
                        .map(ip -> ip.equals(ipAddress))
                        .orElse(false))
                .filter(request -> request.getCreatedAt().isAfter(since))
                .count();
    }

    @Override
    public Mono<Long> countRecent(Instant since) {
        return getAll()
                .filter(request -> request.getCreatedAt().isAfter(since))
                .count();
    }

    @Override
    public Flux<Request> findInTimeFrame(Instant from, Instant to) {
        return getAll()
                .filter(request -> !request.getCreatedAt().isBefore(from))
                .filter(request -> request.getCreatedAt().isBefore(to));
    }

    @Override
    protected boolean allowSameVersionUpdate() {
        return true;
    }

}
