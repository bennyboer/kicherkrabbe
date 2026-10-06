package de.bennyboer.kicherkrabbe.embroideries;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class EmbroideriesPage {

    long skip;

    long limit;

    long total;

    List<EmbroideryDetails> results;

    public static EmbroideriesPage of(long skip, long limit, long total, List<EmbroideryDetails> results) {
        notNull(results, "Results must be given");

        return new EmbroideriesPage(skip, limit, total, results);
    }

}
