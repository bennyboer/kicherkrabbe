package de.bennyboer.kicherkrabbe.messaging;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RoutingKey {

    String[] parts;

    public static RoutingKey parse(String key) {
        notNull(key, "Routing key must be given");
        check(!key.isBlank(), "Routing key must not be empty");

        return new RoutingKey(key.split("\\."));
    }

    public static RoutingKey ofParts(String... parts) {
        notNull(parts, "Routing key parts must be given");
        check(parts.length > 0, "Routing key parts must not be empty");

        return new RoutingKey(parts);
    }

    public String asString() {
        return String.join(".", parts);
    }

    @Override
    public String toString() {
        return "RoutingKey(%s)".formatted(asString());
    }

}
