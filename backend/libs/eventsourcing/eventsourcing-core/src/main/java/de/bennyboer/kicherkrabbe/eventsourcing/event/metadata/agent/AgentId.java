package de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class AgentId {

    private static final AgentId SYSTEM = new AgentId("SYSTEM");

    private static final AgentId ANONYMOUS = new AgentId("ANONYMOUS");

    String value;

    public static AgentId of(String value) {
        notNull(value, "AgentId must be given");
        check(!value.isBlank(), "AgentId must not be blank");

        return new AgentId(value);
    }

    public static AgentId system() {
        return SYSTEM;
    }

    public static AgentId anonymous() {
        return ANONYMOUS;
    }

    @Override
    public String toString() {
        return String.format("AgentId(%s)", value);
    }

}
