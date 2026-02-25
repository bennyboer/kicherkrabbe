package de.bennyboer.kicherkrabbe.offers;

import lombok.Getter;

@Getter
public class AliasAlreadyInUseError extends Exception {

    private final OfferId conflictingOfferId;

    private final OfferAlias alias;

    public AliasAlreadyInUseError(OfferId conflictingOfferId, OfferAlias alias) {
        super("The offer alias '%s' is already in use for offer with ID '%s'".formatted(
                alias.getValue(),
                conflictingOfferId.getValue()
        ));

        this.conflictingOfferId = conflictingOfferId;
        this.alias = alias;
    }

}
