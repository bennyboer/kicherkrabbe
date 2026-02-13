package de.bennyboer.kicherkrabbe.highlights.persistence.lookup.inmemory;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.inmemory.InMemoryEventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.highlights.HighlightId;
import de.bennyboer.kicherkrabbe.highlights.LinkId;
import de.bennyboer.kicherkrabbe.highlights.LinkType;
import de.bennyboer.kicherkrabbe.highlights.persistence.lookup.HighlightLookupRepo;
import de.bennyboer.kicherkrabbe.highlights.persistence.lookup.LookupHighlight;
import de.bennyboer.kicherkrabbe.highlights.persistence.lookup.LookupHighlightPage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Comparator;

public class InMemoryHighlightLookupRepo extends InMemoryEventSourcingReadModelRepo<HighlightId, LookupHighlight>
        implements HighlightLookupRepo {

    @Override
    public Flux<LookupHighlight> findPublished() {
        return getAll()
                .filter(LookupHighlight::isPublished)
                .sort(Comparator.comparingLong(LookupHighlight::getSortOrder));
    }

    @Override
    public Mono<LookupHighlightPage> findAll(Collection<HighlightId> highlightIds, long skip, long limit) {
        return getAll()
                .filter(highlight -> highlightIds.contains(highlight.getId()))
                .sort(Comparator.comparingLong(LookupHighlight::getSortOrder))
                .collectList()
                .flatMap(highlights -> {
                    long total = highlights.size();

                    return Flux.fromIterable(highlights)
                            .skip(skip)
                            .take(limit)
                            .collectList()
                            .map(results -> LookupHighlightPage.of(skip, limit, total, results));
                });
    }

    @Override
    public Mono<LookupHighlight> findById(HighlightId highlightId) {
        return get(highlightId);
    }

    @Override
    public Flux<LookupHighlight> findByLink(LinkType linkType, LinkId linkId) {
        return getAll()
                .filter(highlight -> highlight.getLinks().contains(linkType, linkId));
    }

}
