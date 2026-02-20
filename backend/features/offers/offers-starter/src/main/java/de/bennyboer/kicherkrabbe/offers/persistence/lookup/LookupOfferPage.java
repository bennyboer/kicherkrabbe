package de.bennyboer.kicherkrabbe.offers.persistence.lookup;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class LookupOfferPage {

    long skip;

    long limit;

    long total;

    List<LookupOffer> results;

    public static LookupOfferPage of(long skip, long limit, long total, List<LookupOffer> results) {
        notNull(results, "Results must be given");
        check(skip >= 0, "Skip must be greater or equal to 0");
        check(limit > 0, "Limit must be greater than 0");
        check(total >= 0, "Total must be greater or equal to 0");

        return new LookupOfferPage(skip, limit, total, results);
    }

}
