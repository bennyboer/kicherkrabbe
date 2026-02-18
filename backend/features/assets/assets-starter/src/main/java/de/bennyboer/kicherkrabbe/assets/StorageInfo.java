package de.bennyboer.kicherkrabbe.assets;

import lombok.AllArgsConstructor;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class StorageInfo {

    long usedBytes;

    long limitBytes;

    public static StorageInfo of(long usedBytes, long limitBytes) {
        return new StorageInfo(usedBytes, limitBytes);
    }

}
