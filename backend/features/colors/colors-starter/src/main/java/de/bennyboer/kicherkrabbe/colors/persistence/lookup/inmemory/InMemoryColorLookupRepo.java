package de.bennyboer.kicherkrabbe.colors.persistence.lookup.inmemory;

import de.bennyboer.kicherkrabbe.colors.ColorId;
import de.bennyboer.kicherkrabbe.colors.persistence.lookup.ColorLookupRepo;
import de.bennyboer.kicherkrabbe.colors.persistence.lookup.LookupColor;
import de.bennyboer.kicherkrabbe.colors.persistence.lookup.LookupColorPage;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.inmemory.InMemoryEventSourcingReadModelRepo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Comparator;
import java.util.Locale;

public class InMemoryColorLookupRepo extends InMemoryEventSourcingReadModelRepo<ColorId, LookupColor>
        implements ColorLookupRepo {

    @Override
    public Mono<LookupColorPage> find(Collection<ColorId> colorIds, String searchTerm, long skip, long limit) {
        return getAll()
                .filter(color -> colorIds.contains(color.getId()))
                .filter(color -> {
                    if (searchTerm.isBlank()) {
                        return true;
                    }

                    return color.getName()
                            .getValue()
                            .toLowerCase(Locale.ROOT)
                            .contains(searchTerm.toLowerCase(Locale.ROOT));
                })
                .sort(Comparator.comparing(LookupColor::getCreatedAt))
                .collectList()
                .flatMap(colors -> {
                    long total = colors.size();

                    return Flux.fromIterable(colors)
                            .skip(skip)
                            .take(limit)
                            .collectList()
                            .map(results -> LookupColorPage.of(skip, limit, total, results));
                });
    }

    @Override
    protected ColorId getId(LookupColor readModel) {
        return readModel.getId();
    }

}
