package de.bennyboer.kicherkrabbe.fabrictypes;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class FabricTypesPage {

    long skip;

    long limit;

    long total;

    List<FabricTypeDetails> results;

    public static FabricTypesPage of(long skip, long limit, long total, List<FabricTypeDetails> results) {
        notNull(results, "Results must be given");

        return new FabricTypesPage(skip, limit, total, results);
    }

}
