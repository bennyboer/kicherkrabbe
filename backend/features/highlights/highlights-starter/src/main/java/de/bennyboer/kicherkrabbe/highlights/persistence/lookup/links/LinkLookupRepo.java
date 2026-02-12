package de.bennyboer.kicherkrabbe.highlights.persistence.lookup.links;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.EventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.highlights.Link;
import de.bennyboer.kicherkrabbe.highlights.LinkId;
import de.bennyboer.kicherkrabbe.highlights.LinkType;
import reactor.core.publisher.Mono;

public interface LinkLookupRepo extends EventSourcingReadModelRepo<String, LookupLink> {

    Mono<LinkPage> find(String searchTerm, long skip, long limit);

    Mono<Link> findOne(LinkType type, LinkId linkId);

    Mono<Void> remove(LinkType type, LinkId id);

}
