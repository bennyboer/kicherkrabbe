package de.bennyboer.kicherkrabbe.embroideries;

import lombok.Getter;

@Getter
public class AliasAlreadyInUseError extends Exception {

    private final EmbroideryId conflictingEmbroideryId;

    private final EmbroideryAlias alias;

    public AliasAlreadyInUseError(EmbroideryId conflictingEmbroideryId, EmbroideryAlias alias) {
        super("The embroidery alias '%s' is already in use for embroidery with ID '%s'".formatted(
                alias.getValue(),
                conflictingEmbroideryId.getValue()
        ));

        this.conflictingEmbroideryId = conflictingEmbroideryId;
        this.alias = alias;
    }

}
