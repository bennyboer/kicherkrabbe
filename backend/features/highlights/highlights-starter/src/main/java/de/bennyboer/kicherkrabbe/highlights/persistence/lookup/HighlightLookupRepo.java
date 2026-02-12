package de.bennyboer.kicherkrabbe.highlights.persistence.lookup;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.EventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.highlights.HighlightId;
import de.bennyboer.kicherkrabbe.highlights.LinkId;
import de.bennyboer.kicherkrabbe.highlights.LinkType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface HighlightLookupRepo extends EventSourcingReadModelRepo<HighlightId, LookupHighlight> {

    Flux<LookupHighlight> findPublished();

    Mono<LookupHighlightPage> findAll(Collection<HighlightId> highlightIds, long skip, long limit);

    Mono<LookupHighlight> findById(HighlightId highlightId);

    Flux<LookupHighlight> findByLink(LinkType linkType, LinkId linkId);

}
