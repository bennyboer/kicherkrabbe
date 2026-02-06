package de.bennyboer.kicherkrabbe.fabrics;

import lombok.Getter;

@Getter
public class AliasAlreadyInUseError extends Exception {

    private final FabricId conflictingFabricId;

    private final FabricAlias alias;

    public AliasAlreadyInUseError(FabricId conflictingFabricId, FabricAlias alias) {
        super("The fabric alias '%s' is already in use for fabric with ID '%s'".formatted(
                alias.getValue(),
                conflictingFabricId.getValue()
        ));

        this.conflictingFabricId = conflictingFabricId;
        this.alias = alias;
    }

}
