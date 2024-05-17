package de.bennyboer.kicherkrabbe.fabrics;

import lombok.Getter;

import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class FabricTypesMissingError extends Exception {

    private final Set<FabricTypeId> missingFabricTypes;

    public FabricTypesMissingError(Set<FabricTypeId> missingFabricTypes) {
        super("Fabric types are missing: " + missingFabricTypes.stream()
                .map(FabricTypeId::getValue)
                .collect(Collectors.joining(", ")));

        this.missingFabricTypes = missingFabricTypes;
    }

}
