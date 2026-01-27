package de.bennyboer.kicherkrabbe.permissions;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class Action {

    String name;

    public static Action of(String name) {
        notNull(name, "Action name must be given");
        check(!name.isBlank(), "Action name must not be blank");

        return new Action(name);
    }

    @Override
    public String toString() {
        return String.format("Action(%s)", name);
    }

}
