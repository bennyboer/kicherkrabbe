package de.bennyboer.kicherkrabbe.assets;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static java.util.UUID.randomUUID;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class AssetId {

    String value;

    public static AssetId of(String value) {
        notNull(value, "Asset ID must be given");
        check(!value.isBlank(), "Asset ID must not be blank");

        return new AssetId(value);
    }

    public static AssetId create() {
        return new AssetId(randomUUID().toString());
    }

    @Override
    public String toString() {
        return "AssetId(%s)".formatted(value);
    }

}
