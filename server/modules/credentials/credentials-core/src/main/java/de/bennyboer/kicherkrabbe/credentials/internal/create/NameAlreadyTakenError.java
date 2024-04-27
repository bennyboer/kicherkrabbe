package de.bennyboer.kicherkrabbe.credentials.internal.create;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
public class NameAlreadyTakenError extends Exception {

    String name;

    public NameAlreadyTakenError(String name) {
        super("Name already taken: " + name);
        this.name = name;
    }

}
