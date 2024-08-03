package de.bennyboer.kicherkrabbe.patterns.persistence.lookup;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class LookupPatternPage {

    long skip;

    long limit;

    long total;

    List<LookupPattern> results;

    public static LookupPatternPage of(long skip, long limit, long total, List<LookupPattern> results) {
        notNull(results, "Results must be given");

        return new LookupPatternPage(skip, limit, total, results);
    }

}
