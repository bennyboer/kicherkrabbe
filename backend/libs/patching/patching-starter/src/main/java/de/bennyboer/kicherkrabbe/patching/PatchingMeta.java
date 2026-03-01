package de.bennyboer.kicherkrabbe.patching;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Optional;

import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class PatchingMeta {

    int version;

    @Nullable
    InstanceId lockedBy;

    public static PatchingMeta of(int version, @Nullable InstanceId lockedBy) {
        return new PatchingMeta(version, lockedBy);
    }

    public Optional<InstanceId> getLockedBy() {
        return Optional.ofNullable(lockedBy);
    }

}
