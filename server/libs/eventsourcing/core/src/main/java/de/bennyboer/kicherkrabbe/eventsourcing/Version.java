package de.bennyboer.kicherkrabbe.eventsourcing;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class Version implements Comparable<Version> {

    long value;

    public static Version zero() {
        return new Version(0);
    }

    public static Version of(long value) {
        check(value >= 0, "Version must be greater than 0");

        return new Version(value);
    }

    public Version increment() {
        return new Version(value + 1);
    }

    public Version decrement() {
        if (isZero()) {
            throw new IllegalStateException("Version must be greater than 0");
        }

        return new Version(value - 1);
    }

    public boolean isPreviousTo(Version other) {
        return value == other.value - 1;
    }

    public boolean isZero() {
        return value == 0;
    }

    @Override
    public String toString() {
        return String.format("Version(%d)", value);
    }

    @Override
    public int compareTo(Version other) {
        return Long.compare(value, other.value);
    }

}
