package de.bennyboer.kicherkrabbe.topics.persistence.lookup;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class LookupTopicPage {

    long skip;

    long limit;

    long total;

    List<LookupTopic> results;

    public static LookupTopicPage of(long skip, long limit, long total, List<LookupTopic> results) {
        notNull(results, "Results must be given");

        return new LookupTopicPage(skip, limit, total, results);
    }

}
