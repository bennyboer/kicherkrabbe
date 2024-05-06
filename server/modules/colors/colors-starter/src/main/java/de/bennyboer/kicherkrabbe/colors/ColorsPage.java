package de.bennyboer.kicherkrabbe.colors;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class ColorsPage {

    long skip;

    long limit;

    long total;

    List<ColorDetails> results;

    public static ColorsPage of(long skip, long limit, long total, List<ColorDetails> results) {
        notNull(results, "Results must be given");

        return new ColorsPage(skip, limit, total, results);
    }

}
