package de.bennyboer.kicherkrabbe.products.persistence.lookup.links.inmemory;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.inmemory.InMemoryEventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.products.persistence.lookup.links.LinkLookupRepo;
import de.bennyboer.kicherkrabbe.products.persistence.lookup.links.LinkPage;
import de.bennyboer.kicherkrabbe.products.persistence.lookup.links.LookupLink;
import de.bennyboer.kicherkrabbe.products.product.Link;
import de.bennyboer.kicherkrabbe.products.product.LinkId;
import de.bennyboer.kicherkrabbe.products.product.LinkType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;

public class InMemoryLinkLookupRepo
        extends InMemoryEventSourcingReadModelRepo<String, LookupLink>
        implements LinkLookupRepo {

    @Override
    public Mono<LinkPage> find(String searchTerm, long skip, long limit) {
        return getAll()
                .filter(link -> searchTerm.isBlank() || link.getName().getValue().toLowerCase().contains(searchTerm.toLowerCase()))
                .sort(Comparator.comparing((LookupLink link) -> link.getName().getValue()))
                .map(LookupLink::toLink)
                .collectList()
                .map(links -> {
                    long total = links.size();
                    long fromIdx = Math.min(skip, total);
                    long toIdx = Math.min(skip + limit, total);

                    return LinkPage.of(total, links.subList((int) fromIdx, (int) toIdx));
                });
    }

    @Override
    public Mono<Link> findOne(LinkType type, LinkId linkId) {
        return getAll()
                .filter(link -> link.getType() == type && link.getLinkId().equals(linkId))
                .map(LookupLink::toLink)
                .next();
    }

    @Override
    public Mono<Void> remove(LinkType type, LinkId id) {
        return getAll()
                .filter(link -> link.getType() == type && link.getLinkId().equals(id))
                .collectList()
                .flatMapMany(Flux::fromIterable)
                .flatMap(link -> remove(link.getId()))
                .then();
    }

}
