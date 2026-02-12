package de.bennyboer.kicherkrabbe.highlights;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class HighlightsPage {

    long skip;

    long limit;

    long total;

    List<HighlightDetails> results;

    public static HighlightsPage of(long skip, long limit, long total, List<HighlightDetails> results) {
        notNull(results, "Results must be given");

        return new HighlightsPage(skip, limit, total, results);
    }

}
