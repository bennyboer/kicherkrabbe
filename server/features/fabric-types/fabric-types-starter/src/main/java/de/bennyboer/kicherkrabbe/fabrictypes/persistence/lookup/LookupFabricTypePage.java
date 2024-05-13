package de.bennyboer.kicherkrabbe.fabrictypes.persistence.lookup;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class LookupFabricTypePage {

    long skip;

    long limit;

    long total;

    List<LookupFabricType> results;

    public static LookupFabricTypePage of(long skip, long limit, long total, List<LookupFabricType> results) {
        notNull(results, "Results must be given");

        return new LookupFabricTypePage(skip, limit, total, results);
    }

}
