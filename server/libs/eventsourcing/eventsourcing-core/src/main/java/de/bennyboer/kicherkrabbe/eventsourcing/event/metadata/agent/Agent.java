package de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentType.*;
import static lombok.AccessLevel.PRIVATE;

/**
 * An agent may be a user or a system that performs commands on an aggregate.
 */
@Value
@AllArgsConstructor(access = PRIVATE)
public class Agent {

    AgentType type;

    AgentId id;

    public static Agent of(AgentType type, AgentId id) {
        notNull(type, "AgentType must be given");
        notNull(id, "AgentId must be given");

        return new Agent(type, id);
    }

    public static Agent user(AgentId id) {
        return of(USER, id);
    }

    public static Agent system() {
        return of(SYSTEM, AgentId.system());
    }

    public static Agent anonymous() {
        return of(ANONYMOUS, AgentId.anonymous());
    }

    public boolean isSystem() {
        return type == SYSTEM;
    }

    public boolean isAnonymous() {
        return type == ANONYMOUS;
    }

    public boolean isUser() {
        return type == USER;
    }

    @Override
    public String toString() {
        return String.format("Agent(%s, %s)", type, id);
    }

}
