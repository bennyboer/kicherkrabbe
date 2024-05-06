package de.bennyboer.kicherkrabbe.colors.persistence.lookup.inmemory;

import de.bennyboer.kicherkrabbe.colors.ColorId;
import de.bennyboer.kicherkrabbe.colors.persistence.lookup.ColorLookupRepo;
import de.bennyboer.kicherkrabbe.colors.persistence.lookup.LookupColor;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.inmemory.InMemoryEventSourcingReadModelRepo;
import reactor.core.publisher.Flux;

import java.util.Comparator;
import java.util.Locale;

public class InMemoryColorLookupRepo extends InMemoryEventSourcingReadModelRepo<ColorId, LookupColor>
        implements ColorLookupRepo {

    @Override
    public Flux<LookupColor> find(String searchTerm, long skip, long limit) {
        return getAll()
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
                .skip(skip)
                .take(limit);
    }

    @Override
    protected ColorId getId(LookupColor readModel) {
        return readModel.getId();
    }

}
