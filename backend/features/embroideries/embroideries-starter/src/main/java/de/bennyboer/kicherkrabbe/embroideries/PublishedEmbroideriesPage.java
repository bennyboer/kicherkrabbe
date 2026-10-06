package de.bennyboer.kicherkrabbe.embroideries;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class PublishedEmbroideriesPage {

    long skip;

    long limit;

    long total;

    List<PublishedEmbroidery> results;

    public static PublishedEmbroideriesPage of(long skip, long limit, long total, List<PublishedEmbroidery> results) {
        notNull(results, "Results must be given");

        return new PublishedEmbroideriesPage(skip, limit, total, results);
    }

}
