package de.bennyboer.kicherkrabbe.inquiries;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

import java.util.Optional;

import static lombok.AccessLevel.PRIVATE;

@Value
@With(PRIVATE)
@AllArgsConstructor(access = PRIVATE)
public class Fingerprint {

    @Nullable
    String ipAddress;

    public static Fingerprint of(@Nullable String ipAddress) {
        return new Fingerprint(ipAddress);
    }

    public Optional<String> getIpAddress() {
        return Optional.ofNullable(ipAddress);
    }

    public Fingerprint anonymize() {
        return withIpAddress(null);
    }

    @Override
    public String toString() {
        return "Fingerprint(ipAddress=%s)".formatted(ipAddress);
    }

}
