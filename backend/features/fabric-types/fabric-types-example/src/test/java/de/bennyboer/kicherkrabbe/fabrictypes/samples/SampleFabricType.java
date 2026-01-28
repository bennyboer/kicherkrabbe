package de.bennyboer.kicherkrabbe.fabrictypes.samples;

import lombok.Builder;

@Builder
public class SampleFabricType {

    @Builder.Default
    private String name = "Jersey";

    public String getName() {
        return name;
    }

}
