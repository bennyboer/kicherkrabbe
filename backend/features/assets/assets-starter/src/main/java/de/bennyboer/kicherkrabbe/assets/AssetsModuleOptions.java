package de.bennyboer.kicherkrabbe.assets;

import lombok.AllArgsConstructor;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class AssetsModuleOptions {

    long storageLimitBytes;

    public static AssetsModuleOptions of(long storageLimitBytes) {
        return new AssetsModuleOptions(storageLimitBytes);
    }

}
