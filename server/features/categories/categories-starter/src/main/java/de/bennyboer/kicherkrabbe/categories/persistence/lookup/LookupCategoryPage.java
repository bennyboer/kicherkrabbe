package de.bennyboer.kicherkrabbe.categories.persistence.lookup;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class LookupCategoryPage {

    long skip;

    long limit;

    long total;

    List<LookupCategory> results;

    public static LookupCategoryPage of(long skip, long limit, long total, List<LookupCategory> results) {
        notNull(results, "Results must be given");

        return new LookupCategoryPage(skip, limit, total, results);
    }

}
