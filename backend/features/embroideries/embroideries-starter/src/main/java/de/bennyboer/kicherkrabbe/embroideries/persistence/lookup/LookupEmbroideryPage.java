package de.bennyboer.kicherkrabbe.embroideries.persistence.lookup;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class LookupEmbroideryPage {

    long skip;

    long limit;

    long total;

    List<LookupEmbroidery> results;

    public static LookupEmbroideryPage of(long skip, long limit, long total, List<LookupEmbroidery> results) {
        notNull(results, "Results must be given");

        return new LookupEmbroideryPage(skip, limit, total, results);
    }

}
