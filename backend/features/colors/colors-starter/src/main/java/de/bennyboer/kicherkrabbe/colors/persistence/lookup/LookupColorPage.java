package de.bennyboer.kicherkrabbe.colors.persistence.lookup;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class LookupColorPage {

    long skip;

    long limit;

    long total;

    List<LookupColor> results;

    public static LookupColorPage of(long skip, long limit, long total, List<LookupColor> results) {
        notNull(results, "Results must be given");

        return new LookupColorPage(skip, limit, total, results);
    }

}
