package de.bennyboer.kicherkrabbe.fabrictypes.samples;

import de.bennyboer.kicherkrabbe.fabrictypes.FabricTypeName;
import lombok.Builder;

@Builder
public class SampleFabricType {

    @Builder.Default
    private String name = "Jersey";

    public FabricTypeName getName() {
        return FabricTypeName.of(name);
    }

}
