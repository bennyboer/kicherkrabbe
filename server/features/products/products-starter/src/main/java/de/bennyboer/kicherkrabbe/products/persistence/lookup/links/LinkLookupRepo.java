package de.bennyboer.kicherkrabbe.products.persistence.lookup.links;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.EventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.products.product.Link;
import de.bennyboer.kicherkrabbe.products.product.LinkId;
import de.bennyboer.kicherkrabbe.products.product.LinkType;
import reactor.core.publisher.Mono;

public interface LinkLookupRepo extends EventSourcingReadModelRepo<String, LookupLink> {

    Mono<LinkPage> find(String searchTerm, long skip, long limit);

    Mono<Link> findOne(LinkType type, LinkId linkId);

    Mono<Void> remove(LinkType type, LinkId id);

}
