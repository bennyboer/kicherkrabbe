package de.bennyboer.kicherkrabbe.fabrics;

import lombok.Getter;

import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class TopicsMissingError extends Exception {

    private final Set<TopicId> missingTopics;

    public TopicsMissingError(Set<TopicId> missingTopics) {
        super("Topics are missing: " + missingTopics.stream()
                .map(TopicId::getValue)
                .collect(Collectors.joining(", ")));

        this.missingTopics = missingTopics;
    }

}
