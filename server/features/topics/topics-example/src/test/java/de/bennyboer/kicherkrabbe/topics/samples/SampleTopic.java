package de.bennyboer.kicherkrabbe.topics.samples;

import lombok.Builder;

@Builder
public class SampleTopic {

    @Builder.Default
    private String name = "Sample Topic";

    public String getName() {
        return name;
    }

}
