package de.bennyboer.kicherkrabbe.eventsourcing.aggregate;

import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class ApplyCommandResult {

    /**
     * The events that were emitted by the aggregate as an result of the command.
     * There may be zero, one or multiple events.
     */
    List<Event> events;

    public static ApplyCommandResult of(List<Event> events) {
        notNull(events, "Events must be given");

        return new ApplyCommandResult(events);
    }

    public static ApplyCommandResult of(Event... events) {
        return ApplyCommandResult.of(List.of(events));
    }

    public static ApplyCommandResult of() {
        return ApplyCommandResult.of(List.of());
    }

}
