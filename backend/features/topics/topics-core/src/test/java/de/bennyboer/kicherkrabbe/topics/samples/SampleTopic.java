package de.bennyboer.kicherkrabbe.topics.samples;

import de.bennyboer.kicherkrabbe.topics.TopicName;
import lombok.Builder;

@Builder
public class SampleTopic {

    @Builder.Default
    private String name = "Sample Topic";

    public TopicName getName() {
        return TopicName.of(name);
    }

}
