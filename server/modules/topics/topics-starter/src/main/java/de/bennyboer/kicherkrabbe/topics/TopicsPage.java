package de.bennyboer.kicherkrabbe.topics;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class TopicsPage {

    long skip;

    long limit;

    long total;

    List<TopicDetails> results;

    public static TopicsPage of(long skip, long limit, long total, List<TopicDetails> results) {
        notNull(results, "Results must be given");

        return new TopicsPage(skip, limit, total, results);
    }

}
