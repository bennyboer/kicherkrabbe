package de.bennyboer.kicherkrabbe.patching;

import de.bennyboer.kicherkrabbe.commons.Preconditions;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.UUID;

import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class InstanceId {

    String value;

    public static InstanceId create() {
        return new InstanceId(UUID.randomUUID().toString());
    }

    public static InstanceId of(String value) {
        Preconditions.notNull(value, "Instance ID must not be null");
        Preconditions.check(!value.isBlank(), "Instance ID must not be blank");

        return new InstanceId(value);
    }

}
