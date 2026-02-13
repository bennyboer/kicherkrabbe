package de.bennyboer.kicherkrabbe.highlights.persistence.lookup;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class LookupHighlightPage {

    long skip;

    long limit;

    long total;

    List<LookupHighlight> results;

    public static LookupHighlightPage of(long skip, long limit, long total, List<LookupHighlight> results) {
        notNull(results, "Results must be given");

        return new LookupHighlightPage(skip, limit, total, results);
    }

}
