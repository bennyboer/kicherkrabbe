package de.bennyboer.kicherkrabbe.patterns;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class PatternsPage {

    long skip;

    long limit;

    long total;

    List<PatternDetails> results;

    public static PatternsPage of(long skip, long limit, long total, List<PatternDetails> results) {
        notNull(results, "Results must be given");

        return new PatternsPage(skip, limit, total, results);
    }

}
