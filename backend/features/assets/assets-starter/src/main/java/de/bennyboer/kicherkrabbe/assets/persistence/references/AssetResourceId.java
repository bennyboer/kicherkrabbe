package de.bennyboer.kicherkrabbe.assets.persistence.references;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class AssetResourceId {

    String value;

    public static AssetResourceId of(String value) {
        notNull(value, "Asset resource ID must be given");
        check(!value.isBlank(), "Asset resource ID must not be blank");

        return new AssetResourceId(value);
    }

    @Override
    public String toString() {
        return "AssetResourceId(%s)".formatted(value);
    }

}
